package com.szmirren.vxApi.core.auth;

import com.szmirren.vxApi.core.common.PathUtil;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

/**
 * 用于验证用户是否正确
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiUserAuthUtil {
	/**
	 * 验证用户是否正确,如果正确返回json格式的用户如果不存在返回null
	 * 
	 * @param suser
	 *            用户名字
	 * @param spass
	 *            用户密码
	 * @param vertx
	 *            vertx
	 * @param handler
	 *            结果
	 */
	public static void auth(String suser, String spass, Vertx vertx, Handler<AsyncResult<JsonObject>> handler) {
		FileSystem file = vertx.fileSystem();
		String path = PathUtil.getPathString("user.json");
		file.readFile(path, res -> {
			if (res.succeeded()) {
				JsonObject users = res.result().toJsonObject();
				if (users.getValue(suser) instanceof JsonObject) {
					JsonObject user = users.getJsonObject(suser);
					if (spass.equals(user.getString("pwd"))) {
						handler.handle(Future.<JsonObject>succeededFuture(user));
					} else {
						handler.handle(Future.<JsonObject>succeededFuture(null));
					}
				} else {
					handler.handle(Future.<JsonObject>succeededFuture(null));
				}
			} else {
				handler.handle(Future.failedFuture(res.cause()));
			}
		});
	}
}
