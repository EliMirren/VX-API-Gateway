package com.szmirren.vxApi.core.options;

import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.core.enums.ParamSystemVarTypeEnum;
import com.szmirren.vxApi.core.enums.ParamTypeEnum;

import io.vertx.core.json.JsonObject;

/**
 * 用于映射前后端对应的参数值
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiParamOptions {
	private String apiParamName;// 网关参数的名字
	private ParamPositionEnum apiParamPosition;// 网关参数的位置
	private String serParamName;// 服务端参数的名字
	private ParamPositionEnum serParamPosition;// 服务端参数的位置
	private ParamTypeEnum paramType;// 前后端映射的参数类型
	private ParamSystemVarTypeEnum sysParamType;// 系统参数类型
	private Object paramValue;// 参数值
	private String describe;// 参数描述
	private int type = 0;// 参数的类型0=前端映射参数,1=系统参数,2=透传参数,9=自定义参数

	/**
	 * 将对象装换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		if (this.apiParamName != null) {
			json.put("apiParamName", this.apiParamName);
		}
		if (this.apiParamPosition != null) {
			json.put("apiParamPosition", this.apiParamPosition);
		}
		json.put("serParamName", this.serParamName);
		json.put("serParamPosition", this.serParamPosition);
		json.put("paramType", this.paramType);
		if (this.paramValue != null) {
			json.put("paramValue", this.paramValue);
		}
		if (this.describe != null) {
			json.put("describe", this.describe);
		}
		if (this.sysParamType != null) {
			json.put("sysParamType", this.sysParamType);
		}
		json.put("type", this.type);
		return json;
	}

	/**
	 * 通过JsonObject实例化一个对象
	 * 
	 * @param obj
	 * @return
	 */
	public static VxApiParamOptions fromJson(JsonObject obj) {
		if (obj == null) {
			return null;
		}
		VxApiParamOptions option = new VxApiParamOptions();
		if (obj.getValue("apiParamName") instanceof String) {
			option.setApiParamName(obj.getString("apiParamName"));
		}
		if (obj.getValue("serParamName") instanceof String) {
			option.setSerParamName(obj.getString("serParamName"));
		}
		if (obj.getValue("apiParamPosition") instanceof String) {
			option.setApiParamPosition(ParamPositionEnum.valueOf(obj.getString("apiParamPosition")));
		}
		if (obj.getValue("serParamPosition") instanceof String) {
			option.setSerParamPosition(ParamPositionEnum.valueOf(obj.getString("serParamPosition")));
		}
		if (obj.getValue("paramType") instanceof String) {
			option.setParamType(ParamTypeEnum.valueOf(obj.getString("paramType")));
		}
		if (obj.getValue("describe") instanceof String) {
			option.setDescribe(obj.getString("describe"));
		}
		if (obj.getValue("sysParamType") instanceof String) {
			option.setSysParamType(ParamSystemVarTypeEnum.valueOf(obj.getString("sysParamType")));
		}
		if (obj.getValue("paramValue") != null) {
			option.setParamValue(obj.getValue("paramValue"));
		}
		if (obj.getValue("type") instanceof Number) {
			option.setType(((Number) obj.getValue("type")).intValue());
		}
		return option;
	}

	public VxApiParamOptions() {
		super();
	}

	public String getApiParamName() {
		return apiParamName;
	}

	public void setApiParamName(String apiParamName) {
		this.apiParamName = apiParamName;
	}

	public ParamPositionEnum getApiParamPosition() {
		return apiParamPosition;
	}

	public void setApiParamPosition(ParamPositionEnum apiParamPosition) {
		this.apiParamPosition = apiParamPosition;
	}

	public String getSerParamName() {
		return serParamName;
	}

	public void setSerParamName(String serParamName) {
		this.serParamName = serParamName;
	}

	public ParamPositionEnum getSerParamPosition() {
		return serParamPosition;
	}

	public void setSerParamPosition(ParamPositionEnum serParamPosition) {
		this.serParamPosition = serParamPosition;
	}

	public ParamTypeEnum getParamType() {
		return paramType;
	}

	public void setParamType(ParamTypeEnum paramType) {
		this.paramType = paramType;
	}

	public Object getParamValue() {
		return paramValue;
	}

	public void setParamValue(Object paramValue) {
		this.paramValue = paramValue;
	}

	public ParamSystemVarTypeEnum getSysParamType() {
		return sysParamType;
	}

	public void setSysParamType(ParamSystemVarTypeEnum sysParamType) {
		this.sysParamType = sysParamType;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	/**
	 * 参数的类型0=前端映射参数,1=系统参数,2=透传参数,9=自定义参数
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
