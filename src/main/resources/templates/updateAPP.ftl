<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="webkit|ie=edge">
    <title>VX-API-UPDATE-APP</title>
    <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css"
          integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
          crossorigin="anonymous">
    <script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
    <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"
            integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
            crossorigin="anonymous"></script>
    <link rel="stylesheet" href="/static/css/main.css">
</head>

<body style="font-size: 16px;min-width: 1250px">
<nav class="navbar navbar-inverse">
    <div class="container-fluid">
       <div class="navbar-header">
			<a href="/static/Application.html" class="navbar-brand"
				style="font-family: 微软雅黑; color: white; font-size: 24px;">VX-API</a>
		</div>
		<span class="login-out"><a href="/loginOut" style="color: white">退出</a></span>
    </div>
</nav>
<div class="container">
    <ol class="breadcrumb">
        <li>
            <a href="javascript:history.go(-1);">应用列表</a>
        </li>
        <li>修改网关应用</li>
    </ol>
    <div class="rightP">
        <div id="apiList" class="cont ">
            <!-- 创建APP -->
            <div id="apiListModal" cur='1'>
                <ul class="console-clearfix">
                    <li class="console-guidebar-default guidebar-current cApiNav cApiNav1" style="width:33.3%;">基本信息
                    </li>

                    <li class="console-guidebar-default cApiNav cApiNav2" style="width:33.3%;">服务设置</li>

                    <li class="console-guidebar-default cApiNav cApiNav3" style="width:33.3%;">参数设置</li>

                </ul>
                <!-- 头部 -->
                <!--基本设置-->
                <div class="createApiPage">
                    <div class="console-panel createAPI1">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">基本配置</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                            <table width="100%" class="descriptionTable">
                                <tr>
                                    <td width="22%" class="ng-binding t-r"><span class="red">* </span>应用名称:</td>
                                    <td>
                                        <input type="text" id="app-name"  disabled="disabled" 
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               value="${context.app.appName}" 
                                               placeholder="请输入应用的名称">
                                    </td>
                                </tr>
                                <tr>
                                    <td class="ng-binding t-r"><span class="red">* </span>应用描述:</td>
                                    <td>
                                        <textarea type="text" id="app-describe"
                                                  class="console-textarea console-width-6 ng-pristine ng-valid"
                                                  maxlength="90" placeholder="请用一句话描述该作用"><#t>${context.app.describe}<#t></textarea>
                                    </td>
                                </tr>
                                <tr>
                                    <td  class="ng-binding t-r">应用的作用域:</td>
                                    <td>
                                        <label class="label_crear"><input name="scope" type="radio" value="0" <#if context.app.scope==0>checked="checked"</#if>/> 测试版</label>
                                        <label class="label_crear">&nbsp;&nbsp;<input name="scope" type="radio" value="1" <#if context.app.scope==1>checked="checked"</#if>/> 预览版</label>
                                        <label class="label_crear">&nbsp;&nbsp;<input name="scope" type="radio" value="2" <#if context.app.scope==2>checked="checked"</#if>/> 正式版</label>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <!--服务设置-->
                    <div class="console-panel console-mt4 createAPI2  hide">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">HTTP服务设置</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                            <table width="100%" class="descriptionTable">
                                <tr>
                                    <td width="22%"  class="ng-binding t-r">
                                        HTTP服务:
                                    </td>
                                    <td>
                                        <label class="label_crear"><input type="checkbox" id="is-create-http" <#if context.app.serverOptions.createHttp== true>checked="checked"</#if>> 开启HTTP服务</label>
                                    </td>
                                </tr>
                                <tr class="http-hide-show">
                                    <td align="right" class="t-r">HTTP服务端口:</td>
                                    <td><input type="text" value="${context.app.serverOptions.httpPort?c}"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="请输入HTTP服务端口号" id="http-port"
                                               value="${context.app.serverOptions.httpPort?c}"
                                               ></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <div class="console-panel console-mt4 createAPI2  hide">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">HTTPS服务设置</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                            <table width="100%" class="descriptionTable">
                                <tr>
                                    <td width="22%"  class="ng-binding t-r">
                                        HTTPS服务:
                                    </td>
                                    <td>
                                        <label class="label_crear"><input type="checkbox" id="is-create-https" <#if context.app.serverOptions.createHttps== true>checked="checked"</#if>> 开启HTTPS服务</label>
                                    </td>
                                </tr>
                                <tr class="https-hide-show" style="display: none">
                                    <td align="right" class="t-r">HTTPS服务端口:</td>
                                    <td><input type="text" value="${context.app.serverOptions.httpsPort?c}"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="请输入HTTPS服务端口号" id="https-port"
                                                value="${context.app.serverOptions.httpsPort?c}"
                                               ></td>
                                </tr>
                                <tr class="https-hide-show" style="display: none">
                                    <td align="right" class="t-r"><span class="red">*</span>HTTPS证书类型:</td>
                                    <td>
                                    <#if context.app.serverOptions.certOptions?exists>
										<label  class="label_crear"><input name="cert-type" type="radio" value="PEM" <#if context.app.serverOptions.certOptions.certType == "PEM">checked="checked"</#if>/> PEM</label> 
										<label  class="label_crear">&nbsp;&nbsp;<input name="cert-type" type="radio" value="PFX"  <#if context.app.serverOptions.certOptions.certType == "PFX">checked="checked"</#if>/> PFX</label>
									<#else>
										<label  class="label_crear"><input name="cert-type" type="radio" value="PEM"  checked="checked"/> PEM</label> 
										<label  class="label_crear">&nbsp;&nbsp;<input name="cert-type" type="radio" value="PFX"/> PFX</label>
									</#if>
								</td>
                                </tr>
                                <tr class="https-hide-show" style="display: none">
                                    <td align="right" class="t-r"><span class="red">*</span>证书key:</td>
                                    <td><input type="text" id="cert-key"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               required="required" placeholder="pfx证书的密码或者pem证书key的路径"
                                               <#if context.app.serverOptions.certOptions?exists> value="${context.app.serverOptions.certOptions.certKey}"</#if>
                                               ></td>
                                </tr>
                                <tr class="https-hide-show" style="display: none">
                                    <td align="right" class="t-r"><span class="red">*</span>证书path:</td>
                                    <td><input type="text" id="cert-path"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               required="required" placeholder="请输入证书所在路径"
                                               <#if context.app.serverOptions.certOptions?exists> value="${context.app.serverOptions.certOptions.certPath}"</#if>
                                               ></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                    <!--跨域设置-->
                    <div class="console-panel console-mt4 createAPI2  hide">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">跨域设置</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                            <table width="100%" class="descriptionTable">
                                <tr>
                                    <td width="22%" class="ng-binding t-r">跨域设置:</td>
                                    <td>
                                        <label class="label_crear"><input type="checkbox" id="is-create-cors" <#if context.app.corsOptions??>checked="checked"</#if>> 开启跨域</label>
                                    </td>
                                </tr>
                                <tr class="cors-hide-show" style="display: none">
                                    <td align="right" class="t-r"><span class="red">*</span>允许的请求源 / allowedOrigin:</td>
                                    <td><input type="text" id="allowedOrigin"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               required="required"
                                               placeholder="允许的请求源:多个以号,隔开"
                                               value="<#if context.app.corsOptions??>${context.app.corsOptions.allowedOrigin!}</#if>" 
                                               ></td>
                                </tr>
                                <tr class="cors-hide-show" style="display: none">
                                    <td align="right" class="t-r">是否允许发送Cookie / allowCredentials:</td>
                                    <td>
	                                    <#if context.app.corsOptions??>
											<#if context.app.corsOptions.allowCredentials == true>
												<label class="label_crear"><input name="allowCredentials" type="radio" value="true"  checked="checked" /> true</label> 
												<label class="label_crear">&nbsp;&nbsp;<input name="allowCredentials" type="radio" value="false"/> false</label>
											<#else>
												<label class="label_crear"><input name="allowCredentials" type="radio" value="true"/> true</label> 
												<label class="label_crear">&nbsp;&nbsp;<input name="allowCredentials" type="radio" value="false" checked="checked" /> false</label>
											</#if>
										<#else>
											<label class="label_crear"><input name="allowCredentials" type="radio" value="true"/> true</label> 
											<label class="label_crear">&nbsp;&nbsp;<input name="allowCredentials" type="radio" value="false" checked="checked" /> false</label>
										</#if>
                                    </td>
                                </tr>
                                <tr class="cors-hide-show" style="display: none">
                                    <td align="right" class="t-r">缓存时间(秒) / maxAgeSeconds:</td>
                                    <td><input type="text" id="maxAgeSeconds"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               required="required" value="<#if context.app.corsOptions??>${context.app.corsOptions.maxAgeSeconds?c}</#if>"></td>
                                </tr>
                                <tr class="cors-hide-show" style="display: none">
                                    <td align="right" class="t-r">允许请求的header / allowedHeaders:</td>
                                    <td><input type="text" id="allowedHeaders"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               required="required"
                                               placeholder="允许请求的header:多个以号,隔开"
                                               value="<#t><#if context.app.corsOptions??>
												<#if context.app.corsOptions.allowedHeaders??>
													<#list context.app.corsOptions.allowedHeaders as header>${header}<#t><#sep>,</#sep><#t>
													</#list>  
												</#if></#if><#t>"
                                               ></td>
                                </tr>
                                <tr class="cors-hide-show" style="display: none">
                                    <td align="right" class="t-r">暴露的header列表 / exposedHeaders:</td>
                                    <td><input type="text" id="exposedHeaders"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               required="required"
                                               placeholder="暴露给浏览器的header列表:多个以号,隔开"
                                                value="<#t><#if context.app.corsOptions??>
												<#if context.app.corsOptions.exposedHeaders??>
													<#list context.app.corsOptions.exposedHeaders as header>${header}<#t><#sep>,</#sep><#t>
													</#list>  
												</#if></#if><#t>"
                                               ></td>
                                </tr>
                                <tr class="cors-hide-show" style="display: none">
                                    <td align="right" class="t-r">允许的请求方式 / allowedMethods :</td>
                                    <td>
	                                    <#assign GET = false>
										<#assign HEAD = false>
										<#assign POST = false>
										<#assign PUT = false>
										<#assign DELETE = false>
										<#assign OPTIONS = false>
										<#assign TRACE = false>
										<#assign CONNECT = false>
										<#assign PATCH = false>
										<#assign OTHER = false>
										<#if context.app.corsOptions??>
											<#if context.app.corsOptions.allowedMethods??>
												<#list context.app.corsOptions.allowedMethods as method>
													<#if method == "GET"><#assign GET = true>
													<#elseif method == "HEAD"><#assign HEAD = true>
													<#elseif method == "POST"><#assign POST = true>
													<#elseif method == "PUT"><#assign PUT = true>
													<#elseif method == "DELETE"><#assign DELETE = true>
													<#elseif method == "OPTIONS"><#assign OPTIONS = true>
													<#elseif method == "TRACE"><#assign TRACE = true>
													<#elseif method == "CONNECT"><#assign CONNECT = true>
													<#elseif method == "PATCH"><#assign PATCH = true>
													<#elseif method == "OTHER"><#assign OTHER = true>
													</#if>
												</#list>
											</#if>
										</#if>
										<label class="label_crear"><input type="checkbox" name="allowedMethods" value="GET" <#if GET == true>checked="checked"</#if>> GET</label>
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="HEAD" <#if HEAD == true>checked="checked"</#if>> HEAD</label> 
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="POST"  <#if POST == true>checked="checked"</#if>> POST</label> 
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="PUT"  <#if PUT == true>checked="checked"</#if>> PUT</label> 
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="DELETE"  <#if DELETE == true>checked="checked"</#if>> DELETE</label> 
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="OPTIONS"  <#if OPTIONS == true>checked="checked"</#if>> OPTIONS</label> 
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="TRACE"  <#if TRACE == true>checked="checked"</#if>> TRACE</label> 
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="CONNECT"  <#if CONNECT == true>checked="checked"</#if>> CONNECT</label> 
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="PATCH"  <#if PATCH == true>checked="checked"</#if>> PATCH</label> 
										<label class="label_crear">&nbsp;&nbsp;<input type="checkbox" name="allowedMethods" value="OTHER" <#if OTHER == true>checked="checked"</#if>> OTHER</label>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <!-- 参数设置 -->
                    <div class="console-panel console-mt4 createAPI3 hide">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">参数配置</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                            <table width="100%" class="descriptionTable">
                                <tr>
                                    <td class="t-r" style="width: 30%">请求主体的最大长度既上传文件等大小限制:</td>
                                    <td><input type="text" id="contentLength"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="请求主体的最大长度既上传文件等大小限制,默认:-1无限制"
                                               value="${context.app.contentLength?c}"
                                               ></td>
                                </tr>
                                <tr>
                                    <td class="t-r">会话超时时间(毫秒):</td>
                                    <td><input type="text" id="sessionTimeOut"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="会话超时时间(毫秒)默认:1800000"
                                               value="${context.app.sessionTimeOut?c}"
                                               ></td>
                                </tr>
                                <tr>
                                    <td class="t-r">会话的cookie名称:</td>
                                    <td><input type="text" id="sessionCookieName"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="会话的cookie名称默认:vx-api.session"
                                               value="${context.app.sessionCookieName}"
                                               ></td>
                                </tr>
                                <tr>
                                    <td class="t-r" title="API与后台服务器 既你自己的服务器 交互是否使用keepAlive">API与后台服务器是否使用keepAlive:
                                    </td>
                                    <td>
	                                    <label class="label_crear"><input name="keepAlive" type="radio" value="true" <#if context.app.keepAlive == true>checked="checked"</#if>/> true</label> 
										<label class="label_crear">&nbsp;&nbsp;<input name="keepAlive" type="radio" value="false"  <#if context.app.keepAlive == false>checked="checked"</#if>/> false</label>	
									</td>
                                </tr>
                                <tr>
									<td class="t-r" title="HTTP对象解码器的缓冲区大小单位byte, 默认128byte">HTTP对象解码器的缓冲区大小(byte):</td>
									<td><input type="text" id="decoderInitialBufferSize" class="console-textbox console-width-4 ng-pristine ng-valid" placeholder="HTTP对象解码器的缓冲区大小单位byte, 默认128byte" value="${context.app.decoderInitialBufferSize?c}"></td>
								</tr>
                                <tr>
                                    <td class="t-r" title="API与后台服务器 既你自己的服务器 交互的连接池数量 maxPoolSize">API与后台服务器交互的连接池数量:
                                    </td>
                                    <td><input type="text" id="maxPoolSize"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="API与后台服务器 既你自己的服务器 交互的连接池数量 默认:5"
                                               value="${context.app.maxPoolSize?c}"
                                               ></td>
                                </tr>
                                <tr>
                                    <td class="t-r" title="maxInitialLineLength">参数值最大总长度:</td>
                                    <td><input type="text" id="maxInitialLineLength"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="参数值最大总长度 默认:4096"
                                               value="${context.app.maxInitialLineLength?c}"
                                               ></td>
                                </tr>
                                <tr>
                                    <td class="t-r" title="maxHeaderSize">header参数值最大总长度:</td>
                                    <td><input type="text" id="maxHeaderSize"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="header参数值最大总长度 默认:4096"
                                               value="${context.app.maxHeaderSize?c}"
                                               ></td>
                                </tr>
                                <tr>
                                    <td class="t-r" title="应用服务拓展配置">应用服务拓展配置:</td>
                                    <td>
                                        <div>
                                            <a href="http://vertx.io/docs/apidocs/io/vertx/core/http/HttpServerOptions.html" target="_blank">参考vert.x服务配置</a></div>
                                        <textarea id="custom" class="console-textarea console-width-6 ng-pristine ng-valid" style="resize: none;" rows="4" placeholder="参考Vert.x中的HttpServerOptions服务配置"><#t>${context.app.serverOptions.custom!}<#t></textarea>
                                    </td>
                                </tr>
                            </table>
                        </div>

                    </div>
                    <!-- createAPI3 End -->
                </div>


                <div class="console-panel-body console-p4 console-mt4 ">
                    <div class="console-form">
                        <div class="console-form-row">
                            <div class="console-form-body">
                                    <span class="console-button-wrap ng-isolate-scope">
                                        <a class="console-button console-button-default console-button-medium hide cp-lPage">
                                            <span ng-transclude="">
                                                <span class="ng-scope ng-binding">上一步</span>
                                            </span>
                                        </a>
                                    </span>
                                    <span class="console-ml2 console-button-wrap ng-isolate-scope" theme="blue">
                                        <a class="console-button console-button-blue console-button-medium cp-nPage">
                                            <span ng-transclude="">
                                                <span class="ng-scope ng-binding">下一步</span>
                                            </span>
                                        </a>
                                    </span>
                                    <span class="console-ml2 console-button-wrap ng-isolate-scope " theme="blue"
                                          onclick="updateAPP()">
                                        <a class="console-button console-button-blue console-button-medium console-button-disabled hide cp-sPage">
                                            <span>
                                                <span class="ng-scope ng-binding">保存</span>
                                            </span>
                                        </a>
                                    </span>
                            </div>
                        </div>
                    </div>
                </div>


            </div>


        </div>
        <!-- apiListModal End -->


    </div>
    <!-- rightP End -->
<input type="hidden" id="time" value="${context.app.time!}">
</div>

<script src="/static/js/createAPP.js"></script>
<script>

</script>
</body>

</html>