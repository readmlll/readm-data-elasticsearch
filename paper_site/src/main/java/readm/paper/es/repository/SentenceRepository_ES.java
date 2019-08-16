package readm.paper.es.repository;

import lombok.NoArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import readm.paper.es.pojo.SentenceEntity_ES;
import vip.readm.data.es.repository.EsRepository;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @Author: Readm
 * @Date: 2019/8/14 4:29
 * @Version 1.0
 */


@NoArgsConstructor
@Component
public class SentenceRepository_ES extends EsRepository<SentenceEntity_ES,String> {



}
