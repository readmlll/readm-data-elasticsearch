package vip.readm.common.dto;

/**
 * @Author: Readm
 * @Date: 2019/5/16 15:28
 * @Version 1.0
 */

import lombok.*;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用的数据转换对象 用于一部分需要多表查询的数据组合
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class CommonDtoWarp<T> {
    private T obj;
    private Map<String, Object> dataMap=new HashMap<>();
}
