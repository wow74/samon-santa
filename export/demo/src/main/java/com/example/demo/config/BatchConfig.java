package com.example.demo.config;

import com.example.demo.csv.CsvFooterCallback;
import com.example.demo.csv.CsvHeaderCallback;
import com.example.demo.domain.model.Yorishiro;
import com.example.demo.listener.ProcessListener;
import com.example.demo.listener.ReadListener;
import com.example.demo.listener.WriteListener;
import com.example.demo.processor.EvaluationProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class BatchConfig {

  @Value("${csv.path}")
  private String csvPath;

  @Autowired
  private CsvHeaderCallback csvHeaderCallback;

  @Autowired
  private CsvFooterCallback csvFooterCallback;

  @Autowired
  private ReadListener readListener;

  @Autowired
  private ProcessListener processListener;

  @Autowired
  private WriteListener writeListener;

  @Autowired
  private EvaluationProcessor evaluationProcessor;

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
  public FlatFileItemWriter<Yorishiro> csvWriter() {
    WritableResource resource = new FileSystemResource(csvPath);
    DelimitedLineAggregator<Yorishiro> aggregator = new DelimitedLineAggregator<>();
    aggregator.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);

    BeanWrapperFieldExtractor<Yorishiro> extractor = new BeanWrapperFieldExtractor<>();
    extractor.setNames(new String[]{"id", "name", "age", "evaluationString"});
    aggregator.setFieldExtractor(extractor);

    return new FlatFileItemWriterBuilder<Yorishiro>()
            .name("evaluationCsvWriter")
            .resource(resource)
            .append(false)
            .lineAggregator(aggregator)
            .headerCallback(csvHeaderCallback)
            .footerCallback(csvFooterCallback)
            .encoding(StandardCharsets.UTF_8.name())
            .build();
  }

  @Bean
  public SqlPagingQueryProviderFactoryBean queryProvider() {
    SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
    provider.setDataSource(mysqlDataSource());
    provider.setSelectClause("select id, name, age, evaluation");
    provider.setFromClause("from yorishiro");
    provider.setSortKey("id");
    return provider;
  }

  @Bean
  @StepScope
  public JdbcPagingItemReader<Yorishiro> jdbcPagingReader() throws Exception {
    RowMapper<Yorishiro> rowMapper = new BeanPropertyRowMapper<>(Yorishiro.class);
    return new JdbcPagingItemReaderBuilder<Yorishiro>()
            .name("jdbcPagingItemReader")
            .dataSource(mysqlDataSource())
            .queryProvider(queryProvider().getObject())
            .rowMapper(rowMapper)
            .pageSize(5)
            .build();
  }

  @Bean
  public TaskExecutor asyncTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(3);
    executor.setThreadNamePrefix("task_");
    executor.initialize();
    return executor;
  }

  @Bean
  public Step exportStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
    return new StepBuilder("ExportStep", jobRepository)
            .<Yorishiro, Yorishiro>chunk(2, transactionManager)
            .reader(jdbcPagingReader()).listener(readListener)
            .processor(evaluationProcessor).listener(processListener)
            .writer(csvWriter()).listener(writeListener)
            .taskExecutor(asyncTaskExecutor())
            .build();
  }

  @Bean
  public Job exportJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, ThreadPoolTaskExecutor executor) throws Exception {
    System.setOut(new PrintStream(System.out, true, "UTF-8"));
    return new JobBuilder("ExportJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(exportStep(jobRepository, transactionManager))
            .listener(new JobExecutionListener() {
              @Override
              public void afterJob(JobExecution jobExecution) {
                executor.shutdown();
              }
            })
            .build();
  }
}
