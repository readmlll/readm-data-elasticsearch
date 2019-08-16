package vip.readm.common.utils;

import vip.readm.common.dto.RestResult;

/**
 * @Author: Readm
 * @Date: 2019/5/16 3:53
 * @Version 1.0
 */

public class RestResultUtils {

    public static RestResult<Object> success(Object data){

        return new RestResult<>().setMsg("success").setCode(200).setData(data);
    }


    public static RestResult<Object> error(Object data){

        return new RestResult<>().setMsg("error").setCode(500).setData(data);
    }

}
