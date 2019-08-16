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
@Document(indexName = "idx_test_paper_test_002", type = "sentence")
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
