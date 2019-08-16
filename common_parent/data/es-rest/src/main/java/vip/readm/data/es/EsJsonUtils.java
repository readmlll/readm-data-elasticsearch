package vip.readm.data.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import vip.readm.data.es.config.LocalDateTimeConfig;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Readm
 * @Date: 2019/8/13 22:32
 * @Version 1.0
 */

public class EsJsonUtils {

   public static ObjectMapper objectMapper;

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    static public class EsIndex{

        String indexName;
        String indexType;

        public static ObjectMapper objectMapper;

        List<EsField> fields=new ArrayList<>();


        public String getCreateJson(){

            String json="";
            Map typeObj=new HashMap<>();
            Map propertiesObj=new HashMap<>();

            typeObj.put("properties", propertiesObj);


            fields.forEach(field->{

                Map fieldAttrObj=new HashMap<>();

                fieldAttrObj.put("store",field.isStore());
                fieldAttrObj.put("index", field.isIndex());

                if(!StringUtils.isEmpty( field.getAnalyzer()))
                    fieldAttrObj.put("analyzer", field.getAnalyzer());
                if(!StringUtils.isEmpty( field.getSearch_analyzer()))
                    fieldAttrObj.put("search_analyzer", field.getSearch_analyzer());

                fieldAttrObj.put("type", field.getFieldType());

                propertiesObj.put(field.getFieldName(), fieldAttrObj);

            });


            try {
                json=objectMapper.writeValueAsString(typeObj);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }

    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    static public class EsField{

        String fieldName;
        String fieldType="text";
        boolean isIndex=true;
        boolean isStore=true;
        String analyzer;
        String search_analyzer;
    }


    public static String getEsfieldTypeToString(FieldType fieldType){

        String res="text";

        switch (fieldType){
            case Keyword:
                res="keyword";
                break;
            case Text:
                res="text";
                break;
            case Integer:
                res="integer";
                break;
            case Long:
                res="long";
                break;
            case Date:
                res="date";
                break;
            case Float:
                res="float";
                break;
            case Double:
                res="double";
                break;
            case Boolean:
                res="boolean";
                break;
            case Object:
                res="object";
                break;
            case Attachment:
                res="attachment";
                break;
            case Ip:
                res="ip";
                break;
        }

        return res;
    }

    /**
     * 解析实体对象 解析spring data elasticsearch部分注解
     * @param cl
     * @return
     */
    public  EsIndex parseEsEntity(Class cl){

        EsIndex esIndex=new EsIndex();
        Annotation annotation=cl.getAnnotation(Document.class);
        Document document=null;
        if(annotation instanceof Document){
            document=(Document)annotation;
        }else{
            return esIndex;
        }

        esIndex.setIndexName(document.indexName()).setIndexType(document.type());
        Field[] fileds= cl.getDeclaredFields();
        EsField ESField=null;

        for (Field field:fileds){

            ESField=new EsField();

            org.springframework.data.elasticsearch.annotations.Field  esField=
                    (org.springframework.data.elasticsearch.annotations.Field)
                            field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);

            if(esField==null){
                continue;
            }
            ESField.setFieldName(field.getName()).setIndex(esField.index())
                    .setStore(esField.store()).setFieldType(getEsfieldTypeToString(esField.type()))
            .setAnalyzer(esField.analyzer()).setSearch_analyzer(esField.searchAnalyzer());

            esIndex.getFields().add(ESField);
        }

        return esIndex;
    }




   /* public static void main(String[] args) {

        EsIndex esIndex=parseEsEntity(SentenceEntity_ES.class);
        System.out.println(esIndex.getCreateJson());
    }*/


    //    @Field(type = FieldType.Text,index = true,store = true,searchAnalyzer="ik_smart",analyzer="ik_max_word")
}
