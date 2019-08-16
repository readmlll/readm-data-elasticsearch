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
