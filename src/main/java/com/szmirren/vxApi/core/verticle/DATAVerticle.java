package com.szmirren.vxApi.core.verticle;

import java.text.MessageFormat;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.szmirren.vxApi.core.common.PathUtil;
import com.szmirren.vxApi.core.common.StrUtil;
import com.szmirren.vxApi.core.common.VxApiDATAStoreConstant;
import com.szmirren.vxApi.core.common.VxApiEventBusAddressConstant;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * 网关应用与API持久化的Verticle
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class DATAVerticle extends AbstractVerticle {
	private static final Logger LOG = LogManager.getLogger(DATAVerticle.class);

	/**
	 * JDBC客户端
	 */
	private JDBCClient jdbcClient = null;
	/**
	 * 当前Vertx的唯一标识
	 */
	private String thisVertxName;

	@Override
	public void start(Future<Void> fut) throws Exception {
		LOG.info("start DATA Verticle ...");
		thisVertxName = System.getProperty("thisVertxName", "VX-API");
		initShorthand();// 初始化简写后的常量数据
		JsonObject dbConfig = config();
		String url = dbConfig.getString("url");
		if (dbConfig.getString("url").contains("jdbc:sqlite:")) {
			String temp = url.replace("jdbc:sqlite:", "");
			if (temp.indexOf("/") < 0) {
				dbConfig.put("url", "jdbc:sqlite:" + PathUtil.getPathString(temp));
			}
		}
		jdbcClient = JDBCClient.createShared(vertx, dbConfig, VxApiGatewayAttribute.NAME);

		// application operation address
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.FIND_APP, this::findApplication);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.GET_APP, this::getApplication);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.ADD_APP, this::addApplication);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEL_APP, this::delApplication);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.UPDT_APP, this::updtApplication);
		// api operation address
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.FIND_API_ALL, this::findAPI);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.FIND_API_BY_PAGE, this::findAPIByPage);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.GET_API, this::getAPI);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.ADD_API, this::addAPI);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.DEL_API, this::delAPI);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.UPDT_API, this::updtAPI);
		// blacklist
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.FIND_BLACKLIST, this::findBlacklist);
		vertx.eventBus().consumer(thisVertxName + VxApiEventBusAddressConstant.REPLACE_BLACKLIST, this::replaceBlacklist);
		LOG.info("start DATA Verticle successful");
		fut.complete();
	}

	// =============================================
	// =====================网关应用==================
	// =============================================
	/**
	 * 网关应用的表名简写
	 */
	private String APPTN = null;
	/**
	 * 网关应用的主键列简写
	 */
	private String APPIC = null;
	/**
	 * 网关应用json字符串内容的列名简写
	 */
	private String APPCC = null;
	/**
	 * 网关应用通讯传输中获取json中存放主键的属性名简写
	 */
	private String APPIN = null;
	/**
	 * 网关应用通讯传输中获取json中存放json字符串对象的属性名简写
	 */
	private String APPCN = null;

	/**
	 * 获得所有应用程序
	 * 
	 * @param msg
	 */
	public void findApplication(Message<String> msg) {
		String sql = MessageFormat.format("select {0} AS {1} ,{2} AS {3} from {4} ", APPIC, APPIN, APPCC, APPCN, APPTN);
		jdbcClient.query(sql, res -> {
			if (res.succeeded()) {
				List<JsonObject> rows = res.result().getRows();
				if (rows != null) {
					JsonArray array = new JsonArray();
					rows.forEach(va -> {
						if (va.getValue(APPCN) instanceof String) {
							array.add(new JsonObject(va.getString(APPCN)));
						} else if (va.getValue(APPCN) instanceof JsonObject) {
							array.add(va.getJsonObject(APPCN));
						}
					});
					msg.reply(array);
				} else {
					msg.reply(new JsonArray());
				}
			} else {
				msg.fail(500, res.cause().toString());
				LOG.error("执行查询所有应用程序-->失败:" + res.cause().toString());
			}
		});

	}

	/**
	 * 获得一个应用程序,接收应用程序的字符串名字
	 * 
	 * @param msg
	 */
	public void getApplication(Message<String> msg) {
		String name = msg.body();
		if (name == null) {
			msg.fail(411, "the application name is null");
		} else {
			String sql = MessageFormat.format("select {0} AS {1} ,{2} AS {3} from {4} where {0} = ? ", APPIC, APPIN, APPCC, APPCN, APPTN);
			JsonArray params = new JsonArray();
			params.add(name);
			jdbcClient.queryWithParams(sql, params, res -> {
				if (res.succeeded()) {
					List<JsonObject> rows = res.result().getRows();
					JsonObject result = null;
					if (rows != null && rows.size() > 0) {
						result = rows.get(0);
					} else {
						result = new JsonObject();
					}
					msg.reply(result);
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行查询应用程序-->失败:" + res.cause().toString());
				}
			});
		}
	}

	/**
	 * 添加应用程序,接收一个网关应用的json对象,
	 * 
	 * @param msg
	 */
	public void addApplication(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(411, "the application options is null");
		} else {
			JsonObject body = msg.body();
			String sql = MessageFormat.format("insert into {0} ({1},{2}) values(?,?)", APPTN, APPIC, APPCC);
			JsonArray params = new JsonArray();
			params.add(body.getString("appName"));
			params.add(body.getJsonObject("app").toString());
			jdbcClient.updateWithParams(sql, params, res -> {
				if (res.succeeded()) {
					int result = res.result().getUpdated();
					msg.reply(result);
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行添加应用程序-->失败:" + res.cause().toString());
				}
			});
		}
	}

	/**
	 * 删除一个应用程序,接收应用程序的字符串名字
	 * 
	 * @param msg
	 */
	public void delApplication(Message<String> msg) {
		if (msg.body() == null) {
			msg.fail(411, "the application name is null");
		} else {
			String isql = MessageFormat.format("delete from {0} where {1} = ?", APITN, API_APPIC);
			JsonArray iparams = new JsonArray();
			iparams.add(msg.body());
			jdbcClient.updateWithParams(isql, iparams, ires -> {
				if (ires.succeeded()) {
					String sql = MessageFormat.format("delete from {0} where {1} = ?", APPTN, APPIC);
					JsonArray params = new JsonArray();
					params.add(msg.body());
					jdbcClient.updateWithParams(sql, params, res -> {
						if (res.succeeded()) {
							int result = res.result().getUpdated();
							msg.reply(result);
						} else {
							msg.fail(500, res.cause().toString());
							LOG.error("执行删除应用程序-->失败:" + ires.cause().toString());
						}
					});
				} else {
					msg.fail(500, ires.cause().toString());
					LOG.error("执行删除应用程序-->失败:" + ires.cause().toString());
				}
			});

		}
	}

	/**
	 * 修改应用程序,接收一个网关应用的json对象
	 * 
	 * @param msg
	 */
	public void updtApplication(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(411, "the application name is null");
		} else {
			JsonObject body = msg.body();
			String sql = MessageFormat.format("update {0} set {1}= ? where {2} = ?", APPTN, APPCC, APIIC);
			JsonArray params = new JsonArray();
			params.add(body.getJsonObject("app").toString());
			params.add(body.getString("appName"));
			jdbcClient.updateWithParams(sql, params, res -> {
				if (res.succeeded()) {
					int result = res.result().getUpdated();
					msg.reply(result);
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行更新应用程序-->失败:" + res.cause().toString());
				}
			});
		}
	}

	// =============================================
	// =====================APIS====================
	// =============================================
	/**
	 * 存放API的表名简写
	 */
	private String APITN = null;
	/**
	 * 存放API的主键列简写
	 */
	private String APIIC = null;
	/**
	 * 存放API表中网关应用主键的列简写
	 */
	private String API_APPIC = null;
	/**
	 * 存放API,json字符串内容的列名简写
	 */
	private String APICC = null;
	/**
	 * API通讯传输中获取json中存放主键的属性名简写
	 */
	private String APIIN = null;
	/**
	 * API通讯传输中获取json中存放应用网关主键的属性名简写
	 */
	private String API_APPIN = null;
	/**
	 * API通讯传输中获取json中存放json字符串对象的属性名简写
	 */
	private String APICN = null;

	/**
	 * 查看所有API
	 * 
	 * @param msg
	 */
	public void findAPI(Message<JsonObject> msg) {
		if (msg.body() == null || msg.body().getString("appName") == null) {
			msg.fail(411, "the application name is null");
		} else {
			String sql = MessageFormat.format("select {0} AS {1},{2} AS {3},{4} AS {5} from {6} where {2} = ? ", APIIC, APIIN, API_APPIC,
					API_APPIN, APICC, APICN, APITN);
			// 添加从请求中获取添加值并添加到查询条件中
			JsonArray params = new JsonArray();
			params.add(msg.body().getString("appName"));
			jdbcClient.queryWithParams(sql, params, res -> {
				if (res.succeeded()) {
					List<JsonObject> rows = res.result().getRows();
					JsonArray array = new JsonArray();
					rows.forEach(va -> {
						if (va.getValue(APICN) instanceof String) {
							array.add(new JsonObject(va.getString(APICN)));
						} else if (va.getValue(APICN) instanceof JsonObject) {
							array.add(va.getJsonObject(APICN));
						}
					});
					msg.reply(array);
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行查看所有API-->失败:" + res.cause().toString());
				}
			});
		}
	}

	/**
	 * 获得一个应用的所有API,传入应用程序的name,
	 * 
	 * @param msg
	 */
	public void findAPIByPage(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(411, "the application name is null");
		} else {
			JsonObject body = msg.body();
			String countSql = MessageFormat.format("select count({0}) from {1} where {2} = ? ", APIIC, APITN, API_APPIC);
			JsonArray countParams = new JsonArray();
			countParams.add(body.getString("appName"));
			jdbcClient.queryWithParams(countSql, countParams, count -> {
				if (count.succeeded()) {
					Object value = count.result().getResults().get(0).getValue(0);
					long dataCount = ((Number) value).longValue();
					// TODO 如果存储的数据库不支持limit 与offset 需要自己修改一下分页语句
					String sql = MessageFormat.format("select {0} AS {1},{2} AS {3},{4} AS {5} from {6} where {2} = ? limit ? offset ?", APIIC, APIIN,
							API_APPIC, API_APPIN, APICC, APICN, APITN);
					JsonArray params = new JsonArray();
					params.add(body.getString("appName"));
					params.add(body.getInteger("limit"));
					params.add(body.getInteger("offset"));
					jdbcClient.queryWithParams(sql, params, res -> {
						if (res.succeeded()) {
							List<JsonObject> rows = res.result().getRows();
							JsonArray array = new JsonArray();
							rows.forEach(va -> {
								if (va.getValue(APICN) instanceof String) {
									array.add(new JsonObject(va.getString(APICN)));
								} else if (va.getValue(APICN) instanceof JsonObject) {
									array.add(va.getJsonObject(APICN));
								}
							});
							msg.reply(new JsonObject().put("dataCount", dataCount).put("data", array));
						} else {
							msg.fail(500, res.cause().toString());
							LOG.error("执行查看所有API-->失败:" + res.cause().toString());
						}
					});
				} else {
					msg.fail(500, count.cause().toString());
					LOG.error("执行查看所有API-->失败:" + count.cause().toString());
				}
			});
		}
	}

	/**
	 * 获得一个api
	 * 
	 * @param msg
	 */
	public void getAPI(Message<String> msg) {
		String name = msg.body();
		if (name == null) {
			msg.fail(411, "the application name is null");
		} else {
			String sql = MessageFormat.format("select {0} AS {1},{2} AS {3},{4} AS {5} from {6} where {0} = ? ", APIIC, APIIN, API_APPIC,
					API_APPIN, APICC, APICN, APITN);
			JsonArray params = new JsonArray();
			params.add(name);
			jdbcClient.queryWithParams(sql, params, res -> {
				if (res.succeeded()) {
					List<JsonObject> rows = res.result().getRows();
					JsonObject result = null;
					if (rows != null && rows.size() > 0) {
						result = rows.get(0);
					} else {
						result = new JsonObject();
					}
					msg.reply(result);
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行查看API-->失败:" + res.cause().toString());
				}
			});
		}
	}

	/**
	 * 添加API,接收一个json对象,对象包含该app应用appName,网关的json对象api
	 * 
	 * @param msg
	 */
	public void addAPI(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(411, "the application options is null");
		} else {
			JsonObject body = msg.body();
			String appName = body.getString("appName");

			Future<Void> addApiFuture = Future.future();
			// 判断有没有存在该应用后执行
			addApiFuture.setHandler(check -> {
				String sql = MessageFormat.format("insert into {0} ({1},{2},{3}) values(?,?,?)", APITN, APIIC, API_APPIC, APICC);
				JsonArray params = new JsonArray();
				params.add(body.getString("apiName"));
				params.add(appName);
				params.add(body.getJsonObject("api").toString());
				jdbcClient.updateWithParams(sql, params, res -> {
					if (res.succeeded()) {
						int result = res.result().getUpdated();
						msg.reply(result);
					} else {
						msg.fail(500, res.cause().toString());
						LOG.error("执行添加API-->失败:" + res.cause().toString());
					}
				});
			});
			String sql = MessageFormat.format("select {0} AS {1} ,{2} AS {3} from {4} where {0} = ? ", APPIC, APPIN, APPCC, APPCN, APPTN);
			JsonArray params = new JsonArray();
			params.add(appName);
			jdbcClient.queryWithParams(sql, params, res -> {
				if (res.succeeded()) {
					List<JsonObject> rows = res.result().getRows();
					if (rows != null && rows.size() > 0) {
						addApiFuture.complete();
					} else {
						msg.fail(500, "网关应用不存在!");
						LOG.debug("执行添加API->查询应用程序-->失败:网关应用不存在!");
					}
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行添加API->查询应用程序-->失败:" + res.cause().toString());
				}
			});

		}
	}

	/**
	 * 删除一个API,接收应用程序的字符串名字
	 * 
	 * @param msg
	 */
	public void delAPI(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(411, "the msg body is null");
		} else {
			String appName = msg.body().getString("appName");
			String apiName = msg.body().getString("apiName");
			if (StrUtil.isNullOrEmpty(appName, apiName)) {
				msg.fail(411, "the appName or apiName is null");
				return;
			}
			String sql = MessageFormat.format("delete from {0} where {1} = ? and {2} = ? ", APITN, API_APPIC, APIIC);
			JsonArray params = new JsonArray();
			params.add(appName);
			params.add(apiName);
			jdbcClient.updateWithParams(sql, params, res -> {
				if (res.succeeded()) {
					int result = res.result().getUpdated();
					msg.reply(result);
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行删除API-->失败:" + res.cause().toString());
				}
			});
		}
	}

	/**
	 * 修改API,接收一个json对象,其中包含主键name,内容content
	 * 
	 * @param msg
	 */
	public void updtAPI(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(411, "the application name is null");
		} else {
			JsonObject body = msg.body();
			String sql = MessageFormat.format("update {0} set {1}= ? where {2} = ?", APITN, APICC, APIIC);
			JsonArray params = new JsonArray();
			params.add(body.getJsonObject("api").toString());
			params.add(body.getString("apiName"));
			jdbcClient.updateWithParams(sql, params, res -> {
				if (res.succeeded()) {
					int result = res.result().getUpdated();
					msg.reply(result);
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行更新API-->失败:" + res.cause().toString());
				}
			});
		}
	}

	// =============================================
	// =====================黑名单列表=================
	// =============================================
	/**
	 * 黑名单列表的表名简写
	 */
	private String BLTN = null;
	/**
	 * 黑名单列表的主键列简写
	 */
	private String BLIC = null;
	/**
	 * 黑名单列表jsonArray字符串内容的列名简写
	 */
	private String BLCC = null;
	/**
	 * 黑名单列表通讯传输中获取json中存放主键的属性名简写
	 */
	private String BLIN = null;
	/**
	 * 黑名单列表通讯传输中获取json中存放json字符串对象的属性名简写
	 */
	private String BLCN = null;

	/**
	 * 查看黑名单
	 * 
	 * @param msg
	 */
	public void findBlacklist(Message<JsonObject> msg) {
		String sql = MessageFormat.format("select {0} AS {1} ,{2} AS {3} from {4} ", BLIC, BLIN, BLCC, BLCN, BLTN);
		jdbcClient.query(sql, res -> {
			if (res.succeeded()) {
				List<JsonObject> rows = res.result().getRows();
				JsonObject result = null;
				if (rows != null && rows.size() > 0) {
					result = rows.get(0);
				} else {
					result = new JsonObject();
				}
				if (result.getValue(BLCN) instanceof String) {
					result.put(BLCN, new JsonArray(result.getString("content")));
				}
				msg.reply(result);
			} else {
				msg.fail(500, res.cause().toString());
				LOG.error("执行查询应用程序-->失败:" + res.cause().toString());
			}
		});
	}

	/**
	 * 更新黑名单
	 * 
	 * @param msg
	 */
	public void replaceBlacklist(Message<JsonObject> msg) {
		if (msg.body() == null) {
			msg.fail(412, "the Blacklist IP is null");
		} else {
			JsonObject body = msg.body();
			String sql = MessageFormat.format("REPLACE INTO {0} ({1},{2}) values(?,?)", BLTN, BLIC, BLCC);
			JsonArray params = new JsonArray();
			params.add(body.getString("blacklistName"));
			params.add(body.getJsonArray("blacklistBody").toString());
			jdbcClient.updateWithParams(sql, params, res -> {
				if (res.succeeded()) {
					int result = res.result().getUpdated();
					msg.reply(result);
				} else {
					msg.fail(500, res.cause().toString());
					LOG.error("执行添加应用程序-->失败:" + res.cause().toString());
				}
			});
		}
	}

	/**
	 * 初始化简写后的常量数据
	 */
	public void initShorthand() {
		this.APPTN = VxApiDATAStoreConstant.APPLICATION_TABLE_NAME;
		this.APPIC = VxApiDATAStoreConstant.APPLICATION_ID_COLUMN;
		this.APPCC = VxApiDATAStoreConstant.APPLICATION_CONTENT_COLUMN;
		this.APPIN = VxApiDATAStoreConstant.APPLICATION_ID_NAME;
		this.APPCN = VxApiDATAStoreConstant.APPLICATION_CONTENT_NAME;
		this.APITN = VxApiDATAStoreConstant.API_TABLE_NAME;
		this.APIIC = VxApiDATAStoreConstant.API_ID_COLUMN;
		this.APICC = VxApiDATAStoreConstant.API_CONTENT_COLUMN;
		this.APIIN = VxApiDATAStoreConstant.API_ID_NAME;
		this.APICN = VxApiDATAStoreConstant.API_CONTENT_NAME;
		this.API_APPIC = VxApiDATAStoreConstant.API_APP_ID_COLUMN;
		this.API_APPIN = VxApiDATAStoreConstant.API_APP_ID_NAME;
		this.BLTN = VxApiDATAStoreConstant.BLACKLIST_TABLE_NAME;
		this.BLIC = VxApiDATAStoreConstant.BLACKLIST_ID_COLUMN;
		this.BLCC = VxApiDATAStoreConstant.BLACKLIST_CONTENT_COLUMN;
		this.BLIN = VxApiDATAStoreConstant.BLACKLIST_ID_NAME;
		this.BLCN = VxApiDATAStoreConstant.BLACKLIST_CONTENT_NAME;

	}

}
