package com.example.demo.processor;

import com.example.demo.domain.model.Yorishiro;
import com.example.demo.repository.YorishiroRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ExistsCheckProcessor implements ItemProcessor<Yorishiro, Yorishiro> {

  @Autowired
  private YorishiroRepository yorishiroRepository;

  @Override
  public Yorishiro process(Yorishiro item) throws Exception {
    if (yorishiroRepository.exists(item.getId())) {
      System.out.println("Process: id=" + item.getId() + ",msg=登録済みデータのため書き込みをスキップ");
      return null;
    }

    return item;
  }

}
