package readm.paper.controller.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import vip.readm.common.exception.CommonControllerException;

/**
 * @Author: Readm
 * @Date: 2019/5/15 23:24
 * @Version 1.0
 */

@ControllerAdvice
public class ExceptionHandle {


    Log log= LogFactory.getLog(ExceptionHandle.class);
    /**
     * 全局异常捕捉处理
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Object errorHandler(Exception ex) {

        if(ex instanceof CommonControllerException){
            CommonControllerException commonControllerException=(CommonControllerException)ex;
            log.error(commonControllerException.getRestResult().getMsg());
            return commonControllerException.getRestResult();
        }


        CommonControllerException commonControllerException=new CommonControllerException();
        commonControllerException.getRestResult().error("error,编码预料外异常:"+ex.getMessage())
        .setCode(505).setData(null);
        log.error("编码预料外异常："+ex.getMessage()+"\n");
        return commonControllerException;
    }

}
