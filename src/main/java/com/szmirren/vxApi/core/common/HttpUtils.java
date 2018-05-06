package com.szmirren.vxApi.core.common;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;

/**
 * HTTP相关的工具
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class HttpUtils {
	/**
	 * 解码URI中的参数,既?后面的参数
	 * 
	 * @param uri
	 *          uri参数,默认以utf-8方式
	 * @return 返回解码后的结果或者返回新的MultiMap
	 */
	public static MultiMap decoderUriParams(String uri) {
		return decoderUriParams(uri, Charset.forName("UTF-8"));
	}

	/**
	 * 解码URI中的参数,既?后面的参数
	 * 
	 * @param uri
	 *          uri参数
	 * @param charset
	 *          解码格式,如果该字段为空则默认为utf-8方式
	 * @return 返回解码后的结果或者返回新的MultiMap,如果发生异常返回新的MultiMap
	 */
	public static MultiMap decoderUriParams(String uri, Charset charset) {
		try {
			if (charset == null) {
				charset = Charset.forName("UTF-8");
			}
			QueryStringDecoder decode = new QueryStringDecoder(uri, charset, false);
			Map<String, List<String>> paramMap = decode.parameters();
			MultiMap params = new CaseInsensitiveHeaders();
			if (!paramMap.isEmpty()) {
				for (Map.Entry<String, List<String>> entry : paramMap.entrySet()) {
					params.add(entry.getKey(), entry.getValue());
				}
			}
			return params;
		} catch (Exception e) {
			return new CaseInsensitiveHeaders();
		}
	}
}
