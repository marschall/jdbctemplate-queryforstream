package com.github.marschall.jdbctemplate.queryforstream;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static void main(String[] args) {
    JdbcDataSource h2Datasource = new JdbcDataSource();
    h2Datasource.setURL("jdbc:h2:mem:");

    DataSource proxyDatasource = ProxyDataSourceBuilder
            .create(h2Datasource)
            .afterMethod(callback -> {
              Method method = callback.getMethod();
              if (method.getName().equals("close")) {
                LOG.info(method.toString());
              }
            })
            .build();

    LOG.info("queryForObject");

    JdbcOperations jdbcTemplate = new JdbcTemplate(proxyDatasource);
    jdbcTemplate.queryForObject("SELECT 1", Integer.class);

    LOG.info("queryForStream");

    jdbcTemplate.queryForStream("SELECT 1", (rs, i) -> rs.getInt(1)).findAny();

    LOG.info("queryForStream closed");

    try (Stream<Integer> stream = jdbcTemplate.queryForStream("SELECT 1", (rs, i) -> rs.getInt(1))) {
      stream.findAny();
    }
  }

}
