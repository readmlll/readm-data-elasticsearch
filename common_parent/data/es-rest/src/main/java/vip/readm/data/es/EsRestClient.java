package vip.readm.data.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.inject.name.Names;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import vip.readm.common.utils.ClassUtils;
import vip.readm.common.utils.SnowFlake;
import vip.readm.common.utils.StreamUtils;
import vip.readm.data.es.pojo.EsEntity;
import vip.readm.data.es.pojo.impl.Page;
import vip.readm.data.es.properties.EsProperties;
import vip.readm.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Readm
 * @Date: 2019/8/14 6:25
 * @Version 1.0
 */


@Data
public class EsRestClient {

    /**
     * 封装 RestHighLevelClient RestClient 的操作
     */


    public static   RestHighLevelClient restHighLevelClient;

    public static RestClient restClient;

    public static EsJsonUtils esJsonUtils;

    public static ObjectMapper objectMapper;

    public static EsProperties esProperties;

    Log log= LogFactory.getLog(EsRestClient.class);





    private String generateIdOnIdIsEmpty(String id){
        if(StringUtils.isEmpty(id)){
            SnowFlake snowFlake=new SnowFlake(esProperties.dataCenterId,esProperties.machineId);
            id=String.valueOf(snowFlake.nextId());
        }
        return id;
    }


    /**
     * 发送原始的es  http请求
     * @param httpMethod  请求类型
     * @param urlPath      请求路径 不包含主机端口协议 只有路径部分
     * @param urlParamMap  路径的参数map
     * @param bodyJson     请求体
     * @param canThrowException 能否抛出异常
     * @return
     */
    public String OriginRequest(HttpMethod httpMethod, String urlPath, Map<String,String> urlParamMap, String bodyJson,Boolean canThrowException){

        String res="";
        Request request = new Request(
                httpMethod.name(),
                urlPath);

        if(urlParamMap!=null){
            urlParamMap.forEach((key,val)->{
                request.addParameter(key, val);
            });

        }

        if(bodyJson!=null){
            request.setEntity(new NStringEntity(
                    bodyJson,
                    ContentType.APPLICATION_JSON));
        }

        try {
            Response response = restClient.performRequest(request);
            res= StreamUtils.readToString( response.getEntity().getContent(),true);
        } catch (IOException e) {
            e.printStackTrace();

            if(canThrowException!=null && canThrowException){
               throw  new RuntimeException("EsRestClient 发送json请求失败. 路径:"+urlPath+"请求体:\n"+bodyJson);
            }

        }
        return res;

    }

    /**
     * 发送原始的es  http请求
     * @param httpMethod  请求类型
     * @param urlPath      请求路径 不包含主机端口协议 只有路径部分
     * @param urlParamMap  路径的参数map
     * @param bodyJson     请求体
     * @return
     */
    public String OriginRequest(HttpMethod httpMethod, String urlPath, Map<String,String> urlParamMap, String bodyJson){
        return  OriginRequest(httpMethod,urlPath,urlParamMap,bodyJson,null);
    }

    /**
     * 兼容 spring data jpa 部分注解
     * 创建文档
     * @param cl  文档entity Class
     * @return
     */
    public boolean createDocument(Class cl){

       boolean exist= existDefineDocument(cl);
       if(exist){
           long count=count(cl);
           log.info("创建es索引,已经存在,实体类:"+cl.getName()+",文档数量:"+count);
           return true;
       }

        EsJsonUtils.EsIndex esIndex=esJsonUtils.parseEsEntity(cl);
        String mappingJspn=esIndex.getCreateJson();
        CreateIndexResponse response=null;
        CreateIndexRequest createIndexRequest=new CreateIndexRequest(esIndex.getIndexName());

        createIndexRequest.settings(Settings.builder()
                .put("index.number_of_shards", esProperties.number_of_shards)
                .put("index.number_of_replicas", esProperties.number_of_replicas)
        );

        createIndexRequest.mapping(esIndex.getIndexType(),mappingJspn , XContentType.JSON);

        try {
            response=restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(response!=null && response.isAcknowledged()){

            log.info("创建es索引成功,实体类:"+cl.getName());
            return true;
        }

        log.error("创建es索引失败,实体类:"+cl.getName());
        return false;
    }


    /**
     * 改变窗口大小
     * @param cl  文档entity Class
     * @param size
     * @return
     */
    public boolean changeDocumentWinodwSize(Class cl,Long size){

        EsJsonUtils.EsIndex esIndex=esJsonUtils.parseEsEntity(cl);
        boolean res=false;
        String sizeStr=String.valueOf(size);
        String json="{ \"index\" : { \"max_result_window\" : "+sizeStr+" } }";

        Request request=new Request("PUT", json);

        String url=esIndex.indexName+"/_settings";
        try {
           String string = OriginRequest(HttpMethod.PUT,url,null,json);
           if(string.contains("acknowledged") && string.contains("true") ){
               return true;
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


    /**
     * 查看文档是否已经定义
     * @param cl
     * @return
     */
    public boolean existDefineDocument(Class cl){

        EsJsonUtils.EsIndex esIndex=esJsonUtils.parseEsEntity(cl);

        try {
           String res= OriginRequest(HttpMethod.GET,esIndex.indexName,null,null);
           if(res.contains(esIndex.indexName)){
               return true;
           }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 增加/修改 文档
     * @param cl  文档entity Class
     * @param entity     文档entity 实体对象  实体id字段 与 元数据id字段统一，为null则自己生成
     * @return      返回元数据id字段 出错返回空字符串
     */
    public String saveDocument(Class cl, EsEntity entity){

        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        String id=entity.getId();

        try {

           esIndex= esJsonUtils.parseEsEntity(cl);

           //如果id为空生成随机id
           id= generateIdOnIdIsEmpty(id);
           entity.setId(id);

            IndexRequest request = new IndexRequest(
                    esIndex.indexName,
                    esIndex.indexType,
                    id
                    );


            String jsonString = objectMapper.writeValueAsString(entity);

            request.source(jsonString, XContentType.JSON);

            IndexResponse response= restHighLevelClient.index(request, RequestOptions.DEFAULT);

            if(response==null){
                log.error("增加/修改 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,IndexResponse为null");
                return "";
            }

           int state= response.status().getStatus();
            if( !(state>=200 &&state <=299) ){
                log.error("增加/修改 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,状态码:"+state);
                return "";
            }


        } catch (Exception e) {
            log.error("增加/修改 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,发生流程异常");
            e.printStackTrace();
            return "";
        }

        log.info("增加/修改 文档:"+esIndex.indexName+"/"+esIndex.indexType+"/"+id+" 成功");
        refreshDocument(cl);
        return id;
    }

    /**
     * 批量 增加/修改 文档
     * @param cl  文档entity Class
     * @param entitys     文档entity 实体对象   实体id字段 与 元数据id字段统一，为null则自己生成
     * @return      返回元数据id字段 出错返回空字符串
     */
    public Integer saveDocuments(Class cl, List<? extends EsEntity> entitys){

        Integer successCount=0;
        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);
        BulkRequest bulkRequest = new BulkRequest();
        String id=null;

        bulkRequest.timeout(TimeValue.timeValueMinutes(60*5));
        try {

            for(EsEntity entity:entitys){

                id= entity.getId();
                id=generateIdOnIdIsEmpty(id);
                entity.setId(id);
                IndexRequest request = new IndexRequest(
                        esIndex.indexName,
                        esIndex.indexType,
                        id
                );
                String jsonString = objectMapper.writeValueAsString(entity);
                request.source(jsonString, XContentType.JSON);

                bulkRequest.add(request);
            }



            BulkResponse response=restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

            BulkItemResponse[] bulkResponse=response.getItems();

            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                int state=0;
                DocWriteResponse itemResponse = bulkItemResponse.getResponse();

                switch (bulkItemResponse.getOpType()) {
                    case INDEX:
                    case CREATE:
                        IndexResponse indexResponse = (IndexResponse) itemResponse;

                        state= indexResponse.status().getStatus();
                        if( (state>=200 &&state <=299) ){
                            successCount++;
                        }

                        break;
                    case UPDATE:
                        UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                        state= updateResponse.status().getStatus();
                        if( (state>=200 &&state <=299) ){
                            successCount++;
                        }
                        break;
                    case DELETE:
                        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                }
            }


            if(response==null){
                log.error("批量 增加/修改 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,BulkResponse为null");
                return successCount;
            }

            int state= response.status().getStatus();
            if( !(state>=200 &&state <=299) ){
                log.error("批量 增加/修改 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,状态码:"+state);
                return successCount;
            }


        } catch (Exception e) {
            log.error("批量 增加/修改 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,发生流程异常");
            e.printStackTrace();
            return successCount;
        }

        log.info("批量 增加/修改 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,成功次数:"+successCount);

        refreshDocument(cl);
        return successCount;
    }



    /**
     * 查询某文档是否存在 根据实体id字段
     * @param cl
     * @param esEntity
     * @return
     */
    public boolean existsDocument(Class cl,EsEntity esEntity){

        boolean exists=false;
        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);

        GetRequest getRequest = new GetRequest(
                esIndex.indexName,
                esIndex.indexType,
                esEntity.getId());

        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        try {
             exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("查询是否存在 文档:"+esIndex.indexName+"/"+esIndex.indexType+"/"+esEntity.getId()+" ,失败,发生流程异常");
        }
        return exists;
    }


    /**+
     * 删除 指定的文档  根据实体对象id
     * @param cl
     * @param entity
     * @return
     */
    public boolean deleteDocument(Class cl,EsEntity entity){

        Integer successCount=0;
        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);
        DeleteRequest deleteRequest = new DeleteRequest(esIndex.indexName, esIndex.indexType, entity.getId());

        try {
            DeleteResponse response=   restHighLevelClient.delete(deleteRequest,RequestOptions.DEFAULT);

            if(response==null){
                log.error("删除文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,DeleteResponse为null");
                return false;
            }

            int state= response.status().getStatus();
            if( !(state>=200 &&state <=299) ){
                log.error("删除文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,状态码:"+state);
                return false;
            }

        } catch (IOException e) {
            log.error("删除文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,发生流程异常");
            e.printStackTrace();
            return false;
        }

        log.info("删除文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,成功");

        refreshDocument(cl);

        return true;
    }



    /**+
     * 删除 指定的文档集合根据实体对象id
     * @param cl
     * @param entitys
     * @return 返回成功个数
     */
    public Integer deleteDocuments(Class cl,List<? extends EsEntity> entitys){

        Integer successCount=0;
        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);
        BulkRequest bulkRequest = new BulkRequest();
        String id=null;
        bulkRequest.timeout(TimeValue.timeValueMinutes(60*5));

        try {
            for(EsEntity entity:entitys){
                id= entity.getId();
                id=generateIdOnIdIsEmpty(id);
                entity.setId(id);
                DeleteRequest request = new DeleteRequest(
                        esIndex.indexName,
                        esIndex.indexType,
                        id
                );
                bulkRequest.add(request);
            }

            BulkResponse response=restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

            BulkItemResponse[] bulkResponse=response.getItems();

            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                int state=0;
                DocWriteResponse itemResponse = bulkItemResponse.getResponse();

                switch (bulkItemResponse.getOpType()) {
                    case INDEX:
                    case CREATE:
                        IndexResponse indexResponse = (IndexResponse) itemResponse;
                        break;

                    case UPDATE:
                        UpdateResponse updateResponse = (UpdateResponse) itemResponse;
                        break;

                    case DELETE:
                        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
                        state= deleteResponse.status().getStatus();
                        if( (state>=200 &&state <=299) ){
                            successCount++;
                        }
                        break;
                }
            }


            if(response==null){
                log.error("批量 删除 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,BulkResponse为null");
                return successCount;
            }

            int state= response.status().getStatus();
            if( !(state>=200 &&state <=299) ){
                log.error("批量 删除 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,状态码:"+state);
                return successCount;
            }


        } catch (Exception e) {
            log.error("批量 删除 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,发生流程异常");
            e.printStackTrace();
            return successCount;
        }

        log.info("批量 删除 文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,成功次数:"+successCount);

        refreshDocument(cl);
        return successCount;

    }


    /**
     * 删除全部文档
     * @return
     */
    public Integer deleteAll(Class cl){

        Integer successCount=0;
        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);

        String json="{\n" +
                "  \"query\": { \n" +
                "    \"match_all\": {\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Integer count=0;

        while (true){
            try {
                String rjson=OriginRequest(HttpMethod.POST,esIndex.indexName+"/_delete_by_query?refresh&slices=5&pretty",null,json);
                Map map=objectMapper.readValue(rjson, Map.class);
                Object o=map.get("deleted");
                if( o instanceof Integer){
                    successCount+=(Integer) o;
                }else{
                    successCount+=Integer.parseInt( (String)o );
                }
            } catch (IOException e) {
               // e.printStackTrace();
                count++;
                if(count<10){
                    refreshDocument(cl);
                    continue;
                }else{
                    break;
                }

            }
            break;
        }
        refreshDocument(cl);
        log.info("删除全部文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,成功次数:"+successCount+" ,重试次数:"+count);


        return successCount;
    }

    /**
     * 查询全部文档总数
     * @return  出错返回-1
     */
    public Long count(Class cl){

        Long successCount=-1L;
        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);


       String json=OriginRequest(HttpMethod.GET,esIndex.indexName+"/_count",null,"");

        System.out.println(json);
        try {
            Map map=objectMapper.readValue(json, Map.class);

            Object o=map.get("count");
            if( o instanceof Long ){
                successCount=(Long) o ;
            }
            else if( o instanceof Integer ){
                successCount=((Integer)o).longValue();
            }
            else{
                successCount=Long.valueOf( (String)o );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("查询文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,总数:"+successCount);
        return successCount;
    }


    /**
     * queryBuilder 转换成 SearchRequest
     *
     * @param query QueryBuilder对象
     * @param cl
     * @param pageable  分页和排序
     * @return
     */
    public SearchRequest queryBuilderToSearchRequest(QueryBuilder query, Class cl, Pageable pageable, HighlightBuilder highlightBuilder){

        SearchRequest searchRequest=new SearchRequest();
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();


        //高亮设置
        if(highlightBuilder!=null){
            sourceBuilder.highlighter(highlightBuilder);
        }

        //分页和排序设置
        if(pageable!=null){
            sourceBuilder.from(pageable.getPageNumber());
            sourceBuilder.size(pageable.getPageSize());

            final SearchSourceBuilder searchSourceBuilder=sourceBuilder;
            if(pageable.getSort()!=null){
                //排序设置
                pageable.getSort().get().forEach(order -> {
                    String direction= order.getDirection().name().toUpperCase();
                    String field= order.getProperty();
                    switch (direction){
                        case "ASC":
                            searchSourceBuilder.sort(new FieldSortBuilder(field).order(SortOrder.ASC));
                            break;
                        case "DESC":
                            searchSourceBuilder.sort(new FieldSortBuilder(field).order(SortOrder.DESC));
                            break;
                    }
                });
            }
        }



        if(cl!=null){
            EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
            esIndex= esJsonUtils.parseEsEntity(cl);
            searchRequest.indices(esIndex.indexName);
            searchRequest.types(esIndex.indexType);
        }

        sourceBuilder.query(query);
        sourceBuilder.timeout(new TimeValue(esProperties.commonTimeOut, TimeUnit.MILLISECONDS));
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }


    /**
     * 通用查询
     * @param cl  文档实体class
     * @param query    查询条件
     * @param filter   筛选条件
     * @param pageable 分页和排序条件
     * @param highlightBuilder 高亮条件设置
     * @return
     */
    public Page<EsEntity> searchDocument(Class cl, QueryBuilder query, QueryBuilder filter, Pageable pageable, HighlightBuilder highlightBuilder){

        refreshDocument(cl);

        SearchRequest searchRequest=null;
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();

        //查询条件设置
        if(query!=null){
            boolQueryBuilder.must(query);
        }

        //筛选设置
        if(filter!=null){
            boolQueryBuilder.filter(filter);
        }

        //分页设置 如果没有分页信息 自动分页为 第一页  10条数据
        if(pageable==null){
            pageable=new Page<EsEntity>(0, 10,null);
        }



        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);
        final  EsJsonUtils.EsIndex esIndexFinal=esIndex;

        List<EsEntity> resList=new ArrayList<>();
        Page<EsEntity> page=null;
        SearchResponse response=null;


        try {
            //转换查询请求
            searchRequest=queryBuilderToSearchRequest(boolQueryBuilder,cl,pageable,highlightBuilder);

            response= restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            if(response==null){
                log.error("查询文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,SearchResponse为null");
                return page;
            }

            Arrays.stream(response.getHits().getHits())
                    .forEach(item -> {


                        Object o=ClassUtils.fillBeanFieldFromMap(cl, item.getSourceAsMap());


                        if(highlightBuilder!=null){

                            try {
                                Map<String,Object> fieldsMap=new HashMap<>();
                                item.getHighlightFields().forEach((k,v)->{

                                    Text[] texts=v.getFragments();
                                    StringJoiner stringJoiner=new StringJoiner("");
                                    for (Text text:texts){
                                        stringJoiner.add(text.string());
                                    }

                                    fieldsMap.put(k, stringJoiner.toString());
                                } );
                                o= ClassUtils.fillBeanInstansFieldFromMap(cl,fieldsMap,o);
                            }catch (Exception e){
                                log.error("查询文档:"+esIndexFinal.indexName+"/"+esIndexFinal.indexType+" ,高亮设置失败,可能原因:查询的属性不是String");
                            }
                        }
                        if(o!=null){
                            resList.add((EsEntity) o);
                        }
                    });



            log.info("查询文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,成功. 符合文档总数:"+response.getHits().totalHits+", 实际封装返回数据数目:"+resList.size());

        } catch (Exception e) {
            log.error("查询文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,流程异常");
            e.printStackTrace();
        }


        page=new Page<>(pageable.getPageNumber(), pageable.getPageSize(), response.getHits().totalHits, resList);
        return page;
    }



    public void refreshDocument(Class cl){

        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);


        try {


//            String json="";
//            String url=esIndex.indexName+"/_cache/clear?pretty";
//            String res="";
//            OriginRequest(HttpMethod.GET,url,null,json,true);

            UpdateRequest updateRequest=new UpdateRequest();
            updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            updateRequest.index(esIndex.indexName);
            updateRequest.type(esIndex.indexType);
            UpdateResponse response=restHighLevelClient.update(updateRequest,RequestOptions.DEFAULT);

            if(response==null){
               throw   new RuntimeException("response 为null");
            }

            int state= response.status().getStatus();
            if( !(state>=200 &&state <=299) ){
                throw  new RuntimeException("状态码:"+state);
            }
        } catch (Exception e) {

            //log.info("刷新缓存:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败 \n"+e.getMessage());
            //e.printStackTrace();
        }


        log.info("刷新缓存:"+esIndex.indexName+"/"+esIndex.indexType+" ,成功");
    }

    /**
     * 通用查询
     * @param cl  文档实体class
     * @param searchRequest    请求条件
     * @return
     */
    public Page<EsEntity> searchDocument(Class cl,SearchRequest searchRequest){

        refreshDocument(cl);

        EsJsonUtils.EsIndex esIndex=new EsJsonUtils.EsIndex();
        esIndex= esJsonUtils.parseEsEntity(cl);
        List<EsEntity> resList=new ArrayList<>();
        Page<EsEntity> page=null;
        SearchResponse response=null;


        try {
            response= restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            if(response==null){
                log.error("查询文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,SearchResponse为null");
                return page;
            }

            Arrays.stream(response.getHits().getHits())
                    .forEach(item -> {
                        Object o=(Object)ClassUtils.fillBeanFieldFromMap(cl, item.getSourceAsMap());
                        if(o!=null){
                            resList.add((EsEntity) o);
                        }
                    });



            log.info("查询文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,成功. 符合文档总数:"+response.getHits().totalHits+", 实际封装返回数据数目:"+resList.size());

        } catch (Exception e) {
            log.error("查询文档:"+esIndex.indexName+"/"+esIndex.indexType+" ,失败,流程异常");
            e.printStackTrace();
        }


        Page pageable=new Page(searchRequest.source().from(), searchRequest.source().size(), null);
        page=new Page<>(pageable.getPageNumber(), pageable.getPageSize(), response.getHits().totalHits, resList);

        return page;
    }


    /**
     * 根据属性名字 列表 方便的构造HighlightBuilder对象
     * @param fieldNames
     * @return
     */
    public static HighlightBuilder getHighlightBuilderFromFieldNames(List<String> fieldNames){

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
        highlightBuilder.field(highlightUser);
        fieldNames.forEach(name->{
            HighlightBuilder.Field highlightField = new HighlightBuilder.Field(name);
            highlightField.highlighterType("unified");
            highlightBuilder.field(highlightField);
        });
        return  highlightBuilder;
    }

    public static HighlightBuilder getHighlightBuilderFromFieldNames(String ... fieldNames){
        HighlightBuilder highlightBuilder = null;
        List<String> names=  Arrays.asList(fieldNames);
        highlightBuilder=getHighlightBuilderFromFieldNames(names);
        return  highlightBuilder;
    }

}
