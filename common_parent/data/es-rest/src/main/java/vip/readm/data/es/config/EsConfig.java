package vip.readm.data.es.config;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.regexp.internal.RE;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import vip.readm.common.utils.ClassUtils;
import vip.readm.data.es.EsJsonUtils;
import vip.readm.data.es.EsRestClient;
import vip.readm.data.es.properties.EsProperties;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Author: Readm
 * @Date: 2019/8/14 4:32
 * @Version 1.0
 */

@NoArgsConstructor
@Data
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
@Configuration
@EnableConfigurationProperties(EsProperties.class)//开启属性注入,通过@autowired注入
public class EsConfig {

    static {
        System.out.println("EsConfig static init");
    }


    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DefaultListableBeanFactory beanFactory;

    public static  Log log= LogFactory.getLog(EsConfig.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    EsProperties esProperties;



    public static RestHighLevelClient restHighLevelClient=null;
    public static RestClient restClient=null;
    public static RestClientBuilder builder=null;

    @PostConstruct
    void init() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public  RestClientBuilder getEsClientBuild(){


        if(builder==null){
            synchronized (this){
                if(builder==null){

                    RestClientBuilder builder = RestClient.builder(
                            new HttpHost(esProperties.host, esProperties.port, "http"));


                    if(esProperties.enableLogin ){
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(AuthScope.ANY,
                                new UsernamePasswordCredentials(esProperties.username, esProperties.passwd));

                        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                            @Override
                            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                                return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                            }
                        });
                    }

                    builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                        @Override
                        public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                            requestConfigBuilder.setConnectTimeout(esProperties.commonTimeOut);
                            requestConfigBuilder.setSocketTimeout(esProperties.commonTimeOut);
                            requestConfigBuilder.setConnectionRequestTimeout(esProperties.commonTimeOut);
                            return requestConfigBuilder;
                        }
                    }).setMaxRetryTimeoutMillis(esProperties.commonTimeOut);


                    EsConfig.builder=builder;

                }
            }
        }

        return builder;
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() throws Exception {
        RestHighLevelClient restHighLevelClient=new RestHighLevelClient(getEsClientBuild());
        EsConfig.restHighLevelClient=restHighLevelClient;
        return restHighLevelClient;
    }

    @Bean
    RestClient restClient(RestHighLevelClient restHighLevelClient){
        RestClient restClient=restHighLevelClient.getLowLevelClient();
        EsConfig.restClient=restClient;
        return  restClient;
    }

    @Bean
    @ConditionalOnBean({ObjectMapper.class})
    EsJsonUtils esJsonUtils(){
        EsJsonUtils esJsonUtils=new EsJsonUtils();
        EsJsonUtils.objectMapper=objectMapper;
        EsJsonUtils.EsIndex.objectMapper=objectMapper;
        return esJsonUtils;
    }

    @PostConstruct
    public void scanRepository(){

        /**
         *
         * 扫描客户定义的仓库对象 根据仓库对象包名
         *
         * 注册关系
         * 所有的 仓库对象继承EsRepository
         * EsRepository继承于  基于RestClient和RestHighLevelClient的 自定义的EsRest客户端 封装前二者操作
         *
         * 于是：创建用户仓库时 创建了 EsRepository 创建了 EsRest
         *
         * EsRest内含有自动注入的属性 所以在 config对象中 PostConstruct注解标注
         */

        String repository_path = esProperties.repository_path;
        List<Class<?>> list= ClassUtils.getClasses(repository_path);
        list.forEach(cl->{
            try {
                defineBean(cl,beanFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public  static void defineBean(Class cl,DefaultListableBeanFactory beanFactory){
        String name=cl.getSimpleName();
        String cp=(""+name.charAt(0)).toLowerCase();
        String beanName=cp+name.substring(1, name.length());

        AnnotatedGenericBeanDefinition annotatedGenericBeanDefinition=new AnnotatedGenericBeanDefinition(cl);
        beanFactory.registerBeanDefinition(beanName,annotatedGenericBeanDefinition);

        log.info("对象注册:"+name+" ,注册成功,bean名字:"+beanName);
    }


}
