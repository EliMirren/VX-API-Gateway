package com.szmirren.vxApi.core.common;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 获取相应路径的工具
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class PathUtil {

	/**
	 * 获得根目录如果在jar中运行获得相对路径,反则返回当前线程运行的根目录
	 * 
	 * @param name
	 * @return
	 */
	public static String getPath(String fileName) {
		if (fileName == null) {
			throw new NullPointerException("文件名字不能为空");
		}
		URL path = Thread.currentThread().getContextClassLoader().getResource(fileName);
		if (path != null && path.getPath().contains(".jar!")) {
			return fileName;
		} else {
			return path == null ? "" : path.getPath().substring(1);
		}
	}

	/**
	 * 获得根目录如果在jar中运行获得相对路径,反则返回当前线程运行的根目录
	 * 
	 * @param fileName
	 * @return
	 * @throws URISyntaxException
	 */
	public static String getPathToURI(String fileName) throws URISyntaxException {
		if (fileName == null) {
			throw new NullPointerException("文件名字不能为空");
		}
		URL path = Thread.currentThread().getContextClassLoader().getResource(fileName);
		if (path != null && path.getPath().contains(".jar!")) {
			return fileName;
		} else {
			return path.toURI().toString();
		}
	}

	/**
	 * 获得资源的流
	 * 
	 * @param fileName
	 * @return
	 */
	public static InputStream getStream(String fileName) {
		if (fileName == null) {
			throw new NullPointerException("文件名字不能为空");
		}
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
	}

}
