package com.szmirren.vxApi.core.common;

import com.szmirren.vxApi.core.entity.VxApiContentType;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;
/**
 * 用户请求的body主体解析工具<br>
 * 当前只解析Content-type=Null或者Urlencoded
 * 
 * @author <a href="http://szmirren.com">Mirren</a>
 *
 */
public class VxApiRequestBodyHandler implements Handler<Buffer> {
	/** 用户请求的body参数 */
	private MultiMap body = new CaseInsensitiveHeaders();
	/** 用户请求的body长度 */
	private long bodyLength;
	/** 用户请求的contentType */
	private VxApiContentType contentType;
	/** 请求体的最大限制长度小于=0代表无限 */
	private long maxContentLength;
	/** 用户请求的数据Buffer */
	private Buffer bodyBuffer = Buffer.buffer();
	/**
	 * 实例化一个用户请求bodyhandler
	 * 
	 * @param contentType
	 * @param maxContentLength
	 */
	public VxApiRequestBodyHandler(VxApiContentType contentType, long maxContentLength) {
		super();
		this.contentType = contentType;
		this.maxContentLength = maxContentLength;
	}

	@Override
	public void handle(Buffer buffer) {
		// 如果buffer为null或者不是支持解析的类型则返回
		if (buffer == null || !contentType.isDecodedSupport()) {
			return;
		}
		bodyLength += buffer.length();
		if (maxContentLength > 0 && bodyLength > maxContentLength) {
			return;
		}
		bodyBuffer.appendBuffer(buffer);
	}
	/**
	 * 获得body的参数
	 * 
	 * @return 返回一个不为null的MultiMap
	 */
	public MultiMap getBody() {
		if (contentType.isApplicationJson()) {
			try {
				JsonObject object = new JsonObject(bodyBuffer);
				if (object.getMap() != null) {
					object.getMap().forEach((k, v) -> {
						body.add(k, v.toString());
					});
				}
			} catch (Exception e) {
			}
		} else if (contentType.isUrlencoded()) {
			MultiMap decoderUriParams = HttpUtils.decoderUriParams(bodyBuffer.toString(), contentType.getCharset());
			if (decoderUriParams != null) {
				body.addAll(decoderUriParams);
			}
		}
		return body;
	}

	/**
	 * 获得body的长度
	 * 
	 * @return
	 */
	public long getBodyLength() {
		return bodyLength;
	}
	/**
	 * 获得是否超过最大Content-Length限定
	 * 
	 * @return 超过返回true , 不超过返回false
	 */
	public boolean isExceededMaxLen() {
		return maxContentLength > 0 && bodyLength > maxContentLength;
	}

	@Override
	public String toString() {
		return "VxApiRequestBodyHandler [body=" + body + ", bodyLength=" + bodyLength + ", contentType=" + contentType + ", maxContentLength="
				+ maxContentLength + "]";
	}

}
