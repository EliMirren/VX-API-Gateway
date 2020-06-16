package com.szmirren.vxApi.core.handler.route.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.entity.VxApiAPILimit;
import com.szmirren.vxApi.core.entity.VxApis;
import com.szmirren.vxApi.core.handler.route.VxApiRouteConstant;
import com.szmirren.vxApi.core.handler.route.VxApiRouteHandlerApiLimit;

import io.vertx.ext.web.RoutingContext;

/**
 * VxApi的访问限制的实现
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiRouteApiLimitImpl implements VxApiRouteHandlerApiLimit {
	// API的相关配置
	private VxApis api;
	// 流量限制
	private VxApiAPILimit limit;

	public VxApiRouteApiLimitImpl(VxApis api) {
		super();
		this.api = api;
		if (api.getLimitUnit() != null) {
			if (api.getApiLimit() <= -1 && api.getIpLimit() <= -1) {
				api.setLimitUnit(null);
			}
		}
	}

	@Override
	public void handle(RoutingContext rct) {
		if (api.getLimitUnit() == null) {
			rct.next();
		} else {
			if (limit != null) {
				if (limit.getIpTop() > -1) {
					Map<String, Long> ipPoints = limit.getUserIpCurPoints();
					String host = rct.request().remoteAddress().host();
					if (ipPoints.get(host) == null) {
						ipPoints.put(host, 1L);
					}
					if (ipPoints.get(host) >= limit.getIpTop()) {
						Instant oldTime = limit.getTimePoints().plusSeconds(api.getLimitUnit().getVal());
						Duration between = Duration.between(Instant.now(), oldTime);
						if (between.getSeconds() > 0) {
							if (!rct.response().ended()) {
								rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
										.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType()).setStatusCode(api.getResult().getLimitStatus())
										.end(api.getResult().getLimitExample());
							}
							return;
						} else {
							VxApiAPILimit newLimit = new VxApiAPILimit(api.getIpLimit(), api.getApiLimit());
							newLimit.setCurPoint(1);
							newLimit.addUserIpCurPotints(host, 1L);
							limit = newLimit;
							rct.next();
							return;
						}
					} else {
						ipPoints.put(host, ipPoints.get(host) + 1);
						limit.setCurPoint(limit.getCurPoint() + 1);
						rct.next();
						return;
					}
				}
				if (limit.getApiTop() > -1) {
					if (limit.getCurPoint() >= limit.getApiTop()) {
						Instant oldTime = limit.getTimePoints().plusSeconds(api.getLimitUnit().getVal());
						Duration between = Duration.between(Instant.now(), oldTime);
						if (between.getSeconds() > 0) {
							if (!rct.response().ended()) {
								rct.response().putHeader(VxApiRouteConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
										.putHeader(VxApiRouteConstant.CONTENT_TYPE, api.getContentType()).setStatusCode(api.getResult().getLimitStatus())
										.end(api.getResult().getLimitExample());
							}
							return;
						} else {
							VxApiAPILimit newLimit = new VxApiAPILimit(api.getIpLimit(), api.getApiLimit());
							newLimit.setCurPoint(1);
							if (api.getIpLimit() != -1) {
								String host = rct.request().remoteAddress().host();
								newLimit.addUserIpCurPotints(host, 1L);
							}
							limit = newLimit;
							rct.next();
							return;
						}
					} else {
						limit.setCurPoint(limit.getCurPoint() + 1);
						rct.next();
						return;
					}
				}
				rct.next();
			} else {
				VxApiAPILimit newLimit = new VxApiAPILimit(api.getIpLimit(), api.getApiLimit());
				newLimit.setCurPoint(1);
				if (api.getIpLimit() != -1) {
					String host = rct.request().remoteAddress().host();
					newLimit.addUserIpCurPotints(host, 1L);
				}
				limit = newLimit;
				rct.next();
			}
		}
	}

}
