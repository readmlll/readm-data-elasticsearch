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


    @Autowired
    EsRestClient esRestClient;


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

        //sentenceRepository_es.saveAll(sentencs);
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


        //sentenceRepository_es.deleteAll();


        sentenceRepository_es.findAll(page).getContent().forEach(item ->{
            System.out.println(item);
        });


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

    }

}
