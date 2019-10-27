package de.headshotharp.web;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MySql {
	@Autowired
	private Config config;

	@Bean
	public JdbcTemplate getJdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	public DataSource getDataSource() {
		return DataSourceBuilder.create().url(createUrl()).username(config.getMysql().getUsername())
				.password(config.getMysql().getPassword()).build();
	}

	private String createUrl() {
		return "jdbc:mysql://" + config.getMysql().getHost() + ":" + config.getMysql().getPort() + "/"
				+ config.getMysql().getDatabase()
				+ "?autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC";
	}
}
