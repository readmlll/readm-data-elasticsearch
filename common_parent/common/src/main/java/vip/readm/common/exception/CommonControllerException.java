package vip.readm.common.exception;


import lombok.*;
import lombok.experimental.Accessors;
import vip.readm.common.dto.RestResult;

/**
 * @Author: Readm
 * @Date: 2019/5/16 3:33
 * @Version 1.0
 */

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper=true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommonControllerException extends RuntimeException {

    private RestResult<String> restResult;


}
