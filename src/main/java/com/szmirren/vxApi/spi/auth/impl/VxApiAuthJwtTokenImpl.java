package com.szmirren.vxApi.spi.auth.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.szmirren.vxApi.core.common.ResultFormat;
import com.szmirren.vxApi.core.common.VxApiGatewayAttribute;
import com.szmirren.vxApi.core.enums.ContentTypeEnum;
import com.szmirren.vxApi.core.enums.HTTPStatusCodeMsgEnum;
import com.szmirren.vxApi.core.enums.ParamPositionEnum;
import com.szmirren.vxApi.spi.auth.VxApiAuth;
import com.szmirren.vxApi.spi.common.HttpHeaderConstant;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * JWTToken权限认证实现类
 * 
 * @author onemy
 *
 */
public class VxApiAuthJwtTokenImpl implements VxApiAuth {
	private static final Logger LOG = LogManager.getLogger(VxApiAuthJwtTokenImpl.class);
	/**
	 * 存放Session中token值key的名字
	 */
	private final static String VX_API_JWT_TOKEN_NAME = "apiToken";
	/**
	 * 存放请求参数中token值key的名字
	 */
	private final static String VX_API_USER_TOKEN_NAME = "apiName";

	private String apiTokenName;// 存在API网关token值的key名字
	private String userTokenName;// 用户请求token值的key名字
	private ParamPositionEnum userTokenScope = ParamPositionEnum.HEADER;// 用户请求token存放的位置,默认为header
	private ContentTypeEnum authFailContentType = ContentTypeEnum.JSON_UTF8;// 认证失败返回结果的contentType,默认json-utf8
	private String authFailResult;// 认证失败返回结果
	
	private JsonArray userKeys;

	@Override
	public void handle(RoutingContext event) {
		String token=event.request().getHeader(apiTokenName);
		String username=event.request().getHeader(userTokenName);
		String userKey=null;
		
		if(username==null||username.equals("")){
			event.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
			.putHeader(HttpHeaderConstant.CONTENT_TYPE, authFailContentType.val()).end(authFailResult);
		}
		
		for(Object object: userKeys){
			if(object instanceof JsonObject){
				if(((JsonObject) object).getString(username) instanceof String){
					userKey=((JsonObject) object).getString(username);
				}
			}
		}
		
		LOG.info("userKey:"+userKey);
		
		if(userKey==null||userKey.equals("")){
			event.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
			.putHeader(HttpHeaderConstant.CONTENT_TYPE, authFailContentType.val()).end(authFailResult);
		}
		
		JWTVerifier verifier=JWT.require(Algorithm.HMAC256(userKey)).build();
		DecodedJWT jwt=null;

		try{
			jwt=verifier.verify(token);
			event.next();
		}catch(Exception e){
			event.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
			.putHeader(HttpHeaderConstant.CONTENT_TYPE, authFailContentType.val()).end(authFailResult);
			
			//throw new RuntimeException("凭证无效或己过期！");
		}
		
		/*
		Session session = event.session();
		if (session == null) {
			event.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
					.putHeader(HttpHeaderConstant.CONTENT_TYPE, authFailContentType.val()).end(authFailResult);
		} else {
			// session中的token
			String apiToken = session.get(apiTokenName) == null ? null : session.get(apiTokenName).toString();
			// 用户request中的token
			String userTokoen = null;
			if (userTokenScope == ParamPositionEnum.HEADER) {
				userTokoen = event.request().getHeader(userTokenName);
			} else {
				userTokoen = event.request().getParam(userTokenName);
			}
			// 检验请求是否正确如果正确放行反则不通过
			if (!StrUtil.isNullOrEmpty(apiToken) && apiToken.equals(userTokoen)) {
				event.next();
			} else {
				event.response().putHeader(HttpHeaderConstant.SERVER, VxApiGatewayAttribute.FULL_NAME)
						.putHeader(HttpHeaderConstant.CONTENT_TYPE, authFailContentType.val()).end(authFailResult);
			}
		}
		*/
	}

	/**
	 * @param obj
	 *          通过一个JsonObject实例化一个对象 <br>
	 *          obj.apiTokenName = API中token的名字<br>
	 *          obj.userTokenName = 用户token在请求中的名字<br>
	 *          obj.userTokenScope =
	 *          用户token在请求中的名字所在的位置枚举类型{@link ParamPositionEnum} 默认在HEADER中<br>
	 *          obj.authFailContentType =
	 *          验证不通过时返回的Content-Type枚举类型{@link ContentTypeEnum} 默认为JSON_UTF8<br>
	 *          obj.authFailResult = 验证不通过时的返回结果 默认为
	 *          {@link ResultFormat}.formatAsNull({@link HTTPStatusCodeMsgEnum}.C401)<br>
	 */
	public VxApiAuthJwtTokenImpl(JsonObject obj) {
		if (obj == null) {
			throw new NullPointerException("JwtToken认证方式的配置文件不能为空!");
		}
		if (obj.getValue("apiTokenName") instanceof String) {
			this.apiTokenName = obj.getString("apiTokenName");
		} else {
			this.apiTokenName = VX_API_JWT_TOKEN_NAME;
		}
		if (obj.getValue("userTokenName") instanceof String) {
			this.userTokenName = obj.getString("userTokenName");
		} else {
			this.userTokenName = VX_API_USER_TOKEN_NAME;
		}

		if(obj.getJsonArray("secretKeys") instanceof JsonArray){
			this.userKeys=obj.getJsonArray("secretKeys");

		}

		if (obj.getValue("userTokenScope") instanceof String) {
			this.userTokenScope = ParamPositionEnum.valueOf(obj.getString("userTokenScope"));
		} else {
			this.userTokenScope = ParamPositionEnum.HEADER;
		}
		if (obj.getValue("authFailContentType") instanceof String) {
			this.authFailContentType = ContentTypeEnum.valueOf(obj.getString("authFailContentType"));
		} else {
			this.authFailContentType = ContentTypeEnum.JSON_UTF8;
		}
		if (obj.getValue("authFailResult") instanceof String) {
			this.authFailResult = obj.getString("authFailResult");
		} else {
			this.authFailResult = ResultFormat.formatAsNull(HTTPStatusCodeMsgEnum.C401);
		}
	}
}
