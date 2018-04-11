package com.szmirren.vxApi.core.handler.route.impl;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.entity.VxApiServerURL;
import com.szmirren.vxApi.core.entity.VxApiServerURLInfo;
import com.szmirren.vxApi.core.entity.VxApiServerURLPollingPolicy;
import com.szmirren.vxApi.core.entity.VxApiTrackInfos;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.enums.LoadBalanceEnum;
import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.core.enums.ParamSystemVarTypeEnum;
import com.szmirren.vxApi.core.handler.route.VxApiRouteConstant;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerHttpService;
import com.szmirren.vxApi.core.options.VxApiParamOptions;
import com.szmirren.vxApi.core.options.VxApiServerEntranceHttpOptions;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandler;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
/**
 * VxApiRoute HTTP/HTTPS服务类型的处理器
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiRouteHandlerHttpServiceImpl implements VxApiRouteHandlerHttpService {
	/**
	 * 当前Vertx的唯一标识
	 */
	private String thisVertxName;
	private String appName;
	private boolean isNext;
	private VxApis api;
	private VxApiServerURLPollingPolicy policy;
	private VxApiServerEntranceHttpOptions serOptions;
	private HttpClient httpClient;
	private WebClient webClient;
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
		JsonObject body = api.getServerEntrance().getBody();
		serOptions = VxApiServerEntranceHttpOptions.fromJson(body);
		if (serOptions == null) {
			throw new NullPointerException("HTTP/HTTPS服务类型的配置文件无法装换为服务类");
		}
		List<VxApiServerURL> urls = serOptions.getServerUrls();
		if (urls == null || urls.size() < 1) {
			throw new NullPointerException("服务端地址不存在");
		}
		// 检查参数是否符合要求
		chackVxApiParamOptions(serOptions);
		if (serOptions.getBalanceType() == null) {
			serOptions.setBalanceType(LoadBalanceEnum.POLLING_AVAILABLE);
		}
		policy = new VxApiServerURLPollingPolicy(urls);
	}

	@Override
	public void handle(RoutingContext rct) {
		// 看有没有可用的服务连接
		if (policy.isHaveService()) {// 有可用连接
			// 后台服务连接信息
			VxApiServerURLInfo urlInfo;
			if (serOptions.getBalanceType() == LoadBalanceEnum.IP_HASH) {
				String ip = rct.request().remoteAddress().host();
				urlInfo = policy.getUrl(ip);
			} else {
				urlInfo = policy.getUrl();
			}
			// 执行监控
			VxApiTrackInfos trackInfo = new VxApiTrackInfos(appName, api.getApiName());
			// 统计请求内容长度
			String resLen = rct.request().getHeader("Content-Length");
			trackInfo.setRequestBufferLen(resLen == null ? 0 : StrUtil.getintTry(resLen));
			// 获得请求连接与进行请求
			String requestPath = urlInfo.getUrl();
			MultiMap headers = new CaseInsensitiveHeaders();
			if (serOptions.getParams() != null) {
				// 路径参数
				QueryStringEncoder queryParam = new QueryStringEncoder("");
				for (VxApiParamOptions p : serOptions.getParams()) {
					String param = null;
					if (p.getType() == 0 || p.getType() == 2) {
						if (p.getApiParamPosition() == ParamPositionEnum.HEADER) {
							param = rct.request().getHeader(p.getApiParamName());
						} else {
							param = rct.request().getParam(p.getApiParamName());
						}
					} else if (p.getType() == 1) {
						if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_HOST) {
							param = rct.request().remoteAddress().host();
						} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_PORT) {
							param = Integer.toString(rct.request().remoteAddress().port());
						} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_PATH) {
							param = rct.request().path() == null ? "" : rct.request().path();
						} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_SESSION_ID) {
							param = rct.session().id();
						} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_ABSOLUTE_URI) {
							param = rct.request().absoluteURI();
						} else if (p.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_REQUEST_SCHEMA) {
							param = rct.request().scheme();
						} else if (p.getSysParamType() == ParamSystemVarTypeEnum.SERVER_API_NAME) {
							param = api.getApiName();
						} else if (p.getSysParamType() == ParamSystemVarTypeEnum.SERVER_UNIX_TIME) {
							param = Long.toString(System.currentTimeMillis());
						} else if (p.getSysParamType() == ParamSystemVarTypeEnum.SERVER_USER_AGENT) {
							param = VxApiGatewayAttribute.VX_API_USER_AGENT;
						}
					} else if (p.getType() == 9) {
						param = p.getParamValue().toString();
					} else {
						continue;
					}
					if (param == null) {
						continue;
					}
					if (p.getSerParamPosition() == ParamPositionEnum.HEADER) {
						headers.add(p.getSerParamName(), param);
					} else if (p.getSerParamPosition() == ParamPositionEnum.PATH) {
						requestPath = requestPath.replace(":" + p.getSerParamName(), param);
					} else {
						queryParam.addParam(p.getSerParamName(), param);
					}
				}
				requestPath += queryParam.toString();
			}
			HttpClientRequest request = httpClient.requestAbs(serOptions.getMethod(), requestPath).setTimeout(serOptions.getTimeOut());
			request.handler(resp -> {
				// 设置请求响应时间
				trackInfo.setResponseTime(Instant.now());
				HttpServerResponse response = rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
						.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType()).setChunked(true);
				Pump.pump(resp, response).start();
				resp.endHandler(end -> {
					// 透传header
					Set<String> tranHeaders = api.getResult().getTranHeaders();
					if (tranHeaders != null && tranHeaders.size() > 0) {
						tranHeaders.forEach(h -> {
							rct.response().putHeader(h, resp.getHeader(h) == null ? "" : resp.getHeader(h));
						});
					}
					if (isNext) {
						rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>succeededFuture(true));// 告诉后置处理器当前操作成功执行
						rct.next();
					} else {
						rct.response().end();
					}
					// 统计响应长度
					String repLen = resp.getHeader("Content-Length");
					trackInfo.setResponseBufferLen(repLen == null ? 0 : StrUtil.getintTry(repLen));
					trackInfo.setEndTime(Instant.now());
					rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, trackInfo.toJson());
				});
			});
			// 异常处理
			request.exceptionHandler(e -> {
				if (isNext) {
					rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>failedFuture(e));// 告诉后置处理器当前操作成功执行
					rct.next();
				} else {
					HttpServerResponse response = rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
							.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType());
					// 如果是连接异常返回无法连接的错误信息,其他异常返回相应的异常
					if (e instanceof ConnectException || e instanceof TimeoutException) {
						response.setStatusCode(api.getResult().getCantConnServerStatus()).end(api.getResult().getCantConnServerExample());
					} else {
						response.setStatusCode(api.getResult().getFailureStatus()).end(api.getResult().getFailureExample());
					}
				}
				// 提交连接请求失败
				policy.reportBadService(urlInfo.getIndex());
				trackInfo.setEndTime(Instant.now());
				// 记录与后台交互发生错误
				trackInfo.setSuccessful(false);
				trackInfo.setErrMsg(e.getMessage());
				trackInfo.setErrStackTrace(e.getStackTrace());
				rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, trackInfo.toJson());
			});
			// 设置请求时间
			trackInfo.setRequestTime(Instant.now());
			if (headers != null && headers.size() > 0) {
				request.headers().addAll(headers);
			}
			// 设置User-Agent
			String agnet = request.headers().get("User-Agent");
			if (agnet == null) {
				agnet = VxApiGatewayAttribute.VX_API_USER_AGENT;
			} else {
				agnet += " " + VxApiGatewayAttribute.VX_API_USER_AGENT;
			}
			request.putHeader("User-Agent", agnet);
			request.end();
			// 判断是否有坏的连接
			if (policy.isHaveBadService()) {
				if (!policy.isCheckWaiting()) {
					if (webClient == null) {
						WebClient.create(rct.vertx());
					}
					policy.setCheckWaiting(true);
					rct.vertx().setTimer(serOptions.getRetryTime(), testConn -> {
						List<VxApiServerURLInfo> service = policy.getBadService();
						for (VxApiServerURLInfo urlinfo : service) {
							webClient.requestAbs(serOptions.getMethod(), urlinfo.getUrl()).timeout(serOptions.getTimeOut()).send(res -> {
								if (res.succeeded()) {
									policy.reportGreatService(urlinfo.getIndex());
								}
							});
						}
						policy.setCheckWaiting(false);
					});
				}
			}
		} else {
			// 无可用连接时,结束当前处理器并尝试重新尝试连接是否可用
			if (isNext) {
				// 告诉后置处理器当前操作执行结果
				rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>failedFuture(new ConnectException("无法连接上后台交互的服务器")));
				rct.next();
			} else {
				rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
						.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType()).setStatusCode(api.getResult().getCantConnServerStatus())
						.end(api.getResult().getCantConnServerExample());
			}
			if (!policy.isCheckWaiting()) {
				policy.setCheckWaiting(true);
				rct.vertx().setTimer(serOptions.getRetryTime(), testConn -> {
					List<VxApiServerURLInfo> service = policy.getBadService();
					for (VxApiServerURLInfo urlinfo : service) {
						webClient.requestAbs(serOptions.getMethod(), urlinfo.getUrl()).timeout(serOptions.getTimeOut()).send(res -> {
							if (res.succeeded()) {
								policy.reportGreatService(urlinfo.getIndex());
							}
						});
					}
					policy.setCheckWaiting(false);
				});
			}
		}
	}
	/**
	 * 服务入口的参数检查与路径初始化
	 * 
	 * @param path
	 * @param ser
	 */
	public void chackVxApiParamOptions(VxApiServerEntranceHttpOptions ser) {
		if (ser.getParams() != null) {
			for (VxApiParamOptions p : ser.getParams()) {
				boolean serIsNull = p.getSerParamName() == null || p.getSerParamPosition() == null;
				boolean apiIsNull = p.getApiParamName() == null || p.getApiParamPosition() == null;
				if (p.getType() == 0 && (serIsNull || apiIsNull)) {
					throw new NullPointerException("参数映射名字与位置不能为null");
				} else if (p.getType() == 1 && (p.getSysParamType() == null || serIsNull)) {
					throw new NullPointerException("系统参数的名字,请求名字与位置不能为null");
				} else if (p.getType() == 2 && (serIsNull || apiIsNull)) {
					throw new NullPointerException("透传参数名字与位置不能为null");
				} else if (p.getType() == 9 && (p.getParamValue() == null || serIsNull)) {
					throw new NullPointerException("自定义常量参数值,请求名字与位置不能为null");
				}
			}
		}
	}
}
