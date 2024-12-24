package com.example.demo.domain.model;

import lombok.Data;
import org.apache.logging.log4j.util.Strings;

@Data
public class Yorishiro {
  private int id;
  private String name;
  private int age;
  private int evaluation;
  private String evaluationString;

  public void evaluationToString() {
    evaluationString = switch (evaluation) {
      case 0 -> "良";
      case 1 -> "不良";
      case 2 -> "最悪";
      default -> null;
    };

    if (Strings.isEmpty(evaluationString)) throw new IllegalStateException("評価値が不正です。");
  }
}
