package com.example.demo.config;

import com.example.demo.domain.model.Yorishiro;
import com.example.demo.listener.ProcessListener;
import com.example.demo.listener.ReadListener;
import com.example.demo.listener.WriteListener;
import com.example.demo.listener.YorishiroSkipListener;
import com.example.demo.processor.EvaluationProcessor;
import com.example.demo.processor.ExistsCheckProcessor;
import com.example.demo.tasklet.DummyTasklet;
import com.example.demo.tasklet.XmasTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
public class BatchConfig {

  @Value("${csv.path}")
  private String csvPath;

  @Autowired
  private ReadListener readListener;

  @Autowired
  private ProcessListener processListener;

  @Autowired
  private WriteListener writeListener;

  @Autowired
  private YorishiroSkipListener yorishiroSkipListener;

  @Autowired
  private EvaluationProcessor evaluationProcessor;

  @Autowired
  private ExistsCheckProcessor existsCheckProcessor;

  @Autowired
  private XmasTasklet xmasTasklet;

  @Autowired
  private DummyTasklet dummyTasklet;

  @Bean
  @ConfigurationProperties("spring.datasource.h2")
  DataSourceProperties h2Properties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.datasource.mysql")
  DataSourceProperties mysqlProperties() {
    return new DataSourceProperties();
  }

  @BatchDataSource
  @Bean
  DataSource h2DataSource() {
    return h2Properties()
            .initializeDataSourceBuilder()
            .build();
  }

  @Primary
  @Bean
  DataSource mysqlDataSource() {
    return mysqlProperties()
            .initializeDataSourceBuilder()
            .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<Yorishiro> csvReader() {
    final String[] title = new String[]{"id", "name", "age", "evaluation"};
    return new FlatFileItemReaderBuilder<Yorishiro>()
            .name("YorishiroCsvReader")
            .resource(new ClassPathResource(csvPath))
            .linesToSkip(1)
            .encoding(StandardCharsets.UTF_8.name())
            .delimited()
            .names(title)
            .fieldSetMapper(new BeanWrapperFieldSetMapper<Yorishiro>() {
              {
                // 依り代クラスにcsvデータをバインドする
                // int型にnullを入れようとするとバインドエラーになる
                // nullのバリデーションチェックをするならInteger型にする
                setTargetType(Yorishiro.class);
              }
            })
            .build();
  }

  @Bean
  @StepScope
  public JdbcBatchItemWriter<Yorishiro> jdbcWriter() {
    final String sql = "insert into yorishiro (id, name, age, evaluation) " +
            "values(:id, :name, :age, :evaluation)";
    return new JdbcBatchItemWriterBuilder<Yorishiro>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql(sql)
            .dataSource(mysqlDataSource())
            .build();
  }

  @Bean
  @StepScope
  public ItemProcessor<Yorishiro, Yorishiro> compositeProcessor() {
    CompositeItemProcessor<Yorishiro, Yorishiro> compositeItemProcessor = new CompositeItemProcessor<>();
    compositeItemProcessor.setDelegates(Arrays.asList(validationProcessor(), existsCheckProcessor, evaluationProcessor));
    return compositeItemProcessor;
  }

  @Bean
  @StepScope
  public BeanValidatingItemProcessor<Yorishiro> validationProcessor() {
    BeanValidatingItemProcessor<Yorishiro> validatingItemProcessor = new BeanValidatingItemProcessor<>();
    validatingItemProcessor.setFilter(true);
    return validatingItemProcessor;
  }

  @Bean
  public Step importStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("ImportStep", jobRepository)
            .<Yorishiro, Yorishiro>chunk(2, transactionManager)
            .reader(csvReader()).listener(readListener)
            .processor(compositeProcessor()).listener(processListener)
            .writer(jdbcWriter()).listener(writeListener)
            .faultTolerant()
            .skipLimit(Integer.MAX_VALUE)
            .skip(RuntimeException.class)
            .listener(yorishiroSkipListener)
            .build();
  }

  @Bean
  public Step xmasStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("XmasStep", jobRepository)
            .tasklet(xmasTasklet, transactionManager)
            .build();
  }

  @Bean
  public Step dummyStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("DummyStep", jobRepository)
            .tasklet(dummyTasklet, transactionManager)
            .build();
  }

  @Bean
  public Flow importFlow(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new FlowBuilder<SimpleFlow>("ImportFlow")
            .start(importStep(jobRepository, transactionManager))
            .build();
  }

  @Bean
  public Flow xmasFlow(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new FlowBuilder<SimpleFlow>("XmasFlow")
            .start(xmasStep(jobRepository, transactionManager))
            .build();
  }

  @Bean
  public Flow dummyFlow(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new FlowBuilder<SimpleFlow>("DummyFlow")
            .start(dummyStep(jobRepository, transactionManager))
            .build();
  }

  @Bean
  public TaskExecutor asyncTaskExecutor() {
    return new SimpleAsyncTaskExecutor("concurrent_");
  }

  @Bean
  public Flow splitFlow(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new FlowBuilder<SimpleFlow>("SplitFlow")
            .split(asyncTaskExecutor())
            .add(xmasFlow(jobRepository, transactionManager), dummyFlow(jobRepository, transactionManager))
            .build();
  }

  @Bean
  public Job importJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws UnsupportedEncodingException {
    System.setOut(new PrintStream(System.out, true, "UTF-8"));
    return new JobBuilder("ImportJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(importFlow(jobRepository, transactionManager))
            .next(splitFlow(jobRepository, transactionManager))
            .build()
            .build();
  }
}
