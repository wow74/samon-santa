package com.example.demo.domain.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;

@Data
public class Yorishiro {

  @NotNull
  private Integer id;

  @NotBlank
  private String name;

  @Min(0)
  @Max(100)
  private int age;

  // evaluationはbatch制御のスキップや分岐でチェックするため、ここではチェックをしない
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
