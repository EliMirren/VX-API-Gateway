package com.szmirren.vxApi.core.verticle;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiDATAStoreConstant;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.entity.VxApiTrackInfos;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 一些系统参数
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class SysVerticle extends AbstractVerticle {
	private Logger LOG = Logger.getLogger(this.getClass());
	/**
	 * VX-API的启动时间
	 */
	private LocalDateTime startVxApiTime = LocalDateTime.now();
	/**
	 * 线程数量
	 */
	private int availableProcessors = 0;
	/**
	 * JVM总的内存量
	 */
	private long totalMemory = 0;
	/**
	 * JVM的空闲内存量
	 */
	private long freeMemory = 0;
	/**
	 * JVM最大内存量
	 */
	private long maxMemory = 0;
	/**
	 * 应用的数量
	 */
	private int appCount = 0;
	/**
	 * 异常次数
	 */
	private int errorCount = 0;
	/**
	 * 存储API的监控记录信息
	 */
	private Map<String, Deque<JsonObject>> trackSucceededMap = new HashMap<>();
	/**
	 * 存储API请求失败的数数
	 */
	private Map<String, Long> requstFailedCount = new HashMap<>();
	/**
	 * 存储API请求的数量
	 */
	private Map<String, Long> requstCount = new HashMap<>();

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_GET_INFO, this::getSysInfo);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_PLUS_APP, this::plusAPP);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_MINUS_APP, this::minusAPP);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_MINUS_APP, this::PlusError);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, this::plusTrackInfos);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_GET_TRACK_INFO, this::getTrackInfo);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_BLACK_IP_FIND, this::findIpList);
		vertx.eventBus().consumer(VxApiEventBusAddressConstant.SYSTEM_BLACK_IP_REPLACE, this::replaceIpList);
		startFuture.complete();
	}

	/**
	 * 查看系统基本信息
	 * 
	 * @param msg
	 */
	public void getSysInfo(Message<JsonObject> msg) {
		availableProcessors = Runtime.getRuntime().availableProcessors();
		totalMemory = Runtime.getRuntime().totalMemory();
		freeMemory = Runtime.getRuntime().freeMemory();
		maxMemory = Runtime.getRuntime().maxMemory();
		JsonObject result = new JsonObject();
		result.put("availableProcessors", availableProcessors);
		result.put("totalMemory", totalMemory / (1024 * 1024));
		result.put("freeMemory", freeMemory / (1024 * 1024));
		result.put("maxMemory", maxMemory / (1024 * 1024));
		Duration duration = Duration.between(startVxApiTime, LocalDateTime.now());
		result.put("vxApiRunTime", StrUtil.millisToDateTime(duration.toMillis(), "$dD $hH $mMin $sS"));
		result.put("appCount", appCount);
		result.put("errorCount", errorCount);
		vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.FIND_BLACKLIST, null, res -> {
			if (res.succeeded()) {
				JsonObject body = res.result().body();
				if (body.getValue(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME) instanceof JsonArray) {
					result.put("content", body.getJsonArray(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME));
				} else if (body.getValue(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME) instanceof String) {
					result.put("content", new JsonArray(body.getString(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME)));
				} else {
					result.put("content", new JsonArray());
				}
				msg.reply(result);
			} else {
				msg.fail(500, res.cause().getMessage());
			}
		});
	}

	/**
	 * app数量+1
	 * 
	 * @param msg
	 */
	public void plusAPP(Message<JsonObject> msg) {
		appCount++;
	}

	/**
	 * app数量-1
	 * 
	 * @param msg
	 */
	public void minusAPP(Message<JsonObject> msg) {
		if (appCount > 0) {
			appCount--;
		}
	}

	/**
	 * 添加异常的数量
	 * 
	 * @param msg
	 */
	public void PlusError(Message<JsonObject> msg) {
		errorCount++;
		if (msg.body() != null) {
			VxApiTrackInfos infos = VxApiTrackInfos.fromJson(msg.body());
			LOG.error(MessageFormat.format("应用:{0} , API:{1} ,在执行的过程中发生了异常:{2} ,堆栈信息{3}", infos.getAppName(),
					infos.getApiName(), infos.getErrMsg(), infos.getErrStackTrace()));
		}
	}

	/**
	 * 添加API追踪信息
	 * 
	 * @param msg
	 */
	public void plusTrackInfos(Message<JsonObject> msg) {
		if (msg.body() != null) {
			VxApiTrackInfos infos = VxApiTrackInfos.fromJson(msg.body());
			// map的key
			String key = infos.getAppName() + "-" + infos.getApiName();
			// 记录API相关信息
			if (!infos.isSuccessful()) {
				// 记录异常
				errorCount++;
				if (requstFailedCount.get(key) == null) {
					requstFailedCount.put(key, 0L);
				}
				requstFailedCount.put(key, requstFailedCount.get(key) + 1);
				LOG.error(MessageFormat.format("应用:{0} , API:{1} ,在执行的过程中发生了异常:{2} ,堆栈信息{3}", infos.getAppName(),
						infos.getApiName(), infos.getErrMsg(), infos.getErrStackTrace()));
			} else {
				JsonObject json = new JsonObject();
				Duration proc = Duration.between(infos.getStartTime(), infos.getEndTime());
				json.put("time", infos.getStartTime());
				json.put("overallTime", proc.toMillis());
				Duration reqs = Duration.between(infos.getRequestTime(), infos.getResponseTime());
				json.put("requestTime", reqs.toMillis());
				json.put("requestBodyLen", infos.getRequestBufferLen());
				json.put("responseBodyLen", infos.getResponseBufferLen());
				if (trackSucceededMap.get(key) == null) {
					trackSucceededMap.put(key, new LinkedList<>());
				} else {
					if (trackSucceededMap.get(key).size() > 100) {
						trackSucceededMap.get(key).pollFirst();
					}
				}
				trackSucceededMap.get(key).add(json);
			}
			// 添加请求数量统计
			if (requstCount.get(key) == null) {
				requstCount.put(key, 0L);
			}
			requstCount.put(key, requstCount.get(key) + 1);
		}
	}

	/**
	 * 查看API运行信息
	 * 
	 * @param msg
	 */
	public void getTrackInfo(Message<JsonObject> msg) {
		String appName = msg.body().getString("appName");
		String apiName = msg.body().getString("apiName");
		String key = appName + "-" + apiName;
		JsonObject result = new JsonObject();
		result.put("rc", requstCount.get(key) == null ? 0 : requstCount.get(key));
		result.put("ec", requstFailedCount.get(key) == null ? 0 : requstFailedCount.get(key));
		result.put("track", trackSucceededMap.get(key) == null ? new JsonObject() : trackSucceededMap.get(key));
		msg.reply(result);
	}

	/**
	 * 查看黑名单ip地址
	 * 
	 * @param msg
	 */
	public void findIpList(Message<JsonObject> msg) {
		vertx.eventBus().<JsonObject>send(VxApiEventBusAddressConstant.FIND_BLACKLIST, null, res -> {
			if (res.succeeded()) {
				JsonObject body = res.result().body();
				if (body.getValue(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME) instanceof JsonArray) {
					msg.reply(body.getJsonArray(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME));
				} else if (body.getValue(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME) instanceof String) {
					msg.reply(new JsonArray(body.getString(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME)));
				} else {
					msg.reply(new JsonArray());
				}
			} else {
				msg.fail(500, res.cause().getMessage());
			}
		});
	}

	/**
	 * 更新黑名单ip地址
	 * 
	 * @param msg
	 */
	public void replaceIpList(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(1400, "参数为空或者缺少参数");
		} else {
			if (msg.body().getValue(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME) instanceof JsonArray) {
				JsonArray array = msg.body().getJsonArray(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME);
				JsonObject body = new JsonObject().put(VxApiDATAStoreConstant.BLACKLIST_ID_NAME, "blacklist")
						.put(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME, array);
				vertx.eventBus().<Integer>send(VxApiEventBusAddressConstant.REPLACE_BLACKLIST, body, res -> {
					if (res.succeeded()) {
						msg.reply(res.result().body());
						// 广播更新自己ip地址
						vertx.eventBus().publish(VxApiEventBusAddressConstant.SYSTEM_PUBLISH_BLACK_IP_LIST, array);
					} else {
						msg.fail(500, res.cause().getMessage());
					}
				});
			} else {
				msg.fail(1405, "参数为空或者缺少参数");
			}
		}
	}

}
