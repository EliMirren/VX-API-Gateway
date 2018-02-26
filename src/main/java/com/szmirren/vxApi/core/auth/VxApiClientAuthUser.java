package com.szmirren.vxApi.core.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

/**
 * 客户端通用权限认证的用户
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiClientAuthUser extends AbstractUser {

	private JsonObject principal;

	@Override
	public JsonObject principal() {
		return principal;
	}

	/**
	 * 设置用户附加信息
	 * 
	 * @param principal
	 */
	public void setPrincipal(JsonObject principal) {
		this.principal = principal;
	}

	@Override
	public void setAuthProvider(AuthProvider authProvider) {
	}

	@Override
	protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
		JsonArray roles = principal.getJsonArray("roles");
		resultHandler.handle(Future.<Boolean>succeededFuture((roles != null && roles.contains(permission))));
	}
}
