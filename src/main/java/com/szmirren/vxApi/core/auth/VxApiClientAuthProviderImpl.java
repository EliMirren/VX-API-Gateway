package com.szmirren.vxApi.core.auth;

import com.szmirren.vxApi.core.common.PathUtil;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * 客户端通用权限认证的实现
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiClientAuthProviderImpl implements AuthProvider {
	private Vertx vertx;

	public VxApiClientAuthProviderImpl(Vertx vertx) {
		super();
		this.vertx = vertx;
	}

	@Override
	public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
		String username = authInfo.getString("username");
		if (authInfo.getString("username") == null || "".equals(authInfo.getString("username"))) {
			resultHandler.handle(Future.<User>succeededFuture(null));
		} else {
			String pwd = authInfo.getString("password");
			String path = PathUtil.getPath("user.json");
			vertx.fileSystem().readFile(path, res -> {
				if (res.succeeded()) {
					JsonObject users = res.result().toJsonObject();
					if (users.getValue(username) instanceof JsonObject) {
						JsonObject user = users.getJsonObject(username);
						if (pwd != null && pwd.equals(user.getString("pwd"))) {
							VxApiClientAuthUser authUser = new VxApiClientAuthUser();
							authUser.setPrincipal(new JsonObject().put("roles", user.getJsonArray("roles")));
							resultHandler.handle(Future.<User>succeededFuture(authUser));
						} else {
							resultHandler.handle(Future.<User>succeededFuture(null));
						}
					} else {
						resultHandler.handle(Future.<User>succeededFuture(null));
					}
				} else {
					resultHandler.handle(Future.failedFuture("找不到用户配置文件:" + path));
				}
			});
		}
	}

	public Vertx getVertx() {
		return vertx;
	}

	public void setVertx(Vertx vertx) {
		this.vertx = vertx;
	}

}
