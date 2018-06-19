package com.szmirren.vxApi.core;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.UUID;

import com.szmirren.vxApi.cluster.VxApiClusterManagerFactory;
import com.szmirren.vxApi.core.common.PathUtil;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;

/**
 * VX-API的启动器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiLauncher extends Launcher {
	private final String CLUSTER_TYPE = "NONE";
	/**
	 * 启动VX-API-Gateway
	 */
	public static void start() {
		VxApiLauncher.main(new String[]{"run", "com.szmirren.vxApi.core.VxApiMain"});
	}
	/**
	 * 停止VX-API-Gateway
	 */
	public static void stop() {
		executeCommand("stop", System.getProperty("thisVertxName"));
	}

	public static void main(String[] args) {
		String thisVertxName = UUID.randomUUID().toString() + LocalTime.now().getNano();
		// 设置当前系统Vertx的唯一标识
		System.setProperty("thisVertxName", thisVertxName);
		InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
		System.setProperty("vertx.disableDnsResolver", "true");
		new VxApiLauncher().dispatch(args);
	}

	public static void executeCommand(String cmd, String... args) {
		new VxApiLauncher().execute(cmd, args);
	}

	/**
	 * 设置vert.x配置
	 */
	@Override
	public void beforeStartingVertx(VertxOptions options) {
		try {
			byte[] bytes = Files.readAllBytes(PathUtil.getPath("conf.json"));
			Buffer buff = Buffer.buffer(bytes);
			// 总配置文件
			JsonObject conf = buff.toJsonObject();
			// vert.x配置文件
			JsonObject vertxc = conf.getJsonObject("vertx", getDefaultVertxConfig());
			initVertxConfig(vertxc, options);
			// 集群配置文件
			JsonObject clusterc = conf.getJsonObject("cluster", new JsonObject().put("clusterType", CLUSTER_TYPE));
			if (!CLUSTER_TYPE.equals(clusterc.getString("clusterType"))) {
				ClusterManager cmgr = VxApiClusterManagerFactory.getClusterManager(clusterc.getString("clusterType"),
						clusterc.getJsonObject("clusterConf", getDefaultClusterConfig()));
				options.setClusterManager(cmgr);
				options.setClustered(true);
			}
		} catch (IOException e) {
			throw new FileSystemException(e);
		}
	}
	/**
	 * 初始化vert.x的配置文件<br>
	 * This method copy from the {@link io.vertx.core.VertxOptionsConverter}
	 * fromJson
	 * 
	 * @param json
	 * @param obj
	 */
	public void initVertxConfig(JsonObject json, VertxOptions obj) {

		if (json.getValue("addressResolverOptions") instanceof JsonObject) {
			obj.setAddressResolverOptions(new io.vertx.core.dns.AddressResolverOptions((JsonObject) json.getValue("addressResolverOptions")));
		}
		if (json.getValue("blockedThreadCheckInterval") instanceof Number) {
			obj.setBlockedThreadCheckInterval(((Number) json.getValue("blockedThreadCheckInterval")).longValue());
		}
		if (json.getValue("clusterHost") instanceof String) {
			obj.setClusterHost((String) json.getValue("clusterHost"));
		}
		if (json.getValue("clusterPingInterval") instanceof Number) {
			obj.setClusterPingInterval(((Number) json.getValue("clusterPingInterval")).longValue());
		}
		if (json.getValue("clusterPingReplyInterval") instanceof Number) {
			obj.setClusterPingReplyInterval(((Number) json.getValue("clusterPingReplyInterval")).longValue());
		}
		if (json.getValue("clusterPort") instanceof Number) {
			obj.setClusterPort(((Number) json.getValue("clusterPort")).intValue());
		}
		if (json.getValue("clusterPublicHost") instanceof String) {
			obj.setClusterPublicHost((String) json.getValue("clusterPublicHost"));
		}
		if (json.getValue("clusterPublicPort") instanceof Number) {
			obj.setClusterPublicPort(((Number) json.getValue("clusterPublicPort")).intValue());
		}
		if (json.getValue("clustered") instanceof Boolean) {
			obj.setClustered((Boolean) json.getValue("clustered"));
		}
		if (json.getValue("eventBusOptions") instanceof JsonObject) {
			obj.setEventBusOptions(new io.vertx.core.eventbus.EventBusOptions((JsonObject) json.getValue("eventBusOptions")));
		}
		if (json.getValue("eventLoopPoolSize") instanceof Number) {
			obj.setEventLoopPoolSize(((Number) json.getValue("eventLoopPoolSize")).intValue());
		}
		if (json.getValue("fileResolverCachingEnabled") instanceof Boolean) {
			obj.setFileResolverCachingEnabled((Boolean) json.getValue("fileResolverCachingEnabled"));
		}
		if (json.getValue("haEnabled") instanceof Boolean) {
			obj.setHAEnabled((Boolean) json.getValue("haEnabled"));
		}
		if (json.getValue("haGroup") instanceof String) {
			obj.setHAGroup((String) json.getValue("haGroup"));
		}
		if (json.getValue("internalBlockingPoolSize") instanceof Number) {
			obj.setInternalBlockingPoolSize(((Number) json.getValue("internalBlockingPoolSize")).intValue());
		}
		if (json.getValue("maxEventLoopExecuteTime") instanceof Number) {
			obj.setMaxEventLoopExecuteTime(((Number) json.getValue("maxEventLoopExecuteTime")).longValue());
		}
		if (json.getValue("maxWorkerExecuteTime") instanceof Number) {
			obj.setMaxWorkerExecuteTime(((Number) json.getValue("maxWorkerExecuteTime")).longValue());
		}
		if (json.getValue("metricsOptions") instanceof JsonObject) {
			obj.setMetricsOptions(new io.vertx.core.metrics.MetricsOptions((JsonObject) json.getValue("metricsOptions")));
		}
		if (json.getValue("preferNativeTransport") instanceof Boolean) {
			obj.setPreferNativeTransport((Boolean) json.getValue("preferNativeTransport"));
		}
		if (json.getValue("quorumSize") instanceof Number) {
			obj.setQuorumSize(((Number) json.getValue("quorumSize")).intValue());
		}
		if (json.getValue("warningExceptionTime") instanceof Number) {
			obj.setWarningExceptionTime(((Number) json.getValue("warningExceptionTime")).longValue());
		}
		if (json.getValue("workerPoolSize") instanceof Number) {
			obj.setWorkerPoolSize(((Number) json.getValue("workerPoolSize")).intValue());
		}

	}

	/**
	 * 获得默认的vert.x配置信息
	 * 
	 * @return
	 */
	public JsonObject getDefaultVertxConfig() {
		return new JsonObject().put("preferNativeTransport", true);
	}

	/**
	 * 获得默认的集群配置文件
	 * 
	 * @return
	 */
	public JsonObject getDefaultClusterConfig() {
		JsonObject json = new JsonObject();
		json.put("zookeeperHosts", "127.0.0.1");
		json.put("sessionTimeout", 20000);
		json.put("connectTimeout", 3000);
		json.put("rootPath", "io.vertx");
		json.put("vxApiConfPath", "/io.vertx/vx.api.gateway/conf");
		JsonObject retry = new JsonObject();
		retry.put("initialSleepTime", 100);
		retry.put("intervalTimes", 10000);
		retry.put("maxTimes", 5);
		json.put("retry", retry);
		return json;
	}

	/**
	 * 获得默认的数据库配置
	 * 
	 * @return
	 */
	public JsonObject getDefaultDataConfig() {
		return new JsonObject().put("url", "jdbc:sqlite:configDB.db").put("driver_class", "org.sqlite.JDBC");
	}

	/**
	 * 获得默认的客户端配置
	 * 
	 * @return
	 */
	public JsonObject getDefaultClientConfig() {
		return new JsonObject().put("clientPort", 5256);
	}

}
