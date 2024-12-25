package com.example.demo.listener;

import com.example.demo.domain.model.Yorishiro;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Component
public class WriteListener implements ItemWriteListener<Yorishiro> {

  @Override
  public void beforeWrite(Chunk<? extends Yorishiro> items) {
    System.out.println("BeforeWrite: count=" + items.size());
  }

  @Override
  public void afterWrite(Chunk<? extends Yorishiro> items) {
    System.out.println("AfterWrite: count=" + items.size());
  }

  @Override
  public void onWriteError(Exception e, Chunk<? extends Yorishiro> items) {
    System.out.println("WriteError: count=" + items.size() + ",error=" + e.getMessage());
  }
}
