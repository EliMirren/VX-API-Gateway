package com.szmirren.vxApi.core.common;

import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;

/**
 * 参数检查工具
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class ParamCheckUtil {
	/**
	 * 检查是否为int类型的数据,
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isInt(String obj) {
		try {
			Integer.valueOf(obj);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 检查是否为int类型的数据,
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isInt(Object obj) {
		try {
			Integer.valueOf(obj.toString());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 检查是否为Long类型的数据,
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isLong(String obj) {
		try {
			Long.valueOf(obj);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 检查否为Long类型的数据,
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isLong(Object obj) {
		try {
			Long.valueOf(obj.toString());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 检查是否为Float类型的数据,
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isFloat(String obj) {
		try {
			Float.valueOf(obj);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 检查是否为Float类型的数据,
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isFloat(Object obj) {
		try {
			Float.valueOf(obj.toString());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 检查是否为Double类型的数据,
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isDouble(String obj) {
		try {
			Double.valueOf(obj);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 检查是否为Double类型的数据,
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isDouble(Object obj) {
		try {
			Double.valueOf(obj.toString());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 检查是否为Boolean数据
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isBoolean(String obj) {
		return (obj != null && (obj.equals("true") || obj.equals("false")));
	}

	/**
	 * 检查字符串是否为Boolean数据
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isBoolean(Object obj) {
		return (obj != null && (obj.equals("true") || obj.equals("false")));
	}

	/**
	 * 检查是否为json格式
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isJosn(Object obj) {
		try {
			Json.encode(obj);
			return true;
		} catch (EncodeException e) {
			return false;
		}
	}
}
