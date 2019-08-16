package readm.paper.properties;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: Readm
 * @Date: 2019/8/14 10:55
 * @Version 1.0
 */



@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@Component
@ConfigurationProperties(prefix = "readm.main")
public class MainProperties {

    public boolean debug=true;


}
