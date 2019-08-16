package vip.readm.data.es.properties;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @Author: Readm
 * @Date: 2019/8/15 2:40
 * @Version 1.0
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@ConditionalOnMissingBean(name = "esProperties")
@Component
@ConfigurationProperties(prefix = "readm.data.es")
public class EsProperties {


    public long dataCenterId=1;  //数据中心
    public long machineId=1;  //机器id

    public String username;
    public String passwd;
    public String host;
    public Integer port;

    public Integer number_of_replicas=3;
    public Integer number_of_shards=3;

    public Integer commonTimeOut=5*60*1000; //统一的超时时间设置

    public String repository_path;  //扫描的仓库包名

    public boolean enableLogin=false; //是否启用 用户名密码登陆


}
