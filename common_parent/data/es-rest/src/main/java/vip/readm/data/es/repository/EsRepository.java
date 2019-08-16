package vip.readm.data.es.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import vip.readm.data.es.EsRestClient;
import vip.readm.data.es.pojo.EsEntity;
import vip.readm.data.es.pojo.impl.Page;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @Author: Readm
 * @Date: 2019/8/14 20:31
 * @Version 1.0
 */

@Data
@AllArgsConstructor
public class EsRepository<T extends EsEntity, ID extends Serializable> extends EsRestClient implements ElasticsearchRepository<T, ID> {

    private Class<T> cl;
    private Class<ID> IdCl;

    @PostConstruct
    public void postConstruct(){
        if(cl!=null ){
            createDocument(1*10000*10000L);
        }
    }

    public  EsRepository(){
       this.getEntityClass();

    }

    public boolean createDocument(Long windowSize){
        boolean res= this.createDocument(cl);

        if(windowSize!=null){
            this.changeDocumentWinodwSize(cl,windowSize);
        }

        return res;
    }

    @Override
    public <S extends T> S index(S entity) {
        boolean res=super.createDocument(cl);
        return null;
    }


    @Override
    public <S extends T> S save(S entity) {
        String id=super.saveDocument(cl, entity);
        entity.setId(id);
        return entity;
    }


    @Override
    public Class<T> getEntityClass() {

        Class<T> tClass=null;
        try {
            tClass=(Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            this.cl=tClass;
            this.IdCl=(Class<ID>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1];

        }catch (Exception e){

        }
        return tClass;
    }


    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> iterable) {

        List list=(List) iterable;
        int size=super.saveDocuments(cl, list);

        if(size>0)
            list=list.subList(0, size-1);




        return list;
    }


    @Override
    public boolean existsById(ID id) {
        super.existsDocument(cl, new EsEntity().setId((String) id));
        return false;
    }


    @Override
    public long count() {

        long res=super.count(cl);

        return res;
    }

    @Override
    public void deleteById(ID id) {

        super.deleteDocument(cl,new EsEntity().setId((String) id));


    }

    @Override
    public void delete(T t) {
        super.deleteDocument(cl,t);

    }

    @Override
    public void deleteAll(Iterable<? extends T> iterable) {

        List list=(List)iterable;
        super.deleteDocuments(cl,list);

    }

    @Override
    public void deleteAll() {
        super.deleteAll(cl);

    }


    @Override
    public Iterable<T> findAll(Sort sort) {

        Page<T> pageOf=new Page<>(0,this.count(cl).intValue(),sort);

        Page<T> page= (Page) super.searchDocument(cl,null, null,pageOf,null);
        return page.getContent();
    }

    @Override
    public Iterable<T> findAll() {
        Page<T> pageOf=new Page<>(0,this.count(cl).intValue(),null);
        Page<T> page= (Page) super.searchDocument(cl,null, null,pageOf,null);
        return page.getContent();
    }


    @Override
    public Page findAll(Pageable pageable) {
        Page<T> page= (Page) super.searchDocument(cl,null, null,pageable,null);
        return page;
    }


    @Override
    public Iterable<T> search(QueryBuilder query) {
        Page<T> page= (Page) super.searchDocument(cl,query, null,null,null);
        return page.getContent();
    }

    @Override
    public Page search(QueryBuilder query, Pageable pageable) {
        Page<T> page= (Page) super.searchDocument(cl,query, null,pageable,null);
        return page;

    }

    public Page search(QueryBuilder query, QueryBuilder filter,Pageable pageable) {
        Page<T> page= (Page) super.searchDocument(cl,query, filter,pageable,null);
        return page;
    }


    public Page search(QueryBuilder query, QueryBuilder filter, Pageable pageable, HighlightBuilder highlightBuilder) {
        Page<T> page= (Page) super.searchDocument(cl,query, filter,pageable,highlightBuilder);
        return page;
    }


    @Override
    public void refresh() {
        super.refreshDocument(cl);
    }


    @Override
    public Iterable<T> findAllById(Iterable<ID> iterable) {

        BoolQueryBuilder boolQueryBuilder= QueryBuilders.boolQuery();
        iterable.forEach(id->{
            try {
                boolQueryBuilder.should(QueryBuilders.matchQuery("_id",id));
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        Iterable<T>  tIterable= search(boolQueryBuilder);
        return tIterable;
    }


    @Override
    public Optional<T> findById(ID id) {

        List<ID> ids=new ArrayList<>();
        ids.add(id);
        Iterator<T> iterator=  findAllById(ids).iterator();

        if(iterator.hasNext()){
            Optional<T> optionalT=Optional.of(iterator.next());
        }
        return Optional.empty();
    }


    public T _findById(ID id) {

        List<ID> ids=new ArrayList<>();
        ids.add(id);
        Iterator<T> iterator=  findAllById(ids).iterator();

        if(iterator.hasNext()){
           return iterator.next();
        }
        return null;
    }


    public static HighlightBuilder getHighlightBuilderFromFieldNames(List<String> fieldNames){

        return EsRestClient.getHighlightBuilderFromFieldNames(fieldNames);
    }

    public static HighlightBuilder getHighlightBuilderFromFieldNames(String ... fieldNames){
        return EsRestClient.getHighlightBuilderFromFieldNames(fieldNames);
    }


    //暂时还未实现
    @Override
    @Deprecated
    public Page search(SearchQuery searchQuery) {

        return null;
    }

    @Override
    @Deprecated
    public Page searchSimilar(T entity, String[] fields, Pageable pageable) {
        return null;
    }


}
