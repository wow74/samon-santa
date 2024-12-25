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

    if(item.getEvaluation() == 1) return item;

    if(item.getEvaluation() == 0) {
      System.out.println("憑依魔法：" + item.getName() + "にサンタが憑依しました。");
      System.out.println("生成魔法：サンタが" + item.getName() + "のためにプレゼントを生成しました。");
      System.out.println("憑依解除：サンタは" + item.getName() + "から出ていきました。");
    } else {
      System.out.println("憑依魔法：" + item.getName() + "にサタンが憑依しました。");
    }
    return item;
  }
}
