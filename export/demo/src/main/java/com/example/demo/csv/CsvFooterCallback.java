package com.example.demo.csv;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;

@Component
@StepScope
public class CsvFooterCallback implements FlatFileFooterCallback {

  @Value("#{StepExecution}")
  private StepExecution stepExecution;

  @Override
  public void writeFooter(Writer writer) throws IOException {
    writer.write("合計=" + stepExecution.getWriteCount() + "件");

  }
}
