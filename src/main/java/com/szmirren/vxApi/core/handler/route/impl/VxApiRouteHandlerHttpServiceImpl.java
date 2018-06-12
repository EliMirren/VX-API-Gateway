package com.szmirren.vxApi.core.handler.route.impl;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.entity.VxApiServerURL;
import com.szmirren.vxApi.core.entity.VxApiServerURLInfo;
import com.szmirren.vxApi.core.entity.VxApiServerURLPollingPolicy;
import com.szmirren.vxApi.core.entity.VxApiTrackInfos;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.enums.LoadBalanceEnum;
import com.szmirren.vxApi.core.handler.route.VxApiRouteConstant;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerHttpService;
import com.szmirren.vxApi.core.options.VxApiServerEntranceHttpOptions;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandler;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.RoutingContext;

/**
 * VxApiRoute HTTP/HTTPS服务类型的处理器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiRouteHandlerHttpServiceImpl implements VxApiRouteHandlerHttpService {
	/** 日志处理 */
	private static final Logger LOG = LogManager.getLogger(VxApiRouteHandlerHttpServiceImpl.class);
	/** 当前Vertx的唯一标识 */
	private String thisVertxName;
	/** 应用的名称 */
	private String appName;
	/** 有否使用后置处理器 */
	private boolean isNext;
	/** API配置 */
	private VxApis api;
	/** 后端服务策略 */
	private VxApiServerURLPollingPolicy policy;
	/** HTTP/HTTPS服务类型的配置 */
	private VxApiServerEntranceHttpOptions serOptions;
	/** HTTP客户端 */
	private HttpClient httpClient;

	/**
	 * 初始化一个服务器
	 * 
	 * @param appName
	 *          应用的名称
	 * @param isNext
	 *          是否有后置处理器
	 * @param api
	 *          API相关的配置
	 * @param httpClient
	 *          {@link io.vertx.core.http.HttpClient}
	 * @throws MalformedURLException
	 *           错误的URI路径
	 * @throws NullPointerException
	 *           少了参数
	 */
	public VxApiRouteHandlerHttpServiceImpl(String appName, boolean isNext, VxApis api, HttpClient httpClient)
			throws NullPointerException, MalformedURLException {
		super();
		this.thisVertxName = System.getProperty("thisVertxName", "VX-API");
		this.appName = appName;
		this.isNext = isNext;
		this.api = api;
		this.httpClient = httpClient;
		serOptions = VxApiServerEntranceHttpOptions.fromJson(api.getServerEntrance().getBody());
		if (serOptions == null) {
			throw new NullPointerException("HTTP/HTTPS服务类型的配置文件无法装换为服务类");
		}
		List<VxApiServerURL> urls = serOptions.getServerUrls();
		if (urls == null || urls.size() < 1) {
			throw new NullPointerException("服务端地址不存在");
		}
		if (serOptions.getBalanceType() == null) {
			serOptions.setBalanceType(LoadBalanceEnum.POLLING_AVAILABLE);
		}
		policy = new VxApiServerURLPollingPolicy(urls);
	}

	@Override
	public void handle(RoutingContext rct) {
		// 添加请求到达核心处理器的数量与当前API正在处理的数量
		rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_HTTP_API_REQUEST, null);
		// 响应用户请求结束时间,响应后检查是否有坏的服务,报告当前API已经处理完毕
		rct.response().endHandler(end -> {
			if (policy.isHaveBadService()) {
				LOG.warn(
						String.format("应用:%s -> API:%s,后台服务存在不可用的后台服务URL,VX将以设定的重试时间:%d进行重试", appName, api.getApiName(), serOptions.getRetryTime()));
				// 进入重试连接后台服务
				retryConnServer(rct.vertx());
			}
			// 减少当前正在处理的数量
			rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_MINUS_CURRENT_PROCESSING, null);
		});
		// 用户请求的request
		HttpServerRequest rctRequest = rct.request();
		// 用户请求的Header
		MultiMap rctHeaders = rctRequest.headers();
		// 当前服务的response
		HttpServerResponse rctResponse = rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
				.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType());
		// 判断后台服务是否有可用连接,有可用连接进行请求,如果没有可用连接进行重试
		if (policy.isHaveService()) {
			// 执行监控
			VxApiTrackInfos trackInfo = new VxApiTrackInfos(appName, api.getApiName());
			trackInfo.setRequestBufferLen(rct.get(VxApiRouteConstant.BODY_KEY_CONTENT_LENGTH));
			// 后台服务连接信息
			VxApiServerURLInfo urlInfo;
			if (serOptions.getBalanceType() == LoadBalanceEnum.IP_HASH) {
				String ip = rct.request().remoteAddress().host();
				urlInfo = policy.getUrl(ip);
			} else {
				urlInfo = policy.getUrl();
			}
			// 获得请求服务连接
			String reqURL = urlInfo.getUrl();
			// 获取请求的Path参数并加载Path参数
			MultiMap reqPathParam = rct.get(VxApiRouteConstant.BODY_KEY_PATH_TYPE_MultiMap);
			if (reqPathParam.entries() != null) {
				for (Entry<String, String> entry : reqPathParam.entries()) {
					reqURL = reqURL.replace(":" + entry.getKey(), entry.getValue());
				}
			}
			// 请求服务的Header
			MultiMap reqHeaderParam = rct.get(VxApiRouteConstant.BODY_KEY_HEADER_TYPE_MultiMap);
			// 请求服务的Query参数
			QueryStringEncoder reqQueryParam = rct.get(VxApiRouteConstant.BODY_KEY_QUERY_TYPE_QueryStringEncoder);
			// 请求服务的Body参数
			QueryStringEncoder reqBodyParam = rct.get(VxApiRouteConstant.BODY_KEY_BODY_TYPE_QueryStringEncoder);
			reqURL += reqQueryParam.toString();
			// 请求处理器
			HttpClientRequest request = httpClient.requestAbs(serOptions.getMethod(), reqURL).setTimeout(serOptions.getTimeOut());
			request.putHeader(VxApiRouteConstant.USER_AGENT, VxApiGatewayAttribute.VX_API_USER_AGENT);
			// 设置请求时间
			trackInfo.setRequestTime(Instant.now());
			if (reqHeaderParam != null && reqHeaderParam.size() > 0) {
				request.headers().addAll(reqHeaderParam);
			}
			// 异常处理器
			request.exceptionHandler(e -> {
				if (isNext) {
					rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>failedFuture(e));// 告诉后置处理器当前操作成功执行
					rct.next();
				} else {
					// 请求结束
					trackInfo.setEndTime(Instant.now());
					// 如果是用户已经关闭了请求连接给予warn提示并结束执行
					if (e != null && e.getMessage().contains("Response is closed")) {
						trackInfo.setEndTime(Instant.now());
						// 记录与后台交互发生错误
						trackInfo.setSuccessful(false);
						trackInfo.setErrMsg("执行请求后端服务的异常:Response is closed,可能是用户关闭了请求");
						trackInfo.setErrStackTrace(null);
						rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, trackInfo.toJson());
					} else {
						// 记录与后台交互发生错误
						trackInfo.setSuccessful(false);
						if (e != null) {
							trackInfo.setErrMsg(e.getMessage());
							trackInfo.setErrStackTrace(e.getStackTrace());
						}
						rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, trackInfo.toJson());
					}
					// 如果是连接异常返回无法连接的错误信息,其他异常返回相应的异常
					try {
						if (e instanceof ConnectException || e instanceof TimeoutException) {
							// 提交连接请求失败
							if (LOG.isDebugEnabled()) {
								LOG.error("URL:" + urlInfo.getUrl() + ",下标:" + urlInfo.getIndex() + " 请求后端服务连接失败或者连接超时,已经提交给连接策略");
							}
							policy.reportBadService(urlInfo.getIndex());
							rctResponse.putHeader(VxApiRouteConstant.DATE, StrUtil.getRfc822DateFormat(new Date()))
									.setStatusCode(api.getResult().getCantConnServerStatus()).end(api.getResult().getCantConnServerExample());
						} else {
							rctResponse.putHeader(VxApiRouteConstant.DATE, StrUtil.getRfc822DateFormat(new Date()))
									.setStatusCode(api.getResult().getFailureStatus()).end(api.getResult().getFailureExample());
						}
					} catch (Exception e1) {
						// 如果是用户已经关闭了请求连接给予warn提示并结束执行
						if (e != null && !e.getMessage().contains("Response is closed")) {
							LOG.error("在请求后端服务的异常处理器中响应用户请求-->异常:", e1);
						}
					}
				}
			});
			request.handler(resp -> {
				// 设置请求响应时间
				trackInfo.setResponseTime(Instant.now());
				rctResponse.putHeader(VxApiRouteConstant.DATE, StrUtil.getRfc822DateFormat(new Date())).setChunked(true);
				// 透传header
				Set<String> tranHeaders = api.getResult().getTranHeaders();
				if (tranHeaders != null && tranHeaders.size() > 0) {
					tranHeaders.forEach(h -> {
						rctResponse.putHeader(h, resp.getHeader(h) == null ? "" : resp.getHeader(h));
					});
				}
				Pump respPump = Pump.pump(resp, rctResponse);
				respPump.start();
				resp.endHandler(end -> {
					if (isNext) {
						rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>succeededFuture(true));// 告诉后置处理器当前操作成功执行
						rct.next();
					} else {
						rctResponse.end();
					}
					// 统计响应长度
					String repLen = resp.getHeader(VxApiRouteConstant.CONTENT_LENGTH);
					trackInfo.setResponseBufferLen(repLen == null ? 0 : StrUtil.getintTry(repLen));
					trackInfo.setEndTime(Instant.now());
					rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, trackInfo.toJson());
				});
				resp.exceptionHandler(e -> {
					LOG.error("API:" + api.getApiName() + ",pump在收到后端服务响应中发生了异常:" + e);
					respPump.stop();
					request.end();
				});
			});
			if (LOG.isDebugEnabled()) {
				LOG.debug("请求后台服务的Headers参数:" + reqHeaderParam + ";");
				LOG.debug("请求后台服务的Query参数:" + reqQueryParam + ";");
				LOG.debug("请求后台服务的URL:" + reqURL + ";");
				LOG.debug("请求后台服务的Body参数:" + reqBodyParam + ";");
			}
			// 判断是否透传Body
			if (api.isPassBody()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("执行透传Body...");
				}
				if (rctHeaders.get(VxApiRouteConstant.CONTENT_TYPE) != null) {
					request.putHeader(VxApiRouteConstant.CONTENT_TYPE, rctHeaders.get(VxApiRouteConstant.CONTENT_TYPE));
				} else if (rctHeaders.get(VxApiRouteConstant.CONTENT_TYPE_LOWER_CASE) != null) {
					request.putHeader(VxApiRouteConstant.CONTENT_TYPE_LOWER_CASE, rctHeaders.get(VxApiRouteConstant.CONTENT_TYPE_LOWER_CASE));
				}
				request.setChunked(true);
				Pump reqPump = Pump.pump(rctRequest, request);
				reqPump.start();
				rctRequest.exceptionHandler(e -> {
					LOG.error("API:" + api.getApiName() + ",pump在请求后端服务中发生了异常:" + e);
					reqPump.stop();
					request.end();
				});
				rctRequest.endHandler(end -> {
					if (LOG.isDebugEnabled()) {
						LOG.debug("执行透传Body-->完成");
					}
					request.end();
				});
			} else {
				request.end(reqBodyParam.toString().replace("?", ""));
			}
		} else {
			// 进入该地方证明没有可用链接,结束当前处理器并尝试重新尝试连接是否可用
			if (isNext) {
				rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>failedFuture(new ConnectException("无法连接上后台交互的服务器")));
				rct.next();
			} else {
				rctResponse.putHeader(VxApiRouteConstant.DATE, StrUtil.getRfc822DateFormat(new Date()))
						.setStatusCode(api.getResult().getCantConnServerStatus()).end(api.getResult().getCantConnServerExample());
			}
			LOG.warn(
					String.format("应用:%s -> API:%s,后台服务已不存在可用的后台服务URL,VX将以设定的重试时间:%d进行重试", appName, api.getApiName(), serOptions.getRetryTime()));
			// 进入重试连接后台服务
			retryConnServer(rct.vertx());
		}
	}
	/**
	 * 当存在坏的后台服务时重试连接后台看后台连接是否可用
	 * 
	 * @param vertx
	 */
	public void retryConnServer(Vertx vertx) {
		if (!policy.isCheckWaiting()) {
			policy.setCheckWaiting(true);
			vertx.setTimer(serOptions.getRetryTime(), testConn -> {
				if (LOG.isDebugEnabled()) {
					LOG.debug(String.format("应用:%s -> API:%s重试连接后台服务URL...", appName, api.getApiName()));
				}
				List<VxApiServerURLInfo> service = policy.getBadService();
				if (service != null) {
					for (VxApiServerURLInfo urlinfo : service) {
						httpClient.requestAbs(serOptions.getMethod(), urlinfo.getUrl()).setTimeout(serOptions.getTimeOut()).handler(resp -> {
							int statusCode = resp.statusCode();
							if (statusCode != 200) {
								LOG.warn(String.format("应用:%s -> API:%s重试连接后台服务URL,连接成功但得到一个%d状态码,", appName, api.getApiName(), statusCode));
							} else {
								if (LOG.isDebugEnabled()) {
									LOG.debug(String.format("应用:%s -> API:%s重试连接后台服务URL,连接成功 !", appName, api.getApiName()));
								}
							}
							policy.reportGreatService(urlinfo.getIndex());
						}).exceptionHandler(e -> {
							if (LOG.isDebugEnabled()) {
								LOG.debug(String.format("应用:%s -> API:%s重试连接后台服务URL->失败:", appName, api.getApiName()), e);
							}
						}).end();
					}
				}
				policy.setCheckWaiting(false);
			});
		}
	}
}
