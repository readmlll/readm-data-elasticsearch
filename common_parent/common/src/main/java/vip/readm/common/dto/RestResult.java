package vip.readm.common.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author: Readm
 * @Date: 2019/5/15 21:50
 * @Version 1.0
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Accessors(chain = true)
@EqualsAndHashCode
public class RestResult<T> implements Serializable {
    String msg;
    int code;
    T data;

    public RestResult<T> success(T data){
        return  new RestResult<T>().setMsg("success").setCode(200).setData(data);
    }
    public RestResult<T> error(T data){
        return  new RestResult<T>().setMsg("error").setCode(500).setData(data);
    }
}
