package com.szmirren.vxApi.core.common;

import java.util.List;

import com.szmirren.vxApi.core.enums.ParamTypeEnum;

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
	 *            指定字符
	 * @param str
	 *            字符串
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
	 *            毫秒
	 * @param pattern
	 *            正则:$y=年,$d=日,$h=小时,$m分钟,$s=秒 <br>
	 *            示例:$y年$d日 = 10年12天
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
		return pattern.replace("$h", Long.toString(hour)).replace("$m", Long.toString(minute)).replace("$s",
				Long.toString(second));
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

}
