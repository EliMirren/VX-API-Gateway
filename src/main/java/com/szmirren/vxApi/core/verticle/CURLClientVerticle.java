package com.szmirren.vxApi.core.verticle;

import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.auth.VxApiClientJsonAuth;
import com.szmirren.vxApi.core.auth.VxApiRolesConstant;
import com.szmirren.vxApi.core.common.ResultFormat;
import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.enums.ContentTypeEnum;
import com.szmirren.vxApi.core.enums.HTTPStatusCodeMsgEnum;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;

/**
 * VX-API的CURL客户端Verticle,
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class CURLClientVerticle extends AbstractVerticle {
	private static final Logger LOG = LogManager.getLogger(CURLClientVerticle.class);

	/**
	 * 返回的CONTENT_TYPE值JSON
	 */
	private final String CONTENT_VALUE_JSON_UTF8 = ContentTypeEnum.JSON_UTF8.val();
	/**
	 * 当前Vertx的唯一标识
	 */
	private String thisVertxName;

	@Override
	public void start(Future<Void> fut) throws Exception {
		thisVertxName = System.getProperty("thisVertxName", "VX-API");
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.route().handler(ResponseContentTypeHandler.create());
		// 通过curl的方式创建网关应用
		router.route("/curl/findAPP").produces(CONTENT_VALUE_JSON_UTF8).handler(this::findAPPbyCURL);
		vertx.createHttpServer().requestHandler(router::accept).listen(5053, res -> {
			if (res.succeeded()) {
				System.out.println("The VX-API console running on port 5053");
				fut.complete();
			} else {
				fut.fail(res.cause());
			}
		});
	}

	/**
	 * 查看所有应用网关,通过curl的方式
	 * 
	 * @param rct
	 */
	public void findAPPbyCURL(RoutingContext rct) {
		String user = rct.request().getParam("user");
		String pwd = rct.request().getParam("pwd");
		LOG.info(MessageFormat.format("[ip: {0},user: {1}] 访问curl模式->查看所有应用网关", rct.request().remoteAddress(), user));
		if (StrUtil.isNullOrEmpty(user, pwd)) {
			JsonObject authInfo = new JsonObject();
			authInfo.put(VxApiRolesConstant.USER_NAME_KEY, user);
			authInfo.put(VxApiRolesConstant.USER_PWD_KEY, pwd);
			authInfo.put(VxApiRolesConstant.USER_ROLE_KEY, VxApiRolesConstant.READ);
			Future.<Boolean>future(auth -> {
				// 进行用户权限认证
				VxApiClientJsonAuth.auth(authInfo, vertx, auth);
			}).compose((auth) -> Future.<JsonArray>future(findApp -> {
				LOG.info("[user: " + user + "] 访问curl模式->查看所有应用网关-->鉴权结果: " + auth);
				// 检查用户权限并查询所有应用网关
				if (auth) {
					vertx.eventBus().<JsonArray>send(thisVertxName + VxApiEventBusAddressConstant.FIND_APP, null, reply -> {
						if (reply.succeeded()) {
							findApp.complete(reply.result().body());
						} else {
							findApp.fail(reply.cause());
						}
					});
				} else {
					rct.response().end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C401));
					findApp.fail("VX-API Manual response");
				}
			})).setHandler(res -> {
				LOG.info("[user:" + user + "] 访问curl模式->查看所有应用网关-->执行结果: " + res.succeeded());
				// 统一失败处理与创建文件是否成功!
				if (res.succeeded()) {
					JsonArray result = res.result();
					rct.response().end(ResultFormat.format(HTTPStatusCodeMsgEnum.C200, result));
				} else {
					if (res.cause() != null && !(res.cause().getMessage().contains("VX-API Manual response"))) {
						LOG.error("[user:" + user + "] 访问curl模式->查看所有应用网关-->异常:" + res.cause());
					}
					if (res.cause() == null) {
						rct.response().end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1000));
					} else if (res.cause().getMessage().contains("VX-API Manual response")) {
					} else {
						rct.response().end(ResultFormat.format(HTTPStatusCodeMsgEnum.C500, res.cause().toString()));
					}
				}
			});
		} else {
			rct.response().end(ResultFormat.formatAsZero(HTTPStatusCodeMsgEnum.C1400));
		}
	}

	/**
	 * 读取文件JsonObject文件并返回
	 * 
	 * @param path
	 * @param handler
	 */
	public void readFile(String path, Handler<AsyncResult<JsonObject>> handler) {
		vertx.fileSystem().readFile(path, fileRes -> {
			try {
				JsonObject result = fileRes.result().toJsonObject();
				handler.handle(Future.<JsonObject>succeededFuture(result));
			} catch (Exception e) {
				handler.handle(Future.failedFuture(e));
			}
		});
	}

}
