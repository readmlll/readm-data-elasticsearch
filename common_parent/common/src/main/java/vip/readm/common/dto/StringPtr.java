package vip.readm.common.dto;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Author: Readm
 * @Date: 2019/5/25 16:56
 * @Version 1.0
 *
 * 模拟string的指针
 */

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class StringPtr {
    public String string;
}
