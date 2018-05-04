package com.szmirren.vxApi.core.handler.route.impl;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
import com.szmirren.vxApi.core.enums.ContentTypeEnum;
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
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
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
	/** 日志处理 */
	private static final Logger LOG = LogManager.getLogger(VxApiRouteHandlerHttpServiceImpl.class);

	/** 当前Vertx的唯一标识 */
	private String thisVertxName;
	/** 应用的名称 */
	private String appName;
	/** 使用有后置处理器 */
	private boolean isNext;
	/** API配置 */
	private VxApis api;
	/** 后端服务策略 */
	private VxApiServerURLPollingPolicy policy;
	/** HTTP/HTTPS服务类型的配置 */
	private VxApiServerEntranceHttpOptions serOptions;
	/** type=0的前端映射参数 */
	private List<VxApiParamOptions> mapParam;
	/** type=1的系统参数 */
	private List<VxApiParamOptions> sysParam;
	/** type=2的透传参数 */
	private List<VxApiParamOptions> passParam;
	/** type=9的自定义参数 */
	private List<VxApiParamOptions> customParam;

	/** HTTP客户端 */
	private HttpClient httpClient;
	/** 简化HTTP请求的WebClient */
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
		serOptions = VxApiServerEntranceHttpOptions.fromJson(api.getServerEntrance().getBody());
		if (serOptions == null) {
			throw new NullPointerException("HTTP/HTTPS服务类型的配置文件无法装换为服务类");
		}
		List<VxApiServerURL> urls = serOptions.getServerUrls();
		if (urls == null || urls.size() < 1) {
			throw new NullPointerException("服务端地址不存在");
		}
		// 初始化参数并检查是否符合要求
		initVxApiParamOptions(serOptions);
		if (serOptions.getBalanceType() == null) {
			serOptions.setBalanceType(LoadBalanceEnum.POLLING_AVAILABLE);
		}
		policy = new VxApiServerURLPollingPolicy(urls);
	}

	@Override
	public void handle(RoutingContext rct) {
		// 当前服务的response
		HttpServerResponse rctResponse = rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
				.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType());
		// 判断后台服务是否有可用连接,有可用连接进行请求,如果没有可用连接进行重试
		if (policy.isHaveService()) {
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

			String contentLength = rct.request().getHeader("Content-Length");
			trackInfo.setRequestBufferLen(contentLength == null ? 0 : StrUtil.getintTry(contentLength));
			// 获得请求连接与进行请求
			String requestPath = urlInfo.getUrl();
			HttpServerRequest rctRequest = rct.request();
			MultiMap rctHeaders = rctRequest.headers();
			MultiMap rctQuery = rctRequest.params();
			// 用户请求的Content-Type类型
			int uctype = getHeaderContentTypeAndJudge(rctRequest);
			if (uctype == 0 || uctype == 1) {
				if (api.isBodyAsQuery()) {

				}
			}

		} else {
			// 进入该地方证明没有可用链接,结束当前处理器并尝试重新尝试连接是否可用
			if (isNext) {
				rct.put(VxApiAfterHandler.PREV_IS_SUCCESS_KEY, Future.<Boolean>failedFuture(new ConnectException("无法连接上后台交互的服务器")));
				rct.next();
			} else {
				rctResponse.setStatusCode(api.getResult().getCantConnServerStatus()).end(api.getResult().getCantConnServerExample());
			}
			LOG.warn(
					String.format("应用:%s -> API:%s,后台服务已不存在可用的后台服务URL,VX将以设定的重试时间:%d进行重试", appName, api.getApiName(), serOptions.getRetryTime()));
			// 进入重试连接后台服务
			retryConnServer(rct.vertx());
		}
	}
	/**
	 * 获得Content-Type并判断Content-Type是什么类型,<br>
	 * 0=Content-Type=null<br>
	 * 1=application/x-www-form-urlencoded<br>
	 * 2=multipart/form-data<br>
	 * 
	 * @param headers
	 * @return
	 */
	public int getHeaderContentTypeAndJudge(HttpServerRequest request) {
		String contentType = request.headers().get(ContentTypeEnum.CONTENT_TYPE.val());
		if (contentType == null) {
			if (request.headers().get(ContentTypeEnum.CONTENT_TYPE.val().toLowerCase()) == null) {
				return 0;
			} else {
				contentType = request.headers().get(ContentTypeEnum.CONTENT_TYPE.val().toLowerCase());
			}
		}
		if (contentType.toLowerCase().startsWith(ContentTypeEnum.MULTIPART_FORM_DATA.val())) {
			return 1;
		}
		if (contentType.toLowerCase().startsWith(ContentTypeEnum.APPLICATION_X_WWW_FORM_URLENCODED.val())) {
			return 2;
		}
		return 0;
	}
	/**
	 * 加载body中url参数
	 * 
	 * @param request
	 */
	public void bodyUrlParamToParams(Buffer body, MultiMap params) {
			
	}

	public void oldhandle(RoutingContext rct) {
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
						webClient = WebClient.create(rct.vertx());
					}
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
				rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
						.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType()).setStatusCode(api.getResult().getCantConnServerStatus())
						.end(api.getResult().getCantConnServerExample());
			}
			if (!policy.isCheckWaiting()) {
				if (webClient == null) {
					webClient = WebClient.create(rct.vertx());
				}
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
	 * 服务入口的参数检查与路径初始化
	 * 
	 * @param path
	 * @param ser
	 */
	private void initVxApiParamOptions(VxApiServerEntranceHttpOptions ser) {
		if (ser.getParams() != null) {
			for (VxApiParamOptions p : ser.getParams()) {
				boolean serIsNull = p.getSerParamName() == null || p.getSerParamPosition() == null;
				boolean apiIsNull = p.getApiParamName() == null || p.getApiParamPosition() == null;
				if (p.getType() == 0) {
					if (serIsNull || apiIsNull) {
						throw new NullPointerException("参数映射名字与位置不能为null");
					}
					if (mapParam == null) {
						mapParam = new ArrayList<>();
					}
					mapParam.add(p);
				} else if (p.getType() == 1) {
					if (p.getSysParamType() == null || serIsNull) {
						throw new NullPointerException("系统参数的名字,请求名字与位置不能为null");
					}
					if (sysParam == null) {
						sysParam = new ArrayList<>();
					}
					sysParam.add(p);
				} else if (p.getType() == 2) {
					if (serIsNull || apiIsNull) {
						throw new NullPointerException("透传参数名字与位置不能为null");
					}
					if (passParam == null) {
						passParam = new ArrayList<>();
					}
					passParam.add(p);
				} else if (p.getType() == 9) {
					if (p.getParamValue() == null || serIsNull) {
						throw new NullPointerException("自定义常量参数值,请求名字与位置不能为null");
					}
					if (customParam == null) {
						customParam = new ArrayList<>();
					}
					customParam.add(p);
				}
			}
		}
	}

	private boolean checkAndLoadHeader() {
		return false;
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
									LOG.warn("应用:%s -> API:%s重试连接后台服务URL,连接成功但得到一个%d状态码,", appName, api.getApiName(), statusCode);
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
