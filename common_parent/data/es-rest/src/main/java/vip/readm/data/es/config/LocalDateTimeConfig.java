package vip.readm.data.es.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import vip.readm.data.es.properties.EsProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: Readm
 * @Date: 2019/8/14 15:35
 * @Version 1.0
 */

@Configuration
@EnableConfigurationProperties(EsProperties.class)//开启属性注入,通过@autowired注入
public class LocalDateTimeConfig {


    private final static String DATE_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";

    public static ObjectMapper objectMapper;
    static {
        LocalDateTimeConfig.objectMapper=new Jackson2ObjectMapperBuilder()
                .findModulesViaServiceLoader(true)
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(
                        DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER)))
                .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(
                        DateTimeFormatter.ofPattern(DATE_TIME_FORMATTER)))
                .build();
    }

}