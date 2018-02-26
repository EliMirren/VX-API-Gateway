package com.szmirren.vxApi.core.enums;

/**
 * 时间单位类
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public enum TimeUnitEnum {
	DAYS(86400), HOURS(3600), MINUTES(60);
	private long val;

	private TimeUnitEnum(long val) {
		this.val = val;
	}

	public long getVal() {
		return val;
	}

}
