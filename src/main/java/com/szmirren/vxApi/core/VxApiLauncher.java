package com.szmirren.vxApi.core;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.UUID;

import com.szmirren.vxApi.cluster.VxApiClusterManagerFactory;
import com.szmirren.vxApi.core.common.PathUtil;

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

	public static void main(String[] args) {
		String thisVertxName = UUID.randomUUID().toString() + LocalTime.now().getNano();
		// 设置当前系统Vertx的唯一标识
		System.setProperty("thisVertxName", thisVertxName);
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
			options = new VertxOptions(vertxc);
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
