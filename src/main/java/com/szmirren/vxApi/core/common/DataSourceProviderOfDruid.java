package com.szmirren.vxApi.core.common;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.pool.DruidDataSource;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.spi.DataSourceProvider;

public class DataSourceProviderOfDruid extends DruidDataSource implements DataSourceProvider {
	private static final long serialVersionUID = 381971380983136762L;

	public int maximumPoolSize(DataSource dataSource, JsonObject config) throws SQLException {
		return config.getInteger("max_pool_size", 15).intValue();
	}

	public DataSource getDataSource(JsonObject config) throws SQLException {
		Log4j2Filter log4j2 = new Log4j2Filter();
		DruidDataSource ds = new DruidDataSource();
		ds.getProxyFilters().add(log4j2);
		ds.setUrl(config.getString("url"));
		ds.setDriverClassName(config.getString("driver_class"));
		ds.setUsername(config.getString("user"));
		ds.setPassword(config.getString("password"));
		ds.setMaxActive(config.getInteger("max_pool_size", 15));
		ds.setMinIdle(config.getInteger("min_pool_size", 1));
		ds.setInitialSize(config.getInteger("initial_pool_size", 1));
		ds.setMinEvictableIdleTimeMillis(config.getLong("max_idle_time", 60000L));
		ds.setValidationQuery(config.getValue("validation_query", "select 1").toString());
		return ds;
	}

	public void close(DataSource dataSource) throws SQLException {
		if ((dataSource instanceof DataSourceProviderOfDruid))
			((DataSourceProviderOfDruid) dataSource).close();
	}
}