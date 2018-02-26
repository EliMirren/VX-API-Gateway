package com.szmirren.vxApi.core.common;

import com.szmirren.vxApi.core.enums.HTTPStatusCodeMsgEnum;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 将StatusCodeMsg转换为返回json<br>
 * 返回结果<br>
 * status : 状态码<br>
 * msg : 信息<br>
 * data : 数据<br>
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class ResultFormat {
	/**
	 * 格式化返回结果,code为状态码枚举类,data为数据
	 * 
	 * @param code
	 * @param data
	 * @return
	 */
	public static String format(HTTPStatusCodeMsgEnum code, Object data) {
		ResultFormatObj result = new ResultFormatObj(code.getCode(), code.getMsg(), data);
		return result.toJsonStr();
	}

	/**
	 * 格式化返回结果,code为状态码枚举类,data为数据
	 * 
	 * @param code
	 * @param data
	 * @return
	 */
	public static String format(HTTPStatusCodeMsgEnum code, JsonArray data) {
		JsonObject result = new JsonObject();
		result.put("status", code.getCode());
		result.put("msg", code.getMsg());
		if (data == null) {
			data = new JsonArray();
		}
		result.put("data", data);
		return result.toString();
	}

	/**
	 * 格式化返回结果,code为状态码枚举类,data为数据
	 * 
	 * @param code
	 * @param data
	 * @return
	 */
	public static String format(HTTPStatusCodeMsgEnum code, JsonObject data) {
		JsonObject result = new JsonObject();
		result.put("status", code.getCode());
		result.put("msg", code.getMsg());
		if (data == null) {
			data = new JsonObject();
		}
		result.put("data", data);
		return result.toString();
	}

	/**
	 * 格式化返回结果其中data为null,code为状态码枚举类
	 * 
	 * @param code
	 * @return
	 */
	public static String formatAsNull(HTTPStatusCodeMsgEnum code) {
		JsonObject result = new JsonObject();
		result.put("status", code.getCode());
		result.put("msg", code.getMsg());
		result.putNull("data");
		return result.toString();
	}

	/**
	 * 格式化返回结果其中data为{},code为状态码枚举类
	 * 
	 * @param code
	 * @return
	 */
	public static String formatAsNewJson(HTTPStatusCodeMsgEnum code) {
		JsonObject result = new JsonObject();
		result.put("status", code.getCode());
		result.put("msg", code.getMsg());
		result.put("data", new JsonObject());
		return result.toString();
	}

	/**
	 * 格式化返回结果其中data为[],code为状态码枚举类
	 * 
	 * @param code
	 * @return
	 */
	public static String formatAsNewArray(HTTPStatusCodeMsgEnum code) {
		JsonObject result = new JsonObject();
		result.put("status", code.getCode());
		result.put("msg", code.getMsg());
		result.put("data", new JsonArray());
		return result.toString();
	}

	/**
	 * 格式化返回结果其中data为0,code为状态码枚举类
	 * 
	 * @param code
	 * @return
	 */
	public static String formatAsZero(HTTPStatusCodeMsgEnum code) {
		JsonObject result = new JsonObject();
		result.put("status", code.getCode());
		result.put("msg", code.getMsg());
		result.put("data", 0);
		return result.toString();
	}

	/**
	 * 格式化返回结果其中data为1,code为状态码枚举类
	 * 
	 * @param code
	 * @return
	 */
	public static String formatAsOne(HTTPStatusCodeMsgEnum code) {
		JsonObject result = new JsonObject();
		result.put("status", code.getCode());
		result.put("msg", code.getMsg());
		result.put("data", 1);
		return result.toString();
	}

	/**
	 * 自定义返回结果
	 * 
	 * @param formatObj
	 * @return
	 */
	public static String formatCustom(ResultFormatObj formatObj) {
		return formatObj.toJsonStr();
	}

}
