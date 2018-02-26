package com.szmirren.vxApi.core.entity;

import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.core.enums.ParamTypeEnum;
import com.szmirren.vxApi.core.options.VxApiParamCheckOptions;

import io.vertx.core.json.JsonObject;

/**
 * 网关的入口参数
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiEntranceParam {
	private String paramName;// 参数的名字
	private String describe;// 描述
	private ParamPositionEnum position;// 参数的位置
	private ParamTypeEnum paramType;// 参数类型
	private boolean isNotNull;// 是否可以为空
	private Object def;// 默认值
	private VxApiParamCheckOptions checkOptions;// 参数的配置

	/**
	 * 将对象装换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("paramName", this.paramName);
		json.put("position", this.position);
		json.put("paramType", this.paramType);
		json.put("isNotNull", this.isNotNull);
		if (def != null) {
			json.put("def", this.def);
		}
		if (describe != null) {
			json.put("describe", this.describe);
		}
		if (checkOptions != null) {
			json.put("checkOptions", checkOptions.toJson());
		}
		return json;
	}

	/**
	 * 通过JsonObject实例化一个对象
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiEntranceParam fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiEntranceParam option = new VxApiEntranceParam();
		if (obj.getValue("paramName") instanceof String) {
			option.setParamName(obj.getString("paramName"));
		}
		if (obj.getValue("describe") instanceof String) {
			option.setDescribe(obj.getString("describe"));
		}
		if (obj.getValue("isNotNull") instanceof Boolean) {
			option.setNotNull(obj.getBoolean("isNotNull"));
		}
		if (obj.getValue("position") instanceof String) {
			option.setPosition(ParamPositionEnum.valueOf(obj.getString("position")));
		}
		if (obj.getValue("paramType") instanceof String) {
			option.setParamType(ParamTypeEnum.valueOf(obj.getString("paramType")));
		}
		if (obj.getValue("def") != null) {
			option.setDef(obj.getValue("def"));
		}
		if (obj.getValue("checkOptions") instanceof JsonObject) {
			option.setCheckOptions(VxApiParamCheckOptions.fromJson(obj.getJsonObject("checkOptions")));
		} else if (obj.getValue("checkOptions") instanceof String) {
			option.setCheckOptions(VxApiParamCheckOptions.fromJson(new JsonObject(obj.getString("checkOptions"))));
		}
		return option;
	}

	public VxApiEntranceParam() {
		super();
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public ParamPositionEnum getPosition() {
		return position;
	}

	public void setPosition(ParamPositionEnum position) {
		this.position = position;
	}

	public ParamTypeEnum getParamType() {
		return paramType;
	}

	public void setParamType(ParamTypeEnum paramType) {
		this.paramType = paramType;
	}

	public boolean isNotNull() {
		return isNotNull;
	}

	public void setNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
	}

	public Object getDef() {
		return def;
	}

	public void setDef(Object def) {
		this.def = def;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public VxApiParamCheckOptions getCheckOptions() {
		return checkOptions;
	}

	public void setCheckOptions(VxApiParamCheckOptions checkOptions) {
		this.checkOptions = checkOptions;
	}

	@Override
	public String toString() {
		return "VxApiEntranceParam [paramName=" + paramName + ", describe=" + describe + ", position=" + position
				+ ", paramType=" + paramType + ", isNotNull=" + isNotNull + ", def=" + def + ", checkOptions="
				+ checkOptions + "]";
	}

}
