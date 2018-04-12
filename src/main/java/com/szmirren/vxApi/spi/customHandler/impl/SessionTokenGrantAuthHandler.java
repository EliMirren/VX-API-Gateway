package com.szmirren.vxApi.spi.customHandler.impl;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.entity.VxApiServerURLInfo;
import com.szmirren.vxApi.core.entity.VxApiServerURLPollingPolicy;
import com.szmirren.vxApi.core.entity.VxApiTrackInfos;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.enums.LoadBalanceEnum;
import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.core.enums.ParamSystemVarTypeEnum;
import com.szmirren.vxApi.core.options.VxApiParamOptions;
import com.szmirren.vxApi.core.options.VxApiServerEntranceHttpOptions;
import com.szmirren.vxApi.spi.common.HttpHeaderConstant;
import com.szmirren.vxApi.spi.customHandler.VxApiCustomHandler;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandler;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

/**
 * session token权限认证的认证授权
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class SessionTokenGrantAuthHandler implements VxApiCustomHandler {
	/**
	 * 存放Session中token值key的名字
	 */
	private final static String VX_API_SESSION_TOKEN_NAME = "vxApiSessionToken";
	/**
	 * 存放请求参数中token值key的名字
	 */
	private final static String VX_API_USER_TOKEN_NAME = "vxApiUserToken";
	/**
	 * 存在API网关token值的key名字
	 */
	private String saveTokenName = VX_API_SESSION_TOKEN_NAME;
	/**
	 * 服务器返回来的Token名字
	 */
	private String getTokenName = VX_API_USER_TOKEN_NAME;
	/**
	 * 路由器要结束了还是讲任务传到下一个处理器,默认为结束
	 */
	private boolean isNext = false;
	/**
	 * 后台服务的轮询策略
	 */
	private VxApiServerURLPollingPolicy policy;

	/**
	 * 请求的客户端
	 */
	private WebClient webClient;
	/**
	 * api
	 */
	private VxApis api;
	/**
	 * 服务端配置文件
	 */
	private VxApiServerEntranceHttpOptions serOptions;
	/**
	 * 当前Vertx的唯一标识
	 */
	private String thisVertxName;

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
			VxApiTrackInfos trackInfo = new VxApiTrackInfos(api.getAppName(), api.getApiName());
			trackInfo.setRequestBufferLen(rct.getBody() == null ? 0 : rct.getBody().length());

			String requestPath = urlInfo.getUrl();
			MultiMap headers = new CaseInsensitiveHeaders();
			MultiMap queryParams = new CaseInsensitiveHeaders();
			if (serOptions.getParams() != null) {
				for (VxApiParamOptions p : serOptions.getParams()) {
					String param = "";
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
						requestPath.replace(":" + p.getSerParamName(), param);
					} else {
						queryParams.add(p.getSerParamName(), param);
					}
				}
			}
			HttpRequest<Buffer> request = webClient.requestAbs(serOptions.getMethod(), requestPath).timeout(serOptions.getTimeOut());
			headers.forEach(va -> request.putHeader(va.getKey(), va.getValue()));
			queryParams.forEach(va -> request.addQueryParam(va.getKey(), va.getValue()));
			trackInfo.setRequestTime(Instant.now());
			request.send(res -> {
				trackInfo.setResponseTime(Instant.now());
				if (res.succeeded()) {
					HttpResponse<Buffer> result = res.result();
					Set<String> tranHeaders = api.getResult().getTranHeaders();
					if (tranHeaders != null && tranHeaders.size() > 0) {
						tranHeaders.forEach(h -> {
							rct.response().putHeader(h, result.getHeader(h) == null ? "" : result.getHeader(h));
						});
					}

					HttpServerResponse response = rct.response();
					// 得到后台服务传过来的token
					String token = res.result().getHeader(getTokenName);
					if (token != null && !"".equals(token)) {
						rct.session().put(saveTokenName, token);
					}
					response.putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
							.putHeader(HttpHeaderConstant.CONTENT_TYPE, api.getContentType()).setChunked(true).write(result.body());
					if (isNext) {
						rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>succeededFuture(true));// 告诉后置处理器当前操作成功执行
						rct.next();
					} else {
						response.end();
					}
					trackInfo.setResponseBufferLen(result.body() == null ? 0 : result.body().length());
					trackInfo.setEndTime(Instant.now());
				} else {
					if (isNext) {
						rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>failedFuture(res.cause()));// 告诉后置处理器当前操作成功执行
						rct.next();
					} else {
						HttpServerResponse response = rct.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
								.putHeader(HttpHeaderConstant.CONTENT_TYPE, api.getContentType());
						// 如果是连接异常返回无法连接的错误信息,其他异常返回相应的异常
						if (res.cause() instanceof ConnectException || res.cause() instanceof TimeoutException) {
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
					trackInfo.setErrMsg(res.cause().getMessage());
					trackInfo.setErrStackTrace(res.cause().getStackTrace());
				}
				rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, trackInfo.toJson());
			});

			// 判断是否有坏的连接
			if (policy.isHaveBadService()) {
				if (!policy.isCheckWaiting()) {
					policy.setCheckWaiting(true);
					rct.vertx().setTimer(serOptions.getRetryTime(), testConn -> {
						List<VxApiServerURLInfo> service = policy.getBadService();
						if (service != null) {
							for (VxApiServerURLInfo urlinfo : service) {
								webClient.requestAbs(serOptions.getMethod(), urlinfo.getUrl()).timeout(serOptions.getTimeOut()).send(res -> {
									if (res.succeeded()) {
										policy.reportGreatService(urlinfo.getIndex());
									}
								});
							}
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
				rct.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
						.putHeader(HttpHeaderConstant.CONTENT_TYPE, api.getContentType()).setStatusCode(api.getResult().getCantConnServerStatus())
						.end(api.getResult().getCantConnServerExample());
			}
			if (!policy.isCheckWaiting()) {
				policy.setCheckWaiting(true);
				rct.vertx().setTimer(serOptions.getRetryTime(), testConn -> {
					List<VxApiServerURLInfo> service = policy.getBadService();
					if (service != null) {
						for (VxApiServerURLInfo urlinfo : service) {
							webClient.requestAbs(serOptions.getMethod(), urlinfo.getUrl()).timeout(serOptions.getTimeOut()).send(res -> {
								if (res.succeeded()) {
									policy.reportGreatService(urlinfo.getIndex());
								}
							});
						}
					}
					policy.setCheckWaiting(false);
				});
			}
		}

	}

	/**
	 * 实例化一个session_token的授权处理器
	 * 
	 * @param option
	 * @param apis
	 * @param httpClient
	 * @throws NullPointerException
	 * @throws MalformedURLException
	 */
	public SessionTokenGrantAuthHandler(JsonObject option, VxApis apis, HttpClient httpClient)
			throws NullPointerException, MalformedURLException {
		if (option.getValue("saveTokenName") instanceof String) {
			this.saveTokenName = option.getString("saveTokenName");
		}
		if (option.getValue("getTokenName") instanceof String) {
			this.getTokenName = option.getString("getTokenName");
		}
		if (option.getValue("isNext") instanceof Boolean) {
			this.isNext = option.getBoolean("isNext");
		}
		this.thisVertxName = System.getProperty("thisVertxName", "VX-API");

		webClient = WebClient.wrap(httpClient);
		this.api = apis;
		this.serOptions = VxApiServerEntranceHttpOptions.fromJson(apis.getServerEntrance().getBody());
		chackVxApiParamOptions(serOptions);
		this.policy = new VxApiServerURLPollingPolicy(serOptions.getServerUrls());
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
