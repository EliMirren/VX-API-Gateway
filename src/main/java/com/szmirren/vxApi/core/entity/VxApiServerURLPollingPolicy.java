package com.szmirren.vxApi.core.entity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务地址的轮询策略 <br>
 * tips:在非线程安全的环境下需要注意线程安全的问题
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiServerURLPollingPolicy {
	/**
	 * 一共有多少个服务对象
	 */
	private int size = 1;
	/**
	 * 当前服务对象坐标
	 */
	private int curIndex = 0;
	/**
	 * 当前权重下标
	 */
	private int weightIndex = 0;
	/**
	 * 服务的地址
	 */
	private String[] urls;
	/**
	 * 服务地址权重
	 */
	private int[] weight;
	/**
	 * 是否存有可用服务
	 */
	private boolean haveService = true;
	/**
	 * 是否存在坏的连接
	 */
	private boolean haveBadService = false;
	/**
	 * 是否正在重试检查坏的服务连接
	 */
	private boolean checkWaiting = false;
	/**
	 * 存储用户的IP地址对应的下标
	 */
	private Map<String, Integer> ipMap = new HashMap<>();
	/**
	 * 用于存储当前下标的服务地址是否可用
	 */
	private Map<Integer, Boolean> availableMap = new HashMap<>();
	/**
	 * 失败的次数
	 */
	private Map<Integer, Integer> failureCountMap = new HashMap<>();

	/**
	 * 创建轮询策略
	 * 
	 * @param urls
	 *            服务连接的URL集,不能为空,size大于1,正确的URL地址
	 * @throws NullPointerException
	 *             服务连接集合空是抛出
	 * @throws MalformedURLException
	 *             不是正确的URL是抛出
	 */
	public VxApiServerURLPollingPolicy(List<VxApiServerURL> urls) throws NullPointerException, MalformedURLException {
		super();
		if (urls == null || urls.size() == 0) {
			throw new NullPointerException("服务URL集不能为空");
		}
		for (VxApiServerURL absoluteURI : urls) {
			new URL(absoluteURI.getUrl());
		}
		size = urls.size();
		this.urls = new String[size];
		weight = new int[size];
		for (int i = 0; i < urls.size(); i++) {
			// 初始化URL与权重
			this.urls[i] = urls.get(i).getUrl();
			weight[i] = urls.get(i).getWeight();
			// 默认设置所有连接可用
			availableMap.put(i, true);
		}
	}

	/**
	 * 以轮询权重的方式得到服务URL
	 * 
	 * @return
	 */
	public VxApiServerURLInfo getUrl() {
		String url = urls[curIndex];
		int index = curIndex;
		if (size > 1) {
			weightIndex++;
			if (weightIndex >= weight[curIndex]) {
				weightIndex = 0;
				moveCurIndex();// 下标移动
			}
		}
		VxApiServerURLInfo urlInfo = new VxApiServerURLInfo(url, index);
		return urlInfo;
	}

	/**
	 * 通过用户的IP地址获得用户对应的服务连接
	 * 
	 * @param ip
	 *            用户的IP
	 * @return
	 */
	public VxApiServerURLInfo getUrl(String ip) {
		VxApiServerURLInfo urlInfo = null;
		if (haveService) {
			Integer index = ipMap.get(ip);
			if (index == null || availableMap.get(index) == false) {
				urlInfo = getUrl();
				ipMap.put(ip, urlInfo.getIndex());
			} else {
				urlInfo = new VxApiServerURLInfo(urls[index], index);
			}
		} else {
			urlInfo = new VxApiServerURLInfo(urls[0], 0);
		}
		return urlInfo;
	}

	/**
	 * 移动当前下标
	 */
	private void moveCurIndex() {
		if (!haveService) {
			curIndex = 0;
		} else if (haveBadService) {
			updateCurIndex();
		} else {
			if ((curIndex + 1) > size - 1) {
				curIndex = 0;
			} else {
				curIndex++;
			}
		}
	}

	/**
	 * 改变当前下标
	 */
	private void updateCurIndex() {
		if (!haveService) {
			curIndex = 0;
		} else {
			if (availableMap.get(curIndex + 1) != null && availableMap.get(curIndex + 1) == true) {
				curIndex++;
				return;
			}
			boolean flag = true;// 标记当前坐标到结束是否有可用的地址
			for (int i = curIndex + 1; i < size; i++) {
				if (availableMap.get(i) == true) {
					curIndex = i;
					flag = false;
					break;
				}
			}
			if (flag) {
				boolean bad = false;
				for (int i = 0; i < size; i++) {
					if (availableMap.get(i) == true) {
						curIndex = i;
						break;
					}
					if (i == size - 1) {
						bad = true;
					}
				}
				if (bad) {
					haveService = false;
				}
			}
		}
	}

	/**
	 * 提交连接失败的连接下标
	 * 
	 * @param index
	 */
	public void reportBadService(int index) {
		if (index >= size || index < 0) {
			return;
		}
		if (failureCountMap.get(index) != null) {
			availableMap.put(index, false);
			haveBadService = true;
			if (index == curIndex) {
				updateCurIndex();
			}
		} else {
			failureCountMap.put(index, 1);
		}
	}

	/**
	 * 提交下标可以使用
	 * 
	 * @param index
	 */
	public void reportGreatService(int index) {
		if (index >= size || index < 0) {
			return;
		}
		failureCountMap.remove(index);
		availableMap.put(index, true);
		haveService = true;
		if (failureCountMap.size() == 0) {
			haveBadService = false;
		}
	}

	/**
	 * 获得坏的连接
	 * 
	 * @return 返回一个不为null的List
	 */
	public List<VxApiServerURLInfo> getBadService() {
		List<VxApiServerURLInfo> result = new ArrayList<>();
		failureCountMap.forEach((k, v) -> result.add(new VxApiServerURLInfo(urls[k], k)));
		return result;
	}

	/**
	 * 查看是否有可用服务连接
	 * 
	 * @return 有可用服务连接返回true,没有可用服务连接返回false
	 */
	public boolean isHaveService() {
		return haveService;
	}

	/**
	 * 查看是否有坏的服务连接
	 * 
	 * @return 存在坏的服务连接放回true,不存在返回false
	 */
	public boolean isHaveBadService() {
		return haveBadService;
	}

	/**
	 * 查看是否正在重试坏的服务连接是否可用,正在检查返回true,不在检查返回false
	 * 
	 * @return
	 */
	public boolean isCheckWaiting() {
		return checkWaiting;
	}

	/**
	 * 设置是否正在重试坏的服务连接,true=正在检查,false=不在检查
	 * 
	 * @param checkWaiting
	 */
	public void setCheckWaiting(boolean checkWaiting) {
		this.checkWaiting = checkWaiting;
	}

}
