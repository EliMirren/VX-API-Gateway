package com.szmirren.vxApi.core.common;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

import com.szmirren.vxApi.core.enums.ParamTypeEnum;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 字符串工具
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class StrUtil {

	/**
	 * 去掉下划线并将字符串转换成帕斯卡命名规范
	 * 
	 * @param str
	 * @return
	 */
	public static String unlineToPascal(String str) {
		if (str != null) {
			if (str.indexOf("_") == -1) {
				return fristToUpCase(str);
			}
			StringBuilder result = new StringBuilder();
			String[] temp = str.split("_");
			for (int i = 0; i < temp.length; i++) {
				if (temp[i].equals("") || temp[i].isEmpty()) {
					continue;
				}
				result.append(fristToUpCaseLaterToLoCase(temp[i]));
			}
			return result.toString();
		}

		return str;
	}

	/**
	 * 去掉下划线并将字符串转换成驼峰命名规范
	 * 
	 * @param str
	 * @return
	 */
	public static String unlineToCamel(String str) {
		if (str != null) {
			if (str.indexOf("_") == -1) {
				return fristToLoCase(str);
			}
			StringBuilder result = new StringBuilder();
			String[] temp = str.split("_");
			boolean falg = false;
			for (int i = 0; i < temp.length; i++) {
				if (temp[i].equals("") || temp[i].isEmpty()) {
					continue;
				}
				if (falg == false) {
					falg = true;
					result.append(temp[i].toLowerCase());
				} else {
					result.append(fristToUpCaseLaterToLoCase(temp[i]));
				}
			}
			return result.toString();
		}

		return str;
	}

	/**
	 * 将字符串首字母大写其后小写
	 * 
	 * @param str
	 * @return
	 */
	public static String fristToUpCaseLaterToLoCase(String str) {
		if (str != null && str.length() > 0) {
			str = (str.substring(0, 1).toUpperCase()) + (str.substring(1).toLowerCase());
		}
		return str;
	}

	/**
	 * 将字符串首字母小写其后大写
	 * 
	 * @param str
	 * @return
	 */
	public static String fristToLoCaseLaterToUpCase(String str) {
		if (str != null && str.length() > 0) {
			str = (str.substring(0, 1).toLowerCase()) + (str.substring(1).toUpperCase());

		}
		return str;
	}

	/**
	 * 将字符串首字母大写
	 * 
	 * @param str
	 * @return
	 */
	public static String fristToUpCase(String str) {
		if (str != null && str.length() > 0) {
			str = str.substring(0, 1).toUpperCase() + str.substring(1);
		}
		return str;
	}

	/**
	 * 将字符串首字母小写
	 * 
	 * @param str
	 * @return
	 */
	public static String fristToLoCase(String str) {
		if (str != null && str.length() > 0) {
			str = str.substring(0, 1).toLowerCase() + str.substring(1);
		}
		return str;
	}

	/**
	 * 检查字符串里面是否包含指定字符,包含返回true
	 * 
	 * @param regex
	 *          指定字符
	 * @param str
	 *          字符串
	 * @return
	 */
	public static boolean indexOf(String regex, String... str) {
		if (str == null) {
			return false;
		}
		for (String temp : str) {
			if (temp.indexOf(regex) == -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 如果jdk大于1.8可以直接使用String.join<br>
	 * 将字符串中间以separator连接起来<br>
	 * 示例:magre(".","1","2","3") 结果:"1.2.3"
	 * 
	 * @param separator
	 * @param str
	 * @return
	 */
	@Deprecated()
	public static String magre(String separator, String... str) {
		StringBuffer result = null;
		for (String temp : str) {
			if (result == null) {
				result = new StringBuffer(temp);
			}
			result.append("," + temp);
		}
		return result.toString();
	}

	/**
	 * 将字符串str中带有集合中rep[0]的字符串,代替为rep[1]的中的字符串
	 * 
	 * @param str
	 * @param rep
	 * @return
	 */
	public static String replace(String str, List<String[]> rep) {
		for (String[] item : rep) {
			if (item[1] == null) {
				item[1] = "";
			}
			str = str.replace(item[0], item[1]);
		}
		return str;
	}

	/**
	 * 创建字符串数组
	 * 
	 * @param str
	 * @return
	 */
	public static String[] asStrArray(String... str) {
		return str;
	}
	/**
	 * 获得RFC822规范的Date
	 * 
	 * @param date
	 * @return
	 */
	public static String getRfc822DateFormat(Date date) {
		SimpleDateFormat rfc822DateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		rfc822DateFormat.setTimeZone(new SimpleTimeZone(0, "GMT"));
		return rfc822DateFormat.format(date);
	}

	/**
	 * 判断字符串是否为null或者空,如果是返回true
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(String str) {
		if (str == null || "".equals(str.trim())) {
			return true;
		}
		return false;
	}
	/**
	 * 判断字符串是否为null或者空,如果是返回true
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNullOrEmpty(String... str) {
		if (str == null || str.length == 0) {
			return true;
		}
		for (int i = 0; i < str.length; i++) {
			if (str[i] == null || "".equals(str[i].trim())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将毫秒按指定格式转换为 年日时分秒,如果如果格式中不存在年则将年装换为天
	 * 
	 * @param time
	 *          毫秒
	 * @param pattern
	 *          正则:$y=年,$d=日,$h=小时,$m分钟,$s=秒 <br>
	 *          示例:$y年$d日 = 10年12天
	 * @return
	 */
	public static String millisToDateTime(long time, String pattern) {
		long day = time / 86400000;
		long hour = (time % 86400000) / 3600000;
		long minute = (time % 86400000 % 3600000) / 60000;
		long second = (time % 86400000 % 3600000 % 60000) / 1000;
		if (pattern.indexOf("$y") == -1) {
			pattern = pattern.replace("$d", Long.toString(day));
		} else {
			pattern = pattern.replace("$y", Long.toString(day / 365)).replace("$d", Long.toString(day % 365));
		}
		return pattern.replace("$h", Long.toString(hour)).replace("$m", Long.toString(minute)).replace("$s", Long.toString(second));
	}

	/**
	 * 判断一个字符串是否为指定对象
	 * 
	 * @param str
	 * @param type
	 * @return
	 */
	public static boolean isType(String str, ParamTypeEnum type) {
		try {
			if (type == ParamTypeEnum.Boolean) {
				return str.equals("true") || str.equals("false");
			} else if (type == ParamTypeEnum.Integer) {
				Integer.parseInt(str);
			} else if (type == ParamTypeEnum.Long) {
				Long.parseLong(str);
			} else if (type == ParamTypeEnum.Float) {
				Float.parseFloat(str);
			} else if (type == ParamTypeEnum.Double) {
				Double.parseDouble(str);
			} else if (type == ParamTypeEnum.JsonObject) {
				new JsonObject(str);
			} else if (type == ParamTypeEnum.JsonArray) {
				new JsonArray(str);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断将字符串转换为指定类型后将其与Number对比:str > num
	 * 
	 * @param type
	 * @param str
	 * @param number
	 * @return
	 */
	public static boolean numberGtNumber(ParamTypeEnum type, String str, Number num) {
		try {
			if (type == ParamTypeEnum.Integer) {
				int i = Integer.parseInt(str);
				return i > num.intValue();
			} else if (type == ParamTypeEnum.Long) {
				long l = Long.parseLong(str);
				return l > num.longValue();
			} else if (type == ParamTypeEnum.Float) {
				float f = Float.parseFloat(str);
				return f > num.floatValue();
			} else if (type == ParamTypeEnum.Double) {
				double d = Double.parseDouble(str);
				return d > num.doubleValue();
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断将字符串转换为指定类型后将其与Number对比:str < num
	 * 
	 * @param type
	 * @param str
	 * @param number
	 * @return
	 */
	public static boolean numberLtNumber(ParamTypeEnum type, String str, Number num) {
		try {
			if (type == ParamTypeEnum.Integer) {
				int i = Integer.parseInt(str);
				return i < num.intValue();
			} else if (type == ParamTypeEnum.Long) {
				long l = Long.parseLong(str);
				return l < num.longValue();
			} else if (type == ParamTypeEnum.Float) {
				float f = Float.parseFloat(str);
				return f < num.floatValue();
			} else if (type == ParamTypeEnum.Double) {
				double d = Double.parseDouble(str);
				return d < num.doubleValue();
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 将一个字符串转换为int,如果字符串为null或者""返回0
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static int getint(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return 0;
		}
		return new Integer(str);
	}
	/**
	 * 将一个字符串转换为int,如果字符串为null或者""返回0,如果转换失败返回0
	 * 
	 * @param str
	 * @return
	 */
	public static int getintTry(String str) {
		if (isNullOrEmpty(str)) {
			return 0;
		}
		try {
			return new Integer(str);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * 将一个字符串转换为Integer,如果字符串为null或者""返回null
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static Integer getInteger(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return null;
		}
		return new Integer(str);
	}
	/**
	 * 将一个字符串转换为long,如果字符串为null或者""返回0
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static long getlong(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return 0l;
		}
		return new Long(str);
	}
	/**
	 * 将一个字符串转换为long,如果字符串为null或者""返回0
	 * 
	 * @param str
	 */
	public static long getlongTry(String str) {
		if (isNullOrEmpty(str)) {
			return 0l;
		}
		try {
			return new Long(str);
		} catch (NumberFormatException e) {
			return 0l;
		}
	}

	/**
	 * 将一个字符串转换为Long,如果字符串为null或者""返回null
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static Long getLong(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return 0L;
		}
		return new Long(str);
	}
	/**
	 * 将一个字符串转换为float,如果字符串为null或者""返回0.0f
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static float getfloat(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return 0.0f;
		}
		return new Float(str);
	}
	/**
	 * 将一个字符串转换为float,如果字符串为null或者""返回null
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static Float getFloat(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return null;
		}
		return new Float(str);
	}
	/**
	 * 将一个字符串转换为double,如果字符串为null或者""返回0.0
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static double getdouble(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return 0.0;
		}
		return new Double(str);
	}
	/**
	 * 将一个字符串转换为Double,如果字符串为null或者""返回null
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static Double getDouble(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return null;
		}
		return new Double(str);
	}
	/**
	 * 将一个字符串转换为Date,如果字符串为null或者""返回null
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public static Date getDate(String str) throws NumberFormatException {
		if (isNullOrEmpty(str)) {
			return null;
		}
		try {
			return Date.from(Instant.parse(str));
		} catch (Exception e) {
			return new Date(new Long(str));
		}
	}
	/**
	 * 将一个字符串转换为Instant,如果字符串为null或者""返回null
	 * 
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 * @throws DateTimeParseException
	 * @throws RuntimeException
	 */
	public static Instant getInstant(String str) throws NumberFormatException, DateTimeParseException, RuntimeException {
		if (isNullOrEmpty(str)) {
			return null;
		}
		try {
			return Instant.parse(str);
		} catch (Exception e) {
			return Instant.ofEpochMilli(new Long(str));
		}
	}
	/**
	 * 将一个字符串转换为JsonObject,如果字符串为null或者""返回null
	 * 
	 * @param str
	 * @return
	 * @throws DecodeException
	 */
	public static JsonObject getJsonObject(String str) throws DecodeException {
		if (isNullOrEmpty(str)) {
			return null;
		}
		return new JsonObject(str);
	}
	/**
	 * 将一个字符串转换为JsonArray,如果字符串为null或者""返回null
	 * 
	 * @param str
	 * @return
	 * @throws DecodeException
	 */
	public static JsonArray getJsonArray(String str) throws DecodeException {
		if (isNullOrEmpty(str)) {
			return null;
		}
		return new JsonArray(str);
	}

}
