package vip.readm.data.es.pojo;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @Author: Readm
 * @Date: 2019/8/14 17:44
 * @Version 1.0
 */

@Data
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EsEntity {

    @Id
    @Field(type = FieldType.Keyword,index = true,store = true)
    private String id;


}
