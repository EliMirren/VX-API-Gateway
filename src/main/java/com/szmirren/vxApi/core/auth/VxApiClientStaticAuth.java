package com.szmirren.vxApi.core.auth;

import java.util.Base64;

import com.szmirren.vxApi.core.common.ResultFormat;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.enums.HTTPStatusCodeMsgEnum;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * 使用json进行相应操作的权限认证
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiClientStaticAuth implements Handler<RoutingContext> {
	public static final String AUTHORIZATION = "Authorization";
	public static final String IS_AUTH = "isAuth";

	public static VxApiClientStaticAuth create() {
		return new VxApiClientStaticAuth();
	}

	@Override
	public void handle(RoutingContext event) {
		if (event.session().get(AUTHORIZATION) != null) {
			VxApiClientAuthUser user = new VxApiClientAuthUser();
			user.setPrincipal(event.session().get(AUTHORIZATION));
			event.setUser(user);
			event.next();
		} else {
			String auth = event.request().getHeader(AUTHORIZATION);
			if (auth == null) {
				event.response()
						.putHeader("WWW-Authenticate", "Basic realm=\"" + VxApiGatewayAttribute.FULL_NAME + "\"")
						.setStatusCode(401).end();
			} else {
				if ("false".equals(event.session().get(IS_AUTH))) {
					event.session().remove(IS_AUTH);
					event.response()
							.putHeader("WWW-Authenticate", "Basic realm=\"" + VxApiGatewayAttribute.FULL_NAME + "\"")
							.setStatusCode(401).end();
				} else {
					try {
						final String suser;
						final String spass;
						String decoded = new String(Base64.getDecoder().decode(auth.split(" ")[1]));
						int colonIdx = decoded.indexOf(":");
						if (colonIdx != -1) {
							suser = decoded.substring(0, colonIdx);
							spass = decoded.substring(colonIdx + 1);
						} else {
							suser = decoded;
							spass = null;
						}
						VxApiUserAuthUtil.auth(suser, spass, event.vertx(), res -> {
							if (res.succeeded()) {
								JsonObject u = res.result();
								if (u != null) {
									event.session().put("userName", suser);
									event.session().put(IS_AUTH, "true");
									event.session().put(AUTHORIZATION, u);
									VxApiClientAuthUser user = new VxApiClientAuthUser();
									user.setPrincipal(u);
									event.setUser(user);
									event.next();
								} else {
									event.response().end(ResultFormat.formatAsNull(HTTPStatusCodeMsgEnum.C401));
								}
							} else {
								event.response().end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause()));
							}
						});
					} catch (Exception e) {
						event.response().end(ResultFormat.format(HTTPStatusCodeMsgEnum.C401, e));
					}
				}
			}
		}

	}
}
