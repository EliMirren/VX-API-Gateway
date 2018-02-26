package com.szmirren.vxApi.core.entity;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 流量监控
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiAPILimit {
	private Instant timePoints = Instant.now();// 统计时间
	private Map<String, Long> userIpCurPoints = new HashMap<>();// 用户ip统计
	private long ipTop = -1;// ip限制的数量
	private long apiTop = -1;// api的限制数量
	private long curPoint;// 当前的数量

	/**
	 * 创建一个流量限制
	 * 
	 * @param ipTop
	 *            IP最大数
	 * @param apiTop
	 *            api最大数
	 */
	public VxApiAPILimit(long ipTop, long apiTop) {
		super();
		this.ipTop = ipTop;
		this.apiTop = apiTop;
	}

	/**
	 * 得到统计的时间
	 * 
	 * @return
	 */
	public Instant getTimePoints() {
		return timePoints;
	}

	/**
	 * 设置统计时间
	 * 
	 * @param timePoints
	 */
	public void setTimePoints(Instant timePoints) {
		this.timePoints = timePoints;
	}

	/**
	 * 获得当前连接的用户
	 * 
	 * @return
	 */
	public Map<String, Long> getUserIpCurPoints() {
		return userIpCurPoints;
	}

	/**
	 * 设置当前连接的用户
	 * 
	 * @param userIpCurPoints
	 */
	public void setUserIpCurPoints(Map<String, Long> userIpCurPoints) {
		this.userIpCurPoints = userIpCurPoints;
	}

	/**
	 * 给当前连接用户连接数添加一个新用户,如果存在该用户将用户数据重置
	 * 
	 * @param ip
	 * @param points
	 */
	public void addUserIpCurPotints(String ip, long points) {
		this.userIpCurPoints.put(ip, points);
	}

	/**
	 * 得到IP的最大限制数
	 * 
	 * @return
	 */
	public long getIpTop() {
		return ipTop;
	}

	/**
	 * 设置IP的最大限制数,-1表示无限
	 * 
	 * @param ipTop
	 */
	public void setIpTop(long ipTop) {
		this.ipTop = ipTop;
	}

	/**
	 * 得到API的最大限制数
	 * 
	 * @return
	 */
	public long getApiTop() {
		return apiTop;
	}

	/**
	 * 设置API的最大限制数,-1表示无限
	 * 
	 * @param apiTop
	 */
	public void setApiTop(long apiTop) {
		this.apiTop = apiTop;
	}

	/**
	 * 得到当前连接的人数
	 * 
	 * @return
	 */
	public long getCurPoint() {
		return curPoint;
	}

	/**
	 * 设置当前连接的人数
	 * 
	 * @param curPoint
	 */
	public void setCurPoint(long curPoint) {
		this.curPoint = curPoint;
	}

	@Override
	public String toString() {
		return "VxApiAPILimit [timePoints=" + timePoints + ", userIpCurPoints=" + userIpCurPoints + ", ipTop=" + ipTop
				+ ", apiTop=" + apiTop + ", curPoint=" + curPoint + "]";
	}

}
