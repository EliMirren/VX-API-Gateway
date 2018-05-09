package com.szmirren.vxApi.core.verticle;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final Logger LOG = LogManager.getLogger(SysVerticle.class);

	/** VX-API的启动时间 */
	private LocalDateTime startVxApiTime = LocalDateTime.now();
	/** 线程数量 */
	private int availableProcessors = 0;
	/** JVM总的内存量 */
	private long totalMemory = 0;
	/** JVM的空闲内存量 */
	private long freeMemory = 0;
	/** JVM最大内存量 */
	private long maxMemory = 0;
	/** 异常次数 */
	private int errorCount = 0;
	/** 请求到达VX的次数 */
	private long requestVxApiCount = 0;
	/** 请求到达核心处理器(HTTP/HTTPS)的次数 */
	private long requestHttpApiCount = 0;
	/** 核心处理器(HTTP/HTTPS)当前正在处理API的数量 */
	private long currentHttpApiProcessingCount = 0;

	/** 存储API的监控记录信息 */
	private Map<String, Deque<JsonObject>> trackSucceededMap = new HashMap<>();
	/** 存储API请求失败的数数 */
	private Map<String, Long> requstFailedCount = new HashMap<>();
	/** 存储API请求的数量 */
	private Map<String, Long> requstCount = new HashMap<>();
	/** 当前Vertx的唯一标识 */
	private String thisVertxName;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		LOG.info("start System Verticle ... ");
		thisVertxName = System.getProperty("thisVertxName", "VX-API");
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_GET_INFO, this::getSysInfo);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_ERROR, this::PlusError);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, this::plusTrackInfos);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_GET_TRACK_INFO, this::getTrackInfo);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_BLACK_IP_FIND, this::findIpList);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_BLACK_IP_REPLACE, this::replaceIpList);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_VX_REQUEST, msg -> requestVxApiCount++);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_HTTP_API_REQUEST, msg -> {
			requestHttpApiCount++;
			currentHttpApiProcessingCount++;
		});
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_MINUS_CURRENT_PROCESSING, msg -> {
			if (currentHttpApiProcessingCount > 0) {
				currentHttpApiProcessingCount--;
			}
		});

		LOG.info("start System Verticle successful");
		super.start(startFuture);
	}

	/**
	 * 查看系统基本信息
	 * 
	 * @param msg
	 */
	public void getSysInfo(Message<JsonObject> msg) {
		// 获取在线网关应用与API的数量
		Future<JsonObject> countResult = Future.future();
		vertx.eventBus().<JsonObject>send(thisVertxName + VxApiEventBusAddressConstant.DEPLOY_APP_COUNT, null, res -> {
			if (res.succeeded()) {
				JsonObject result = res.result().body();
				if (LOG.isDebugEnabled()) {
					LOG.debug("执行获取在线应用的数量-->结果:" + result);
				}
				countResult.complete(result);
			} else {
				countResult.complete(new JsonObject());
				LOG.error("执行获取在线应用的数量-->失败:", res.cause());
			}
		});
		countResult.setHandler(res -> {
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
			result.put("appCount", res.result().getInteger("app", 0));
			result.put("apiCount", res.result().getInteger("api", 0));
			result.put("errorCount", errorCount);
			result.put("requestVxApiCount", requestVxApiCount);
			result.put("requestHttpApiCount", requestHttpApiCount);
			result.put("currentHttpApiProcessingCount", currentHttpApiProcessingCount);
			vertx.eventBus().<JsonObject>send(thisVertxName + VxApiEventBusAddressConstant.FIND_BLACKLIST, null, balckList -> {
				if (res.succeeded()) {
					JsonObject body = balckList.result().body();
					if (body.getValue(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME) instanceof JsonArray) {
						result.put("content", body.getJsonArray(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME));
					} else if (body.getValue(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME) instanceof String) {
						result.put("content", new JsonArray(body.getString(VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME)));
					} else {
						result.put("content", new JsonArray());
					}
					if (LOG.isDebugEnabled()) {
						LOG.debug("执行查询运行状态-->结果:" + result);
					}
					msg.reply(result);
				} else {
					LOG.error("执行查询运行状态-->结果:" + balckList.cause());
					msg.fail(500, balckList.cause().getMessage());
				}
			});
		});
	}

	/**
	 * 添加异常的数量
	 * 
	 * @param msg
	 */
	public void PlusError(Message<JsonObject> msg) {
		errorCount += 1;
		if (msg.body() != null) {
			VxApiTrackInfos infos = VxApiTrackInfos.fromJson(msg.body());
			LOG.error(MessageFormat.format("应用:{0} , API:{1} ,在执行的过程中发生了异常:{2} ,堆栈信息{3}", infos.getAppName(), infos.getApiName(),
					infos.getErrMsg(), infos.getErrStackTrace()));
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
			if (LOG.isDebugEnabled()) {
				LOG.debug(MessageFormat.format("应用:{0} , API:{1} ,执行结果{2}", infos.getAppName(), infos.getApiName(), infos));
			}
			// map的key
			String key = infos.getAppName() + "-" + infos.getApiName();
			// 记录API相关信息
			if (!infos.isSuccessful()) {
				// 记录异常
				errorCount += 1;
				if (requstFailedCount.get(key) == null) {
					requstFailedCount.put(key, 0L);
				}
				requstFailedCount.put(key, requstFailedCount.get(key) + 1);
				LOG.error(MessageFormat.format("应用:{0} , API:{1} ,在执行的过程中发生了异常:{2} ,堆栈信息{3}", infos.getAppName(), infos.getApiName(),
						infos.getErrMsg(), infos.getErrStackTrace()));
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
		result.put("rc", requstCount.get(key) == null ? 0L : requstCount.get(key));
		result.put("ec", requstFailedCount.get(key) == null ? 0L : requstFailedCount.get(key));
		result.put("track", trackSucceededMap.get(key) == null ? new JsonObject() : trackSucceededMap.get(key));
		msg.reply(result);
	}

	/**
	 * 查看黑名单ip地址
	 * 
	 * @param msg
	 */
	public void findIpList(Message<JsonObject> msg) {
		vertx.eventBus().<JsonObject>send(thisVertxName + VxApiEventBusAddressConstant.FIND_BLACKLIST, null, res -> {
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
			if (msg.body().getValue("ipList") instanceof JsonArray) {
				JsonArray array = msg.body().getJsonArray("ipList");
				JsonObject body = new JsonObject().put("blacklistName", "blacklist").put("blacklistBody", array);
				vertx.eventBus().<Integer>send(thisVertxName + VxApiEventBusAddressConstant.REPLACE_BLACKLIST, body, res -> {
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
