package com.example.demo.listener;

import com.example.demo.domain.model.Yorishiro;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class YorishiroSkipListener implements SkipListener<Yorishiro, Yorishiro> {

  @Override
  public void onSkipInRead(Throwable t) {
    System.out.println("SkipRead: error=" + t.getMessage());
  }

  @Override
  public void onSkipInProcess(Yorishiro item, Throwable t) {
    System.out.println("SkipProcess: item=" + item + ",error=" + t.getMessage());
  }

  @Override
  public void onSkipInWrite(Yorishiro item, Throwable t) {
    System.out.println("SkipWrite: item=" + item + ",error=" + t.getMessage());
  }
}
