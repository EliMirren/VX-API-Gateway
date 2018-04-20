package com.szmirren.vxApi.core.handler.route.impl;

import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.entity.VxApiEntranceParam;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.core.handler.route.VxApiRouteConstant;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerParamCheck;
import com.szmirren.vxApi.core.options.VxApiParamCheckOptions;

import io.vertx.ext.web.RoutingContext;

/**
 * VxApiRoute参数检查处理器的实现类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiRouteHandlerParamCheckImpl implements VxApiRouteHandlerParamCheck {
	private VxApis api;

	public VxApiRouteHandlerParamCheckImpl(VxApis api) {
		super();
		this.api = api;
	}

	@Override
	public void handle(RoutingContext rct) {
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
				if (param==null) {
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
