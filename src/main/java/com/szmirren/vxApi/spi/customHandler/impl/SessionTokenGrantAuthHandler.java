package com.szmirren.vxApi.spi.customHandler.impl;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.common.HttpUtils;
import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.entity.VxApiEntranceParam;
import com.szmirren.vxApi.core.entity.VxApiServerURLInfo;
import com.szmirren.vxApi.core.entity.VxApiServerURLPollingPolicy;
import com.szmirren.vxApi.core.entity.VxApiTrackInfos;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.enums.LoadBalanceEnum;
import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.core.enums.ParamSystemVarTypeEnum;
import com.szmirren.vxApi.core.handler.route.VxApiRouteConstant;
import com.szmirren.vxApi.core.options.VxApiParamCheckOptions;
import com.szmirren.vxApi.core.options.VxApiParamOptions;
import com.szmirren.vxApi.core.options.VxApiServerEntranceHttpOptions;
import com.szmirren.vxApi.spi.common.HttpHeaderConstant;
import com.szmirren.vxApi.spi.customHandler.VxApiCustomHandler;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandler;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
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
	private static final Logger LOG = LogManager.getLogger(SessionTokenGrantAuthHandler.class);

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
			String contentType = rct.request().headers().get(VxApiRouteConstant.CONTENT_TYPE);
			if (contentType != null) {
				if (contentType.indexOf("urlencoded") > -1) {
					if (api.isBodyAsQuery()) {
						rct.request().bodyHandler(body -> {
							bodyUrlParamToBodyParams(body, rct.request().params(), null);
						});
					}
				}
			}
			rct.request().endHandler(end -> {
				// 检查参数是否符合
				boolean checkResult = checkParam(rct);
				if (!checkResult) {
					if (!rct.response().ended()) {
						rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
								.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType())
								.setStatusCode(api.getResult().getApiEnterCheckFailureStatus()).end(api.getResult().getApiEnterCheckFailureExample());
					}
					return;
				}
				// 执行监控
				VxApiTrackInfos trackInfo = new VxApiTrackInfos(api.getAppName(), api.getApiName());
				trackInfo.setRequestBufferLen(rct.getBody() == null ? 0 : rct.getBody().length());
				String requestPath = urlInfo.getUrl();
				MultiMap headers = new CaseInsensitiveHeaders();
				MultiMap queryParams = new CaseInsensitiveHeaders();
				MultiMap bodyParams = new CaseInsensitiveHeaders();
				if (serOptions.getParams() != null) {
					loadParam(rct, requestPath, headers, queryParams, bodyParams);
				}
				HttpRequest<Buffer> request = webClient.requestAbs(serOptions.getMethod(), requestPath).timeout(serOptions.getTimeOut());
				request.headers().addAll(headers);
				request.queryParams().addAll(queryParams);
				trackInfo.setRequestTime(Instant.now());
				request.sendForm(bodyParams, res -> {
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
							if (!response.ended()) {
								response.end();
							}
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
							if (!response.ended()) {
								if (res.cause() instanceof ConnectException || res.cause() instanceof TimeoutException) {
									response.setStatusCode(api.getResult().getCantConnServerStatus()).end(api.getResult().getCantConnServerExample());
								} else {
									response.setStatusCode(api.getResult().getFailureStatus()).end(api.getResult().getFailureExample());
								}
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
			});
			// 判断是否有坏的连接
			if (policy.isHaveBadService()) {
				// 重试连接服务器
				retryConnServer(rct.vertx());
			}
		} else {
			// 无可用连接时,结束当前处理器并尝试重新尝试连接是否可用
			if (isNext) {
				// 告诉后置处理器当前操作执行结果
				rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>failedFuture(new ConnectException("无法连接上后台交互的服务器")));
				rct.next();
			} else {
				if (!rct.response().ended()) {
					rct.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
							.putHeader(HttpHeaderConstant.CONTENT_TYPE, api.getContentType()).setStatusCode(api.getResult().getCantConnServerStatus())
							.end(api.getResult().getCantConnServerExample());
				}
			}
			// 重试连接服务器
			retryConnServer(rct.vertx());
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
		checkVxApiParamOptions(serOptions);
		this.policy = new VxApiServerURLPollingPolicy(serOptions.getServerUrls());
	}

	/**
	 * 服务入口的参数检查与路径初始化
	 * 
	 * @param path
	 * @param ser
	 */
	private void checkVxApiParamOptions(VxApiServerEntranceHttpOptions ser) {
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
	/**
	 * 加载请求的参数
	 * 
	 * @param rct
	 *          请求上下文
	 * @param requestPath
	 *          请求url
	 * @param headers
	 *          请求的header
	 * @param queryParams
	 *          请求的queryParams
	 * @param bodyParams
	 *          请求的bodyParams
	 */
	public void loadParam(RoutingContext rct, String requestPath, MultiMap headers, MultiMap queryParams, MultiMap bodyParams) {
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
			} else if (p.getSerParamPosition() == ParamPositionEnum.BODY) {
				bodyParams.add(p.getSerParamName(), param);
			} else {
				queryParams.add(p.getSerParamName(), param);
			}
		}

	}

	/**
	 * 检查参数
	 * 
	 * @param rct
	 * @return
	 */
	private boolean checkParam(RoutingContext rct) {
		boolean flag = true;// 标记参数是否符合要求,符合true,不符合false
		for (VxApiEntranceParam p : api.getEnterParam()) {
			String param = null;
			if (p.getPosition() == ParamPositionEnum.HEADER) {
				param = rct.request().getHeader(p.getParamName());
			} else {
				param = rct.request().getParam(p.getParamName());
			}
			if (param != null) {
				param = param.trim();
			}
			if (param == null || "".equals(param)) {
				if (p.getDef() != null) {
					param = p.getDef().toString();
					if (p.getPosition() == ParamPositionEnum.HEADER) {
						rct.request().headers().add(p.getParamName(), param);
					} else {
						rct.request().params().add(p.getParamName(), param);
					}
				}
			}
			if (p.isNotNull()) {
				if (param == null || "".equals(param) || !StrUtil.isType(param, p.getParamType())) {
					flag = false;
					break;
				}
			}
			if (param == null) {
				continue;
			}
			if (p.getCheckOptions() != null) {
				VxApiParamCheckOptions check = p.getCheckOptions();
				if (check.getMaxLength() != null) {
					if (param.length() > check.getMaxLength()) {
						flag = false;
						break;
					}
				}
				if (check.getMaxValue() != null) {
					if (StrUtil.numberGtNumber(p.getParamType(), param, check.getMaxValue())) {
						flag = false;
						break;
					}
				}
				if (check.getMinValue() != null) {
					if (StrUtil.numberLtNumber(p.getParamType(), param, check.getMinValue())) {
						flag = false;
						break;
					}
				}
				if (check.getRegex() != null) {
					if (!param.matches(check.getRegex())) {
						flag = false;
						break;
					}
				}
				if (check.getEnums() != null) {
					if (!check.getEnums().contains(param)) {
						flag = false;
						break;
					}
				}
			}
		}
		return flag;
	}
	/**
	 * 加载body中参数到query中
	 * 
	 * @param body
	 *          body的内容
	 * @param params
	 *          query参数
	 * @param charset
	 *          body参数的字符编码
	 */
	private void bodyUrlParamToBodyParams(Buffer body, MultiMap params, Charset charset) {
		String data = body.toString();
		if (params == null) {
			params = new CaseInsensitiveHeaders();
		}
		params.addAll(HttpUtils.decoderUriParams(data, charset));
	}
	/**
	 * 当存在坏的后台服务时重试连接后台看后台连接是否可用
	 * 
	 * @param vertx
	 */
	public void retryConnServer(Vertx vertx) {
		if (!policy.isCheckWaiting()) {
			if (webClient == null) {
				webClient = WebClient.create(vertx);
			}
			policy.setCheckWaiting(true);
			vertx.setTimer(serOptions.getRetryTime(), testConn -> {
				List<VxApiServerURLInfo> service = policy.getBadService();
				if (service != null) {
					for (VxApiServerURLInfo urlinfo : service) {
						webClient.requestAbs(serOptions.getMethod(), urlinfo.getUrl()).timeout(serOptions.getTimeOut()).send(res -> {
							if (res.succeeded()) {
								int statusCode = res.result().statusCode();
								if (statusCode != 200) {
									LOG.warn(String.format("应用:%s -> API:%s重试连接后台服务URL,连接成功但得到一个%d状态码,", api.getAppName(), api.getApiName(), statusCode));
								} else {
									if (LOG.isDebugEnabled()) {
										LOG.debug(String.format("应用:%s -> API:%s重试连接后台服务URL,连接成功!", api.getAppName(), api.getApiName()));
									}
								}
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
