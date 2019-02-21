package com.szmirren.vxApi.core.handler.route.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.common.VxApiRequestBodyHandler;
import com.szmirren.vxApi.core.entity.VxApiContentType;
import com.szmirren.vxApi.core.entity.VxApiEntranceParam;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.enums.LoadBalanceEnum;
import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.core.enums.ParamSystemVarTypeEnum;
import com.szmirren.vxApi.core.handler.route.VxApiRouteConstant;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerParamCheck;
import com.szmirren.vxApi.core.options.VxApiParamCheckOptions;
import com.szmirren.vxApi.core.options.VxApiParamOptions;
import com.szmirren.vxApi.core.options.VxApiServerEntranceHttpOptions;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * VxApiRoute参数检查处理器的实现类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiRouteHandlerParamCheckImpl implements VxApiRouteHandlerParamCheck {
	private static final Logger LOG = LogManager.getLogger(VxApiRouteHandlerParamCheckImpl.class);
	/** API配置 */
	private VxApis api;
	/** 主体最大的总长度 */
	private long maxContentLength;
	/** HTTP/HTTPS服务类型的配置 */
	private VxApiServerEntranceHttpOptions serOptions;
	/** API入口检查参数 */
	private List<VxApiEntranceParam> enterParam;
	/** type=0的前端映射参数 */
	private Map<String, VxApiParamOptions> mapParam;
	/** type=1的系统参数,type=2的透传参数,type=9的自定义参数 */
	private List<VxApiParamOptions> otherReqParam;

	public VxApiRouteHandlerParamCheckImpl(VxApis api, long maxContentLength) {
		super();
		this.api = api;
		this.maxContentLength = maxContentLength;
		serOptions = VxApiServerEntranceHttpOptions.fromJson(api.getServerEntrance().getBody());
		if (serOptions == null) {
			throw new NullPointerException("HTTP/HTTPS服务类型的配置文件无法装换为服务类");
		}
		// 初始化参数并检查是否符合要求
		initVxApiParamOptions(serOptions);
		enterParam = api.getEnterParam();
		if (serOptions.getBalanceType() == null) {
			serOptions.setBalanceType(LoadBalanceEnum.POLLING_AVAILABLE);
		}

	}

	@Override
	public void handle(RoutingContext rct) {
		// 当前服务的response
		HttpServerResponse rctResponse = rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
				.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType());
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
		// 记录前端的获取的用户数据的长度
		rct.put(VxApiRouteConstant.BODY_KEY_CONTENT_LENGTH, contentLength);
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
		Future<Void> nextFuture = Future.future();
		// 用户请求的Content-Type类型
		VxApiContentType uctype = loadContentType(rctRequest);
		// 如果不透传body则判断是否需要将用户的body加载到Query中
		if (!api.isPassBody()) {
			if (api.isBodyAsQuery() && uctype.isDecodedSupport()) {
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
							LOG.info("API:" + api.getApiName() + "接收到请求Content-Length大于:" + bodyHandler.getBodyLength() + ",规定的最大值为:" + maxContentLength);
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
						nextFuture.complete();
					}
				});
			} else {
				nextFuture.complete();
			}
		} else {
			nextFuture.complete();
		}

		// 请求后端服务的Future处理器
		nextFuture.setHandler(future -> {
			// 请求服务的Path参数
			MultiMap reqPaths = new CaseInsensitiveHeaders();
			// 请求服务的Header参数
			MultiMap reqHeaderParam = new CaseInsensitiveHeaders();
			// 请求服务的Query参数
			QueryStringEncoder reqQueryParam = new QueryStringEncoder("");
			// 请求服务的Body参数
			QueryStringEncoder reqBodyParam = new QueryStringEncoder("");

			// 该参数为mapParam的copy,用于参数检查时将已经检查的参数添加请求服务的Query参数中,可以省去两次装载参数
			if (mapParam != null) {
				Map<String, VxApiParamOptions> thisMapParam = new HashMap<>(mapParam);
				// 检查入口参数是否符合要求
				boolean checkResult = checkEnterParamAndLoadRequestMapParam(rctHeaders, rctQuerys, thisMapParam, reqPaths, reqHeaderParam,
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
					loadRequestMapParams(thisMapParam, rctHeaders, rctQuerys, reqPaths, reqHeaderParam, reqQueryParam, reqBodyParam);
				}
			}
			if (otherReqParam != null && !otherReqParam.isEmpty()) {
				loadOtherRequestParams(rct, rctHeaders, rctQuerys, reqPaths, reqHeaderParam, reqQueryParam, reqBodyParam);
			}
			// 添加Path参数到RoutingContext
			rct.put(VxApiRouteConstant.BODY_KEY_PATH_TYPE_MultiMap, reqPaths);
			// 添加Header参数到RoutingContext
			rct.put(VxApiRouteConstant.BODY_KEY_HEADER_TYPE_MultiMap, reqHeaderParam);
			// 添加Query参数到RoutingContext
			rct.put(VxApiRouteConstant.BODY_KEY_QUERY_TYPE_QueryStringEncoder, reqQueryParam);
			// 添加Body参数到RoutingContext
			rct.put(VxApiRouteConstant.BODY_KEY_BODY_TYPE_QueryStringEncoder, reqBodyParam);

			// 参数通过放行
			rct.next();
		});
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
	 * @param reqPaths
	 *          请求后端服务的Path参数
	 * @param reqHeaderParam
	 *          请求后端服务的Header
	 * @param reqQueryParam
	 *          请求后端服务的QueryParam
	 * @param reqBodyParam
	 *          请求后端服务的BodyParam
	 * @return 检查符合返回true,检查不符合返回false
	 */
	private boolean checkEnterParamAndLoadRequestMapParam(MultiMap rctHeaders, MultiMap rctQuerys,
			Map<String, VxApiParamOptions> thisMapParam, MultiMap reqPaths, MultiMap reqHeaderParam, QueryStringEncoder reqQueryParam,
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
			loadParams(options.getSerParamPosition(), options.getSerParamName(), param, reqPaths, reqHeaderParam, reqQueryParam, reqBodyParam);
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
	 * @param reqPaths
	 *          Path参数
	 * @param reqHeaderParam
	 *          请求后端服务的Header
	 * @param reqQueryParam
	 *          请求后端服务的QueryParam
	 * @param reqBodyParam
	 *          请求后端服务的BodyParam
	 */
	private void loadRequestMapParams(Map<String, VxApiParamOptions> thisMapParam, MultiMap rctHeaders, MultiMap rctQuerys, MultiMap reqPaths,
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
			loadParams(options.getSerParamPosition(), options.getSerParamName(), param, reqPaths, reqHeaderParam, reqQueryParam, reqBodyParam);
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
	 * @param reqPaths
	 *          请求后端的Path参数
	 * @param reqHeaderParam
	 *          请求后端服务的Header
	 * @param reqQueryParam
	 *          请求后端服务的QueryParam
	 * @param reqBodyParam
	 *          请求后端服务的BodyParam
	 */
	private void loadOtherRequestParams(RoutingContext rct, MultiMap rctHeaders, MultiMap rctQuerys, MultiMap reqPaths,
			MultiMap reqHeaderParam, QueryStringEncoder reqQueryParam, QueryStringEncoder reqBodyParam) {
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
			loadParams(options.getSerParamPosition(), options.getSerParamName(), param, reqPaths, reqHeaderParam, reqQueryParam, reqBodyParam);
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
	 * @param reqPaths
	 *          请求后端的Path的参数
	 * @param reqHeaderParam
	 *          请求后端服务的Header
	 * @param reqQueryParam
	 *          请求后端服务的QueryParam
	 * @param reqBodyParam
	 *          请求后端服务的BodyParam
	 */
	private void loadParams(ParamPositionEnum position, String name, String value, MultiMap reqPaths, MultiMap reqHeaderParam,
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
			reqPaths.add(name, value);
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

	public void handles(RoutingContext rct) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("API:" + api.getApiName() + "接收到参数:" + rct.request().params());
		}
		if (api.getEnterParam() == null) {
			rct.next();
		} else {
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
			if (flag) {
				rct.next();
			} else {
				rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
						.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType()).setStatusCode(api.getResult().getApiEnterCheckFailureStatus())
						.end(api.getResult().getApiEnterCheckFailureExample());
			}
		}

	}

}
