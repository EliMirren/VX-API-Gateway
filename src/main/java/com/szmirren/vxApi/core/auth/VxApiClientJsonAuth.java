package com.szmirren.vxApi.core.auth;

import com.szmirren.vxApi.core.common.PathUtil;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

/**
 * 使用json进行相应操作的权限认证
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiClientJsonAuth {
	/**
	 * 验证用户是否有权限
	 * 
	 * @param user
	 * @param vertx
	 * @param handler
	 */
	public static void auth(JsonObject authInfo, Vertx vertx, Handler<AsyncResult<Boolean>> handler) {
		String username = authInfo.getString(VxApiRolesConstant.USER_NAME_KEY);
		if (username == null) {
			handler.handle(Future.succeededFuture(false));
		} else {
			String pwd = authInfo.getString(VxApiRolesConstant.USER_PWD_KEY);
			String role = authInfo.getString(VxApiRolesConstant.USER_ROLE_KEY);
			FileSystem file = vertx.fileSystem();
			String path = PathUtil.getPath("user.json");
			file.readFile(path, res -> {
				if (res.succeeded()) {
					JsonObject users = res.result().toJsonObject();
					if (users.getValue(username) instanceof JsonObject) {
						JsonObject user = users.getJsonObject(username);
						if (pwd != null && pwd.equals(user.getString("pwd"))
								&& user.getJsonArray("roles").contains(role)) {
							handler.handle(Future.<Boolean>succeededFuture(true));
						} else {
							handler.handle(Future.<Boolean>succeededFuture(false));
						}
					} else {
						handler.handle(Future.<Boolean>succeededFuture(false));
					}
				} else {
					handler.handle(Future.failedFuture(res.cause()));
				}
			});
		}
	}
}
