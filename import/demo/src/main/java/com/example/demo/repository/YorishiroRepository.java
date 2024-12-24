package com.example.demo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class YorishiroRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public boolean exists(int id) {
    final String sql = "select exists (select * from yorishiro where id = ?)";
    return jdbcTemplate.queryForObject(sql, Boolean.class, id);
  }
}
