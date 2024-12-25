package com.example.demo.processor;

import com.example.demo.domain.model.Yorishiro;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class EvaluationProcessor implements ItemProcessor<Yorishiro, Yorishiro> {

  @Override
  public Yorishiro process(Yorishiro item) {
    try {
      item.evaluationToString();
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
      return null;
    }
    return item;
  }
}
