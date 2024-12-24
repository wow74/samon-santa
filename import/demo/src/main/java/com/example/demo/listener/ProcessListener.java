package com.example.demo.listener;

import com.example.demo.domain.model.Yorishiro;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
public class ProcessListener implements ItemProcessListener<Yorishiro, Yorishiro> {

  @Override
  public void beforeProcess(Yorishiro item) {
    System.out.println("BeforeProcess: item=" + item);
  }

  @Override
  public void afterProcess(Yorishiro item, Yorishiro result) {
    System.out.println("AfterProcess: item=" + item + ",result=" + result);
  }

  @Override
  public void onProcessError(Yorishiro item, Exception e) {
    System.out.println("ProcessError: item=" + item + ",error=" + e.getMessage());
  }
}
