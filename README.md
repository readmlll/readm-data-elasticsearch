# readm-data-elasticsearch(轻量级spring boot  集成 elasticsearch restClient方式连接框架)
### 轻量级elasticsearch restClient方式连接框架/阿里云elasticsearch连接框架



### （实测阿里云es 6.7.0没问题，注意开启公网访问，用户名密码，以及设置好白名单，实际上高低版本的client支持6.x-7.x。 7.x以上取消type的概念，重写构造不同高低版本client代码即可兼容大部分es版本）

#### 基于spring boot,(spring-data-es部分注解)、elasticsearch高低版本client

#### 封装的rest版本es客户端. 仿照spring-data 一贯的使用方式，只需少量代码即可转换transport连接方式到restClient连接

## 单独依赖版本，spring boot自动配置，使用方引入只需定义好文档类，自动创建es文档，定义好仓库类开箱即用常用操作，自动配置
## 实现全部ElasticsearchRepository接口



## 更新8.16 :

#### 修复已知问题, 定义仓库对象时 除了继承EsRestClient外，因为没用动态代理方式，所以请加@Component 注解



###### 优点:

目前最新spring-data-es 是3.1.0  Elasticsearch模板还是不支持restClient连接 （据说3.2.0 会支持）

使用本框架
小项目，轻量级使用es的项目，快速方便的转换transport连接方式到restClient连接，还是使用spring-data-es的习惯，一套代码两种方式连接方式可跑。 

我自己的是从自己搭建的es迁移到阿里云es 6.7.0 ，阿里云不支持transport
常用操作的封装，创建文档，查询总数，批量增加修改，批量删除，高亮显示

###### 缺点：

自由度不够，更多属性需要自己修改源码。

于spring-data-es不同点总结：
1.实体对象需要继承EsEntity，必须有id字段且其为string类型 且该字段需要标注@Field注解。
2.仓库对象 不再使用继承接口，需要继承EsRepository类。 
3.没有动态生成方法。 但支持ElasticsearchRepository所有接口声明的方法



###### 关于反射的冗余：

本来是想写工具类的，所以EsRestClient有反射代码的冗余 介意的话自己修改。 

有点功能洁癖写着写着 还是当demo级别框架写
我自己是使用编译jar包 在别的项目中引入本"demo框架" 当做正常框架使用。
你可以抽离代码放到你的项目中，也可以选择和我一样编译jar，引入依赖。 
我会在github上提供一份编译好的jar


本框架主要类目录结构 (详细目录结构在下方)

![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/dir1.png)




###### 项目示例和框架源码目录结构:

common_parent为框架父级总管理项目
  data为次级管理项目
    es-rest为框架项目

paper_site为 使用本框架示例 
	打开idea编译好common_parent项目 安装jar到本地

​	再打开paper_site项目(添加好依赖)
​	填写好yml配置信息 运行readm.paper.PaperApplicationTests.contextLoads 测试方法，
​	即可自动创建es文档 和相关测试代码  (注意打开相关注释等等)





![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/dir2.png)
![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/dir3.png)




文档对象声明 和 其字段声明



![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/U0XXWZV6%24G%40%7BMBYH2~%24WH14.png)



仓库对象定义



![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/D3%5BKBZ39E37NWRC6XMK5MX6.png)





测试仓库方法 



![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/test.png)











例子

 仓库对象定义例子

```java
package readm.paper.es.repository;

import lombok.NoArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import readm.paper.es.pojo.SentenceEntity_ES;
import vip.readm.data.es.repository.EsRepository;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Author: Readm
 * @Date: 2019/8/14 4:29
 * @Version 1.0
 */


@NoArgsConstructor
@Component
public class SentenceRepository_ES extends EsRepository<SentenceEntity_ES,String> {



}
```

文档实体声明

```java
package readm.paper.es.pojo;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import vip.readm.data.es.pojo.EsEntity;

/**
 * @Author: Readm
 * @Date: 2019/8/14 4:29
 * @Version 1.0
 */


@Data
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
//@Document(indexName = "idx_paper", type = "sentence")
@Document(indexName = "idx_test_paper_test_003", type = "sentence")
public class SentenceEntity_ES  extends EsEntity {

    @Id
    @Field(type = FieldType.Keyword,index = true,store = true)
    private String id;

    //用于按id排序
    @Field(type = FieldType.Integer,index = true,store = false)
    private Integer orderId;


    public SentenceEntity_ES setId(String id){
        this.id=id;
        try {
            orderId=Integer.parseInt(id);
        }catch (Exception e){
        }
        return this;
    }

    //原句需要分词 用于搜索
    @Field(type = FieldType.Text,index = true,store = true,searchAnalyzer="standard",analyzer="standard")
    private String text;

    public SentenceEntity_ES setText(String text){

        this.text=text;
        this.setFullText(text);

        return this;
    }

    //原句不分词版
    @Field(type = FieldType.Keyword,index = true,store = false)
    private String fullText;



    //翻译后的中文句子
    @Field(type = FieldType.Text,index = true,store = true,searchAnalyzer="ik_smart",analyzer="ik_max_word")
    private String translateText;

    public SentenceEntity_ES setTranslateText(String translateText){

        this.translateText=translateText;
        this.fullTranslateText=translateText;

        return this;
    }



    //翻译后的中文句子 不分词版本
    @Field(type = FieldType.Keyword,index = true,store = false)
    private String fullTranslateText;


    //文章来源 目前没有用于搜索 ，但是还是建立下索引以防万一
    @Field(type = FieldType.Keyword,index = true,store = true)
    private String articleUrl;


    //图片链接 目前没有用于搜索 ，但是还是建立下索引以防万一
    @Field(type = FieldType.Keyword,index = true,store = true)
    private  String imgUrl;


    //句子模块类型 需要被搜索 但是不分词
    @Field(type = FieldType.Keyword,index = true,store = true)
    private  String type;


    //原句md5
    @Field(type = FieldType.Keyword,index = true,store = false)
    private String textMd5;





}
```



编写业务代码使用仓库对象

```java
package readm.paper.controller;

import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import readm.paper.es.pojo.SentenceEntity_ES;
import readm.paper.es.repository.SentenceRepository_ES;
import vip.readm.data.es.pojo.impl.Page;
import vip.readm.data.es.repository.EsRepository;

/**
 * @Author: Readm
 * @Date: 2019/8/16 16:25
 * @Version 1.0
 */

@RestController
public class TestController {



    @Autowired
    SentenceRepository_ES sentenceRepository_es;

    @GetMapping("/test")
    public Object testGet(
           @RequestParam(required = false) String q
    ){



        MultiMatchQueryBuilder multiMatchQueryBuilder=QueryBuilders.multiMatchQuery("测试修改", "translateText","fullTranslateText");
        Sort sort= Sort.by(Sort.Direction.ASC,"orderId");
        Page<SentenceEntity_ES> page=new Page<>(0,10,sort);

        HighlightBuilder highlightBuilder=null;
        highlightBuilder= EsRepository.getHighlightBuilderFromFieldNames("translateText","fullTranslateText");
        sentenceRepository_es.search(multiMatchQueryBuilder, null, page, highlightBuilder).getContent()
                .forEach(item->{

                    System.out.println(item);

                });

        return "GetMapping q="+q;
    }


    @PostMapping("/test")
    public Object testPost(
            @RequestParam(required = false) String q
    ){

        return "PostMapping q="+q;
    }


    @PutMapping("/test")
    public Object testPut(
            @RequestParam(required = false) String q
    ){

        return "PutMapping q="+q;
    }


    @DeleteMapping("/test")
    public Object testDelete(
            @RequestParam(required = false) String q
    ){

        return "DeleteMapping q="+q;
    }


}
```





仓库对象使用2：

```
package readm.paper;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import readm.paper.es.pojo.SentenceEntity_ES;
import readm.paper.es.repository.SentenceRepository_ES;
import vip.readm.data.es.EsRestClient;
import vip.readm.data.es.config.EsConfig;
import vip.readm.data.es.pojo.impl.Page;
import vip.readm.data.es.repository.EsRepository;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PaperApplicationTests {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    RestClient restClient;

    @Autowired
    SentenceRepository_ES sentenceRepository_es;




    @Test
    public void us(){

    }



    @Test
    public void contextLoads() {


        System.out.println(restHighLevelClient);

        List<SentenceEntity_ES> sentencs=new ArrayList<>();

        for(int i=0;i<3;i++){

            SentenceEntity_ES sentenceEntity_es=new SentenceEntity_ES().setText(i+"test").setTranslateText("测试修改").setArticleUrl("www.baiar.com").setImgUrl("imgurl")
                    .setType("head").setTextMd5("5556");

            sentencs.add(sentenceEntity_es);
        }

        Boolean bRes=false;
        Integer nRes=0;
        sentencs.get(0).setId("5");
        sentencs.get(1).setId("8");
        sentencs.get(2).setId("10");

        sentenceRepository_es.saveAll(sentencs);
        //bRes=sentenceRepository_es
       // nRes=sentenceRepository_es.deleteAll(sentencs);
        //System.out.println(sentenceRepository_es.deleteAll());

      //  System.out.println(res);

        System.out.println("当前总数："+sentenceRepository_es.count());
        TermQueryBuilder termQueryBuilder= QueryBuilders.termQuery("translateText", "测试");

        MultiMatchQueryBuilder multiMatchQueryBuilder=QueryBuilders.multiMatchQuery("测试修改", "translateText","fullTranslateText");

        Sort sort= Sort.by(Sort.Direction.ASC,"orderId");
        Page<SentenceEntity_ES> page=new Page<>(0,10,sort);

        sentenceRepository_es.findAll(page).getContent().forEach(item ->{
            System.out.println(item);
        });


        sentenceRepository_es.deleteAll();


        sentenceRepository_es.findAll(page).getContent().forEach(item ->{
            System.out.println(item);
        });




        sentenceRepository_es.saveAll(sentencs);
        // 高亮设置

            HighlightBuilder.Field highlightField = new HighlightBuilder.Field("translateText");
            highlightField.highlighterType("unified");

            HighlightBuilder.Field highlightField2 = new HighlightBuilder.Field("fullTranslateText");
            highlightField2.highlighterType("unified");

            HighlightBuilder highlightBuilder = new HighlightBuilder();
            HighlightBuilder.Field highlightUser = new HighlightBuilder.Field("user");
            highlightBuilder.field(highlightField);
            highlightBuilder.field(highlightUser);

            highlightBuilder.field(highlightField2);





        System.out.println();
        System.out.println();
        System.out.println("=============================highlightBuilder===========================================");


        sentenceRepository_es.search(multiMatchQueryBuilder, null, page, highlightBuilder).getContent()
        .forEach(item->{

            System.out.println(item);

        });

        System.out.println();
        System.out.println();
        System.out.println("=============================highlightBuilder  utils===========================================");

        highlightBuilder=null;
        highlightBuilder= EsRepository.getHighlightBuilderFromFieldNames("translateText","fullTranslateText");
        sentenceRepository_es.search(multiMatchQueryBuilder, null, page, highlightBuilder).getContent()
                .forEach(item->{

                    System.out.println(item);

                });



        sentenceRepository_es.deleteAll();
        System.out.println("=============================end===========================================");
        sentenceRepository_es.findAll(page).getContent().forEach(item ->{
            System.out.println(item);
        });


    }


    @Test
    public void test001(){

    }

}
```

