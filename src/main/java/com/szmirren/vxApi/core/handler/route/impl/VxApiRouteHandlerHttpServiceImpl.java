package com.szmirren.vxApi.core.handler.route.impl;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.common.VxApiRequestBodyHandler;
import com.szmirren.vxApi.core.entity.VxApiContentType;
import com.szmirren.vxApi.core.entity.VxApiEntranceParam;
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
import com.szmirren.vxApi.core.options.VxApiParamCheckOptions;
import com.szmirren.vxApi.core.options.VxApiParamOptions;
import com.szmirren.vxApi.core.options.VxApiServerEntranceHttpOptions;
import com.szmirren.vxApi.spi.handler.VxApiAfterHandler;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
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
	/** 主体最大的总长度 */
	private long maxContentLength;
	/** 使用有后置处理器 */
	private boolean isNext;
	/** API配置 */
	private VxApis api;
	/** 后端服务策略 */
	private VxApiServerURLPollingPolicy policy;
	/** HTTP/HTTPS服务类型的配置 */
	private VxApiServerEntranceHttpOptions serOptions;
	/** API入口检查参数 */
	private List<VxApiEntranceParam> enterParam;
	/** type=0的前端映射参数 */
	private Map<String, VxApiParamOptions> mapParam;
	/** type=1的系统参数,type=2的透传参数,type=9的自定义参数 */
	private List<VxApiParamOptions> otherReqParam;

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
	public VxApiRouteHandlerHttpServiceImpl(String appName, long maxContentLength, boolean isNext, VxApis api, HttpClient httpClient)
			throws NullPointerException, MalformedURLException {
		super();
		this.thisVertxName = System.getProperty("thisVertxName", "VX-API");
		this.appName = appName;
		this.maxContentLength = maxContentLength;
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
		enterParam = api.getEnterParam();
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
			// 执行监控
			VxApiTrackInfos trackInfo = new VxApiTrackInfos(appName, api.getApiName());
			long contentLength = rct.request().getHeader(VxApiRouteConstant.CONTENT_LENGTH) == null
					? 0L
					: StrUtil.getlongTry(rct.request().getHeader(VxApiRouteConstant.CONTENT_LENGTH));
			if (maxContentLength > 0 && contentLength > maxContentLength) {
				if (LOG.isDebugEnabled()) {
					LOG.info("API:" + api.getApiName() + "接收到请求Content-Length:" + contentLength + ",规定的最大值为:" + maxContentLength);
				}
				rctResponse.setStatusCode(413).setStatusMessage("Request Entity Too Large").end();
				return;
			}
			trackInfo.setRequestBufferLen(contentLength);
			// 用户请求的request
			HttpServerRequest rctRequest = rct.request();
			// 用户请求的Header
			MultiMap rctHeaders = rctRequest.headers();
			if (LOG.isDebugEnabled()) {
				LOG.debug("API:" + api.getApiName() + "接收到Headers:" + rctHeaders.entries());
			}
			// 用户请求的query
			MultiMap rctQuerys = rctRequest.params();
			if (LOG.isDebugEnabled()) {
				LOG.debug("API:" + api.getApiName() + "接收到Querys:" + rctQuerys.entries());
			}
			// 请求后端服务的Future
			Future<Void> requstFuture = Future.future();
			// 用户请求的Content-Type类型
			VxApiContentType uctype = loadContentType(rctRequest);
			// 如果不透传body则判断是否需要将用户的body加载到Query中
			if (!api.isPassBody()) {
				if (api.isBodyAsQuery()) {
					if (uctype.isNullOrUrlencoded()) {
						// 解析用户请求的主体
						VxApiRequestBodyHandler bodyHandler = new VxApiRequestBodyHandler(uctype, maxContentLength);
						rctRequest.handler(body -> {
							if (!bodyHandler.isExceededMaxLen()) {
								bodyHandler.handle(body);
							}
						});
						rctRequest.endHandler(end -> {
							if (bodyHandler.isExceededMaxLen()) {
								if (LOG.isDebugEnabled()) {
									LOG.info(
											"API:" + api.getApiName() + "接收到请求Content-Length大于:" + bodyHandler.getBodyLength() + ",规定的最大值为:" + maxContentLength);
								}
								rctResponse.setStatusCode(413).setStatusMessage("Request Entity Too Large").end();
								return;
							} else {
								if (LOG.isDebugEnabled()) {
									LOG.debug("API:" + api.getApiName() + "接收到Bodys:" + bodyHandler.getBody().entries());
								}
								rctQuerys.addAll(bodyHandler.getBody());
								if (LOG.isDebugEnabled()) {
									LOG.debug("API:" + api.getApiName() + "将body的参数添加到Query后:" + rctQuerys.entries());
								}
								requstFuture.complete();
							}
						});
					}
				}
			} else {
				requstFuture.complete();
			}
			// 请求后端服务的Future处理器
			requstFuture.setHandler(future -> {
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
				// 请求服务的Header
				MultiMap reqHeaderParam = new CaseInsensitiveHeaders();
				// 请求服务的Query参数
				QueryStringEncoder reqQueryParam = new QueryStringEncoder("");
				// 请求服务的Body参数
				QueryStringEncoder reqBodyParam = new QueryStringEncoder("");
				// 该参数为mapParam的copy,用于参数检查时将已经检查的参数添加请求服务的Query参数中,可以省去两次装载参数
				if (mapParam != null) {
					Map<String, VxApiParamOptions> thisMapParam = new HashMap<>(mapParam);
					// 检查入口参数是否符合要求
					boolean checkResult = checkEnterParamAndLoadRequestMapParam(rctHeaders, rctQuerys, thisMapParam, reqURL, reqHeaderParam,
							reqQueryParam, reqBodyParam);
					if (!checkResult) {
						// 参数检查不符合要求,结束请求
						if (LOG.isDebugEnabled()) {
							LOG.debug("API:" + api.getApiName() + "执行参数检查-->结果:检查不通过!");
						}
						rctResponse.putHeader(VxApiRouteConstant.DATE, StrUtil.getRfc822DateFormat(new Date()))
								.setStatusCode(api.getResult().getApiEnterCheckFailureStatus()).end(api.getResult().getApiEnterCheckFailureExample());
						return;
					}
					if (thisMapParam != null && !thisMapParam.isEmpty()) {
						loadRequestMapParams(thisMapParam, rctHeaders, rctQuerys, reqURL, reqHeaderParam, reqQueryParam, reqBodyParam);
					}
				}
				if (otherReqParam != null && !otherReqParam.isEmpty()) {
					loadOtherRequestParams(rct, rctHeaders, rctQuerys, reqURL, reqHeaderParam, reqQueryParam, reqBodyParam);
				}
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
						// 提交连接请求失败
						if (LOG.isDebugEnabled()) {
							LOG.debug("URL:" + urlInfo.getUrl() + ",下标:" + urlInfo.getIndex() + " 请求失败,已经提交给连接策略");
						}
						policy.reportBadService(urlInfo.getIndex());
						trackInfo.setEndTime(Instant.now());
						// 记录与后台交互发生错误
						trackInfo.setSuccessful(false);
						trackInfo.setErrMsg(e.getMessage());
						trackInfo.setErrStackTrace(e.getStackTrace());
						rct.vertx().eventBus().send(thisVertxName + VxApiEventBusAddressConstant.SYSTEM_PLUS_TRACK_INFO, trackInfo.toJson());
						// 如果是连接异常返回无法连接的错误信息,其他异常返回相应的异常
						try {
							if (e instanceof ConnectException || e instanceof TimeoutException) {
								rctResponse.putHeader(VxApiRouteConstant.DATE, StrUtil.getRfc822DateFormat(new Date()))
										.setStatusCode(api.getResult().getCantConnServerStatus()).end(api.getResult().getCantConnServerExample());
							} else {
								rctResponse.putHeader(VxApiRouteConstant.DATE, StrUtil.getRfc822DateFormat(new Date()))
										.setStatusCode(api.getResult().getFailureStatus()).end(api.getResult().getFailureExample());
							}
						} catch (Exception e1) {
							LOG.error("在请求后台服务的异常处理器中响应用户请求-->异常:", e1);
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
					});
					rctRequest.endHandler(end -> {
						request.end();
					});
				} else {
					request.end(reqBodyParam.toString().replace("?", ""));
				}
				if (policy.isHaveBadService()) {
					LOG.warn(
							String.format("应用:%s -> API:%s,后台服务存在不可用的后台服务URL,VX将以设定的重试时间:%d进行重试", appName, api.getApiName(), serOptions.getRetryTime()));
					// 进入重试连接后台服务
					retryConnServer(rct.vertx());
				}
			});
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
				// 初始化参数
				if (p.getType() != 0 && otherReqParam == null) {
					otherReqParam = new ArrayList<>();
				}
				if (p.getType() == 0 && mapParam == null) {
					mapParam = new HashMap<>();
				}
				// 添加请求参数
				if (p.getType() == 0) {
					if (serIsNull || apiIsNull) {
						throw new NullPointerException("参数映射名字与位置不能为null");
					}
					mapParam.put(p.getApiParamName(), p);
				} else if (p.getType() == 1) {
					if (p.getSysParamType() == null || serIsNull) {
						throw new NullPointerException("系统参数的名字,请求名字与位置不能为null");
					}
					otherReqParam.add(p);
				} else if (p.getType() == 2) {
					if (serIsNull || apiIsNull) {
						throw new NullPointerException("透传参数名字与位置不能为null");
					}
					otherReqParam.add(p);
				} else if (p.getType() == 9) {
					if (p.getParamValue() == null || serIsNull) {
						throw new NullPointerException("自定义常量参数值,请求名字与位置不能为null");
					}
					otherReqParam.add(p);
				}
			}
		}
	}
	/**
	 * 加载Content-Type类型
	 * 
	 * @param request
	 * @return
	 */
	private VxApiContentType loadContentType(HttpServerRequest request) {
		String contentType = request.headers().get(VxApiRouteConstant.CONTENT_TYPE);
		if (contentType == null) {
			if (request.headers().get(VxApiRouteConstant.CONTENT_TYPE.toLowerCase()) == null) {
				return new VxApiContentType(null);
			} else {
				contentType = request.headers().get(VxApiRouteConstant.CONTENT_TYPE.toLowerCase());
			}
		}
		return new VxApiContentType(contentType);
	}
	/**
	 * 检查API入口参数并加载服务入口参数
	 * 
	 * @param rctHeaders
	 *          用户请求的Header
	 * @param rctQuerys
	 *          用户请求的Query
	 * @param reqURL
	 *          请求后端服务的URL
	 * @param reqHeaderParam
	 *          请求后端服务的Header
	 * @param reqQueryParam
	 *          请求后端服务的QueryParam
	 * @param reqBodyParam
	 *          请求后端服务的BodyParam
	 * @return 检查符合返回true,检查不符合返回false
	 */
	private boolean checkEnterParamAndLoadRequestMapParam(MultiMap rctHeaders, MultiMap rctQuerys,
			Map<String, VxApiParamOptions> thisMapParam, String reqURL, MultiMap reqHeaderParam, QueryStringEncoder reqQueryParam,
			QueryStringEncoder reqBodyParam) {
		if (enterParam == null || enterParam.isEmpty()) {
			return true;
		}
		for (VxApiEntranceParam enter : enterParam) {
			// 获取参数值
			String param = null;
			if (enter.getPosition() == ParamPositionEnum.HEADER) {
				param = rctHeaders.get(enter.getParamName());
			} else {
				param = rctQuerys.get(enter.getParamName());
			}
			if (param != null) {
				param = param.trim();
			}
			if (param == null || "".equals(param)) {
				if (enter.getDef() != null) {
					param = enter.getDef().toString();
					if (enter.getPosition() == ParamPositionEnum.HEADER) {
						rctHeaders.add(enter.getParamName(), param);
					} else {
						rctQuerys.add(enter.getParamName(), param);
					}
				}
			}
			// 检查参数
			if (enter.isNotNull()) {
				if (StrUtil.isNullOrEmpty(param) || !StrUtil.isType(param, enter.getParamType())) {
					return false;
				}
			}
			if (param == null) {
				continue;
			}
			if (enter.getCheckOptions() != null) {
				VxApiParamCheckOptions check = enter.getCheckOptions();
				if (check.getMaxLength() != null) {
					if (param.length() > check.getMaxLength()) {
						return false;
					}
				}
				if (check.getMaxValue() != null) {
					if (StrUtil.numberGtNumber(enter.getParamType(), param, check.getMaxValue())) {
						return false;
					}
				}
				if (check.getMinValue() != null) {
					if (StrUtil.numberLtNumber(enter.getParamType(), param, check.getMinValue())) {
						return false;
					}
				}
				if (check.getRegex() != null) {
					if (!param.matches(check.getRegex())) {
						return false;
					}
				}
				if (check.getEnums() != null) {
					if (!check.getEnums().contains(param)) {
						return false;
					}
				}
			}
			// 添加用户请求到后台服务的参数
			if (thisMapParam == null || thisMapParam.isEmpty()) {
				continue;
			}
			VxApiParamOptions options = thisMapParam.remove(enter.getParamName());
			if (options == null) {
				continue;
			}
			loadParams(options.getSerParamPosition(), options.getSerParamName(), param, reqURL, reqHeaderParam, reqQueryParam, reqBodyParam);
		}
		return true;
	}
	/**
	 * 加载用户请求映射到后台的参数
	 * 
	 * @param rctHeaders
	 *          用户请求的Header
	 * @param rctQuerys
	 *          用户请求的Query
	 * @param reqURL
	 *          请求后端服务的URL
	 * @param reqHeaderParam
	 *          请求后端服务的Header
	 * @param reqQueryParam
	 *          请求后端服务的QueryParam
	 * @param reqBodyParam
	 *          请求后端服务的BodyParam
	 */
	private void loadRequestMapParams(Map<String, VxApiParamOptions> thisMapParam, MultiMap rctHeaders, MultiMap rctQuerys, String reqURL,
			MultiMap reqHeaderParam, QueryStringEncoder reqQueryParam, QueryStringEncoder reqBodyParam) {
		if (thisMapParam == null || thisMapParam.isEmpty()) {
			return;
		}
		for (VxApiParamOptions options : thisMapParam.values()) {
			String param;
			if (options.getApiParamPosition() == ParamPositionEnum.HEADER) {
				param = rctHeaders.get(options.getApiParamName());
			} else {
				param = rctQuerys.get(options.getApiParamName());
			}
			if (param == null) {
				continue;
			}
			loadParams(options.getSerParamPosition(), options.getSerParamName(), param, reqURL, reqHeaderParam, reqQueryParam, reqBodyParam);
		}
	}
	/**
	 * 加载用户请求后台非映射的参数
	 * 
	 * @param rct
	 *          用户请求的上下文
	 * @param rctHeaders
	 *          用户请求的Header
	 * @param rctQuerys
	 *          用户请求的Query
	 * @param reqURL
	 *          请求后端服务的URL
	 * @param reqHeaderParam
	 *          请求后端服务的Header
	 * @param reqQueryParam
	 *          请求后端服务的QueryParam
	 * @param reqBodyParam
	 *          请求后端服务的BodyParam
	 */
	private void loadOtherRequestParams(RoutingContext rct, MultiMap rctHeaders, MultiMap rctQuerys, String reqURL, MultiMap reqHeaderParam,
			QueryStringEncoder reqQueryParam, QueryStringEncoder reqBodyParam) {
		if (otherReqParam == null || otherReqParam.isEmpty()) {
			return;
		}
		for (VxApiParamOptions options : otherReqParam) {
			String param = null;
			if (options.getType() == 1) {
				if (options.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_HOST) {
					param = rct.request().remoteAddress().host();
				} else if (options.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_PORT) {
					param = Integer.toString(rct.request().remoteAddress().port());
				} else if (options.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_PATH) {
					param = rct.request().path() == null ? "" : rct.request().path();
				} else if (options.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_SESSION_ID) {
					param = rct.session().id();
				} else if (options.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_ABSOLUTE_URI) {
					param = rct.request().absoluteURI();
				} else if (options.getSysParamType() == ParamSystemVarTypeEnum.CLIENT_REQUEST_SCHEMA) {
					param = rct.request().scheme();
				} else if (options.getSysParamType() == ParamSystemVarTypeEnum.SERVER_API_NAME) {
					param = api.getApiName();
				} else if (options.getSysParamType() == ParamSystemVarTypeEnum.SERVER_UNIX_TIME) {
					param = Long.toString(System.currentTimeMillis());
				} else if (options.getSysParamType() == ParamSystemVarTypeEnum.SERVER_USER_AGENT) {
					param = VxApiGatewayAttribute.VX_API_USER_AGENT;
				}
			} else if (options.getType() == 2) {
				if (options.getApiParamPosition() == ParamPositionEnum.HEADER) {
					param = rctHeaders.get(options.getApiParamName());
				} else {
					param = rctQuerys.get(options.getApiParamName());
				}
			} else if (options.getType() == 9) {
				param = options.getParamValue().toString();
			}
			if (param == null) {
				continue;
			}
			loadParams(options.getSerParamPosition(), options.getSerParamName(), param, reqURL, reqHeaderParam, reqQueryParam, reqBodyParam);
		}
	}
	/**
	 * 添加请求参数
	 * 
	 * @param position
	 *          参数位置
	 * @param name
	 *          参数的名称
	 * @param value
	 *          参数的值
	 * @param reqURL
	 *          请求后端服务的URL
	 * @param reqHeaderParam
	 *          请求后端服务的Header
	 * @param reqQueryParam
	 *          请求后端服务的QueryParam
	 * @param reqBodyParam
	 *          请求后端服务的BodyParam
	 */
	private void loadParams(ParamPositionEnum position, String name, String value, String reqURL, MultiMap reqHeaderParam,
			QueryStringEncoder reqQueryParam, QueryStringEncoder reqBodyParam) {
		if (StrUtil.isNullOrEmpty(name, value)) {
			return;
		}
		if (position == ParamPositionEnum.QUERY) {
			reqQueryParam.addParam(name, value);
		} else if (position == ParamPositionEnum.HEADER) {
			reqHeaderParam.add(name, value);
		} else if (position == ParamPositionEnum.BODY) {
			reqBodyParam.addParam(name, value);
		} else if (position == ParamPositionEnum.PATH) {
			reqURL = reqURL.replace(":" + name, value);
		}

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
									LOG.warn(String.format("应用:%s -> API:%s重试连接后台服务URL,连接成功但得到一个%d状态码,", appName, api.getApiName(), statusCode));
								} else {
									if (LOG.isDebugEnabled()) {
										LOG.debug(String.format("应用:%s -> API:%s重试连接后台服务URL,连接成功!", appName, api.getApiName()));
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
