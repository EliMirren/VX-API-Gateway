package com.szmirren.vxApi.core.options;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 参数检查的配置
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiParamCheckOptions {
	private Long maxLength;// 字符串的最大长度
	private Number minValue;// 数值的最小值
	private Number maxValue;// 数值的最大值
	private String regex;// 正则表达式
	private List<String> enums;// 枚举类

	/**
	 * 将对象转换为json
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (maxLength != null) {
			json.put("maxLength", this.maxLength);
		}
		if (minValue != null) {
			json.put("minValue", this.minValue);
		}
		if (maxValue != null) {
			json.put("maxValue", this.maxValue);
		}
		if (regex != null) {
			json.put("regex", this.regex);
		}
		if (enums != null) {
			json.put("enums", enums);
		}
		return json;
	}

	/**
	 * 通过json实例化一个对象
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiParamCheckOptions fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiParamCheckOptions option = new VxApiParamCheckOptions();
		if (obj.getValue("maxLength") instanceof Number) {
			option.setMaxLength(((Number) obj.getValue("maxLength")).longValue());
		}
		if (obj.getValue("minValue") instanceof Number) {
			option.setMinValue((Number) obj.getValue("minValue"));
		}
		if (obj.getValue("maxValue") instanceof Number) {
			option.setMaxValue((Number) obj.getValue("maxValue"));
		}
		if (obj.getValue("regex") instanceof String) {
			option.setRegex(obj.getString("regex"));
		}
		if (obj.getValue("enums") instanceof JsonArray) {
			List<String> list = new ArrayList<>();
			obj.getJsonArray("enums").forEach(va -> {
				if (va instanceof String) {
					list.add(va.toString());
				}
			});
			option.setEnums(list);
		}
		return option;
	}

	public VxApiParamCheckOptions() {
		super();
	}

	public VxApiParamCheckOptions(Long maxLength, Number minValue, Number maxValue, String regex, List<String> enums) {
		super();
		this.maxLength = maxLength;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.regex = regex;
		this.enums = enums;
	}

	public Long getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Long maxLength) {
		this.maxLength = maxLength;
	}

	public Number getMinValue() {
		return minValue;
	}

	public void setMinValue(Number minValue) {
		this.minValue = minValue;
	}

	public Number getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Number maxValue) {
		this.maxValue = maxValue;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public List<String> getEnums() {
		return enums;
	}

	public void setEnums(List<String> enums) {
		this.enums = enums;
	}

	@Override
	public String toString() {
		return "VxApiParamCheckOptions [maxLength=" + maxLength + ", minValue=" + minValue + ", maxValue=" + maxValue
				+ ", regex=" + regex + ", enums=" + enums + "]";
	}

}
