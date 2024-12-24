package com.example.demo.listener;

import com.example.demo.domain.model.Yorishiro;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.stereotype.Component;

@Component
public class ReadListener implements ItemReadListener<Yorishiro> {

  @Override
  public void beforeRead() {
    System.out.println("BeforeRead:");
  }

  @Override
  public void afterRead(Yorishiro item) {
    System.out.println("AfterRead: item=" + item);
  }

  @Override
  public void onReadError(Exception e) {
    System.out.println("ReadError: error=" + e.getMessage());
  }
}
