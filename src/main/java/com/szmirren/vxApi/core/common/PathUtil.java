package com.szmirren.vxApi.core.common;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * 获取相应路径的工具
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class PathUtil {
	/**
	 * 判断是否在jar环境中运行
	 * 
	 * @return 是返回true
	 */
	public static boolean isJarEnv() {
		return PathUtil.class.getResource("").getPath().contains(".jar!");
	}

	/**
	 * 判断是否在jar环境中运行
	 * 
	 * @return 是返回true
	 */
	public static boolean isJarEnv(String fileName) {
		return Thread.currentThread().getContextClassLoader().getResource(fileName).getPath().contains(".jar!");
	}

	/**
	 * 获得根目录如果在jar中运行获得相对路径,反则返回当前线程运行的根目录
	 * 
	 * @param name
	 * @return
	 */
	public static String getPathString(String fileName) {
		if (fileName == null) {
			throw new NullPointerException("文件名字不能为空");
		}
		URL path = Thread.currentThread().getContextClassLoader().getResource(fileName);
		if (path != null && path.getPath().contains(".jar!")) {
			return fileName;
		} else {
			String result = path == null ? "" : path.getPath();
			return result;
		}
	}

	/**
	 * 通过名字获得项目的Path文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static Path getPath(String fileName) {
		File file = new File(PathUtil.getPathString(fileName));
		return file.toPath();
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
