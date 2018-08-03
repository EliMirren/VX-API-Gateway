<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="webkit|ie=edge">
    <title>VX-API-UPDATE-API</title>
    <link rel="stylesheet" href="/static/framework/bootstrap.min.css">
    <script src="/static/framework/jquery.min.js"></script>
    <script src="/static/framework/bootstrap.min.js"></script>
    <link rel="stylesheet" href="/static/css/main.css">
</head>

<body style="font-size: 16px;min-width: 1250px">
<nav class="navbar navbar-inverse">
    <div class="container-fluid">
        <div class="navbar-header">
            <a href="/static/Application.html" class="navbar-brand" style="font-family: 微软雅黑;color: white;font-size: 24px;">VX-API</a>
        </div>
        <span class="login-out">退出</span>
    </div>
</nav>
<input type="hidden" id="appName" value="${context.api.appName}">
<input type="hidden" id="apiCreateTime" value="${context.api.apiCreateTime!}">
<div class="container">
    <ol class="breadcrumb">
        <li>
            <a href="/static/Application.html">应用列表</a>
        </li>
        <li>
            <a href="javascript:history.go(-1);">API详情</a>
        </li>
        <li>修改API</li>
    </ol>
    <div class="rightP">
        <div id="apiList" class="cont ">
            <!-- 创建API -->
            <div id="apiListModal" cur='1'>
                <ul class="console-clearfix">
                    <li class="console-guidebar-default guidebar-current cApiNav cApiNav1">基本信息</li>

                    <li class="console-guidebar-default cApiNav cApiNav2">定义API请求</li>

                    <li class="console-guidebar-default cApiNav cApiNav3">定义API后端服务</li>

                    <li class="console-guidebar-default cApiNav cApiNav4">定义返回结果</li>

                </ul>
                <!-- 头部 -->

                <div class="createApiPage">
                    <!-- 1.25 S! -->
                    <div class="modal " id="senateEdit" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
                        <div class="modal-dialog" role="document">
                            <div class="modal-content">
                                <div class="modal-header">
                                    <button type="button" class="close" data-dismiss="modal"  onclick='modalClose(this)' aria-label="Close">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                    <h4 class="modal-title" style="text-align:left;" id="exampleModalLabel">编辑更多</h4>
                                </div>

                                <div console-scope="contentScope" class="ng-scope console-dialog-body-container">

                                    <div class="console-form console-mb2 ng-scope">

                                        <div class="console-form-row senateType">
                                            <div class="console-form-header ng-binding">类型</div>
                                            <div class="console-form-body">
                                                <div class="console-form-tip-inline ng-binding senateTit">String</div>
                                            </div>
                                        </div>

                                        <div class="console-form-row senateMaxlength">
                                            <div class="console-form-header ng-binding">最大长度</div>
                                            <div class="console-form-body">
                                                <input type="number"
                                                       class="console-textbox console-width-2 ng-pristine ng-valid ng-valid-number senateMaxlen">
                                            </div>
                                        </div>

                                        <div class="console-form-row  senateEnum">
                                            <div class="console-form-header ng-binding">枚举</div>
                                            <div class="console-form-body">
                                                <input type="text" style="width: 74.5% !important;"
                                                       class="console-textbox console-width-12 ng-pristine ng-valid"
                                                       placeholder="用逗号(,)隔开">
                                            </div>
                                        </div>

                                        <div class="console-form-row  ng-hide  senateMinv">
                                            <div class="console-form-header ng-binding">最小值</div>
                                            <div class="console-form-body">
                                                <input class="console-textbox console-width-3 ng-pristine ng-valid ng-valid-number">
                                            </div>
                                        </div>

                                        <div class="console-form-row ng-hide senateMaxv">
                                            <div class="console-form-header ng-binding">最大值</div>
                                            <div class="console-form-body">
                                                <input class="console-textbox console-width-3 ng-pristine ng-valid ng-valid-number">
                                            </div>
                                        </div>


                                        <div class="console-form-row senateVify">
                                            <div class="console-form-header ng-binding ">参数验证</div>
                                            <div class="console-form-body">
                                                    <textarea style="width:74.5% !important;" placeholder="输入正则表达式"
                                                              class="console-textarea console-width-12 ng-pristine ng-valid"
                                                              rows="4"></textarea>
                                            </div>
                                        </div>
                                    </div>


                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-default" onclick='modalClose(this)'
                                            data-dismiss="modal">关闭
                                    </button>
                                    <button type="button" class="btn btn-success senateEnter">确定</button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- 1.25 E! -->
                    <div class="console-panel createAPI1">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">基本配置</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                            <div class="console-form">
                                <div class="console-form-row clearfloat">
                                    <div class="console-form-header ng-binding">
                                        <span class="red">* </span>API名称
                                    </div>
                                    <div class="console-form-body">
                                        <input type="text" id="apiName" disabled="disabled" style="background-color: #EEEEEE;"
                                               class="console-textbox console-width-4 ng-pristine ng-valid"
                                               placeholder="请输入API的名称" value="${context.api.apiName}">
                                    </div>
                                </div>
                                <div class="console-form-row clearfloat">
                                    <div class="console-form-header ng-binding">
                                        <span class="red">* </span>API描述
                                    </div>
                                    <div class="console-form-body">
                                        <textarea type="text" id="apiDescribe" class="console-textarea console-width-6 ng-pristine ng-valid"
                                                  maxlength="90" placeholder="请用一句话描述该API的作用">${context.api.apiDescribe!}</textarea>
                                    </div>
                                </div>
                                <!--安全认证配置,当有新的认证方式只需要在select中添加一个option值,值的内容时处理器在工厂中的名字-->
                                <div class="console-form-row clearfloat">
                                    <div class="console-form-header ng-binding">安全认证</div>
                                    <div class="console-form-body">
                                        <select id="auth-options-name"
                                                class="console-selectbox console-width-4 ng-pristine ng-valid">
                                                <#if context.api.authOptions?exists>
                                               		<option value="${context.api.authOptions.inFactoryName}" class="ng-binding">${context.api.authOptions.inFactoryName}</option>
                                                </#if>
                                            <option value="none" class="ng-binding">不认证</option>
                                            <option value="sessionTokenAuth" class="ng-binding">session-token</option>
                                            <option value="jwtTokenAuth" class="ng-binding">JWT-token</option>
                                        </select>
                                    </div>
                                </div>

                                <div class="console-form-row clearfloat auth-hide-show">
                                    <div class="console-form-header ng-binding">
                                        <span class="red">* </span>认证配置文件
                                    </div>
                                    <div class="console-form-body">
                                        <textarea type="text" id="auth-options-config" rows="6" class="console-textarea console-width-6 ng-pristine ng-valid" placeholder="请输入认证需要的配置文件json格式"
                                        ><#if context.api.authOptions?exists>${context.api.authOptionBody!}</#if>
                                        </textarea>
                                    </div>
                                </div>

                                <div class="console-form-row clearfloat">
                                    <div class="console-form-header ng-binding">访问限制/单位</div>
                                    <div class="console-form-body">
                                        <select id="limitUnit"
                                                class="console-selectbox console-width-4 ng-pristine ng-valid">
                                                <#if context.api.limitUnit?exists>
                                                <option value="${context.api.limitUnit}" class="ng-binding">${context.api.limitUnit}</option>
                                                </#if>
                                            <option value="none" class="ng-binding">不限制</option>
                                            <option value="DAYS" class="ng-binding">一天</option>
                                            <option value="HOURS" class="ng-binding">一小时</option>
                                            <option value="MINUTES" class="ng-binding">一分钟</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="console-form-row clearfloat limit-hide-show">
                                    <div class="console-form-header ng-binding">API访问次数</div>
                                    <div class="console-form-body">
                                        <input type="text" id="apiLimit" <#if context.api.limitUnit?exists>value="${context.api.apiLimit}"</#if> placeholder="请输入API在限制单位中最大访问次数,默认-1无限制"
                                               class="console-textbox console-width-4 ng-pristine ng-valid">
                                    </div>
                                </div>
                                <div class="console-form-row clearfloat limit-hide-show">
                                    <div class="console-form-header ng-binding">IP访问次数</div>
                                    <div class="console-form-body">
                                        <input type="text" id="ipLimit" <#if context.api.limitUnit?exists>value="${context.api.ipLimit}"</#if> placeholder="请输入用户IP在限制单位中最大访问次数,默认-1无限制"
                                               class="console-textbox console-width-4 ng-pristine ng-valid">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!--前置与后置处理器,当有新的处理器只需要在select中添加一个option值,值的内容时处理器在工厂中的名字-->
                    <div class="console-panel createAPI1" style="margin-top:15px;">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">前置与后置处理器</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                            <div class="console-form-row clearfloat">
                                <div class="console-form-header ng-binding">前置处理器</div>
                                <div class="console-form-body">
                                    <select id="beforeHandler"
                                            class="console-selectbox console-width-4 ng-pristine ng-valid">
                                        <#if context.api.beforeHandlerOptions?exists>
                                            <option value="${context.api.beforeHandlerOptions.inFactoryName}" class="ng-binding">${context.api.beforeHandlerOptions.inFactoryName}</option>
                                        </#if>
                                        <option value="none" class="ng-binding">不启用</option>
                                        <!--<option value="这里填写前置处理器在工厂中的名字" class="ng-binding">option</option>-->
                                    </select>
                                </div>
                            </div>

                            <div class="console-form-row clearfloat beforeHandler-hide-show">
                                <div class="console-form-header ng-binding">
                                    <span class="red">* </span>前置处理器配置文件
                                </div>
                                <div class="console-form-body">
                                        <textarea type="text" id="beforeHandlerOptions" rows="6" class="console-textarea console-width-6 ng-pristine ng-valid" placeholder="请输入前置处理器需要的配置文件json格式"
                                        ><#if context.api.beforeHandlerOptions?exists>${context.api.beforeHandlerOptionsBody!}</#if></textarea>
                                </div>
                            </div>

                            <div class="console-form-row">
                                <div class="console-form-header ng-binding">后置处理器</div>
                                <div class="console-form-body">
                                    <select id="afterHandler"
                                            class="console-selectbox console-width-4 ng-pristine ng-valid">
                                        <#if context.api.afterHandlerOptions?exists>
                                            <option value="${context.api.afterHandlerOptions.inFactoryName}" class="ng-binding">${context.api.afterHandlerOptions.inFactoryName}</option>
                                        </#if>
                                        <option value="none" class="ng-binding">不启用</option>
                                        <!--<option value="这里填写后置处理器在工厂中的名字" class="ng-binding">session-token</option>-->
                                    </select>
                                </div>
                            </div>

                            <div class="console-form-row clearfloat afterHandler-hide-show">
                                <div class="console-form-header ng-binding">
                                    <span class="red">* </span>后置处理器配置文件
                                </div>
                                <div class="console-form-body">
                                        <textarea id="afterHandlerOptions" type="text" rows="6" class="console-textarea console-width-6 ng-pristine ng-valid" placeholder="请输入后置处理器需要的配置文件json格式"
                                        ><#if context.api.afterHandlerOptions?exists>${context.api.afterHandlerOptionsBody!}</#if></textarea>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="console-panel console-mt4 createAPI2  hide">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">请求基础定义</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">

                            <table width="100%" class="descriptionTable">
                                <tbody>
                                <tr>
                                    <td style="padding-right:20px;text-align: right;border: 0" class="ng-binding">
                                        <span class="red">* </span>请求Path
                                    </td>
                                    <td style="border: 0;padding-bottom:0;">
                                        <input type="text" id="path" value="${context.api.path}" class="console-textbox console-width-4 ng-pristine ng-valid"  placeholder="请输入Path 示例: /test">
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td style="padding-top: 0;">
                                        <span class="console-form-tip-block ng-binding">
                                        	请求Path必须以 / 开头,如果Path带请求参数需要加上 : 号,示例:[/test/:name]该Path会取name位置的值<br>
                                        	基于路径前缀通配请求可以使用/*,示例:[/path/*],所有以/path/起头的路径都会被会被匹配<br>
                                        	如果网关应用需要端口号复用需保证Path的唯一性,否则有可能在暂停应用的时候误停相同Path的API	
										</span>
                                    </td>
                                </tr>

                                <tr>
                                    <td style="padding-right:20px;text-align: right;border: 0" class="ng-binding">
                                     	   请求类型/consumes
                                    </td>
                                    <td style="border: 0;padding-bottom:1px;">
                                        <input type="text" id="consumes" class="console-textbox console-width-4 ng-pristine ng-valid" 
                                        placeholder="请输入接收的处理类型,多个以,号隔开,默认所有类型"
	                                        <#if context.api.consumes?exists>
	                                        value="
	                                         <#list context.api.consumes as item>
												<#t>${item}<#t><#sep>,</#sep>
											 </#list>
											"
										</#if>
                                        >
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td style="padding-top: 0;">
                                                <span class="console-form-tip-block ng-binding">设置API只处理请求Content-Type:为指定的类型的请求,* 代表任意 示例: */json
                                                    <br/>代表该API只处理请求Content-Type:任意/json结尾的数据格式,默认处理所有类型,多个以,号隔开 </span>
                                    </td>
                                </tr>

                                <tr>
                                    <td style="padding-right:20px;text-align: right;" class="ng-binding">HTTP Method
                                    </td>
                                    <td>
                                        <select type="text" id="method"
                                                class="console-selectbox console-width-4 ng-valid ng-dirty">
                                            <option value="${context.api.method}">${context.api.method}</option>
                                            <option value="ALL">ALL</option>
                                            <option value="GET">GET</option>
                                            <option value="HEAD">HEAD</option>
                                            <option value="PUT">PUT</option>
                                            <option value="POST">POST</option>
                                            <option value="DELETE">DELETE</option>
                                            <option value="PATCH">PATCH</option>
                                            <option value="TRACE">TRACE</option>
                                            <option value="CONNECT">CONNECT</option>
                                            <option value="PATCH">PATCH</option>
                                            <option value="OPTIONS">OPTIONS</option>
                                            <option value="OTHER">OTHER</option>
                                        </select>
                                    </td>
                                </tr>
                                </tbody>
                            </table>

                            <div class="console-panel console-mt4">
                                <div class="console-panel-header">
                                    <span class="console-panel-header-line"></span>
                                    <div class="console-float-left ng-binding">入参定义</div>

                                </div>
                                <div class="request-param-setter ng-isolate-scope ng-pristine ng-valid newParam-setter">
                                    <table>
                                        <tbody>
                                        <tr>
                                            <th width="150" class="ng-binding">参数名</th>
                                            <th width="150" class="ng-binding">参数位置</th>
                                            <th width="150" class="ng-binding">类型</th>
                                            <th width="60" class="ng-binding">必填</th>
                                            <th class="ng-binding">默认值</th>
                                            <th class="ng-binding">描述</th>
                                            <th class="ng-binding">操作</th>
                                        </tr>
                                        
                                        <#if context.api.enterParam?exists>
                   						 	<#list context.api.enterParam as item>
		                                        <tr class="ng-scope" 
		                                        	<#if item.checkOptions?exists>
		                                        	<#if item.checkOptions.maxLength?exists> data-maxlen="${item.checkOptions.maxLength?c}" </#if> 
				                        			<#if item.checkOptions.maxValue?exists> data-maxv="${item.checkOptions.maxValue?c}"</#if>
				                        			<#if item.checkOptions.minValue?exists> data-minv="${item.checkOptions.minValue?c}"</#if>
				                        			<#if item.checkOptions.regex?exists> data-paramvify="${item.checkOptions.regex}"</#if>
				                        			<#if item.checkOptions.enums?exists> data-enum="<#t><#list item.checkOptions.enums as item>${item}<#t> <#sep>,</#sep></#list>"
													</#if>
														data-type="${item.paramType!}"
		                                        	</#if>
		                                        >
							                        <td><input value="${item.paramName!}" class="console-textbox console-width-12 ng-pristine ng-valid-pattern ng-invalid ng-invalid-required paramName" type="text"></td>
							                        <td>
							                            <select class="console-selectbox console-width-12 ng-pristine ng-valid paramChange" onchange="paramPsHandle(this)">
							                                <option value="${item.position!}">${item.position!}</option>
							                                <option value="QUERY">QUERY</option>
							                                <option value="BODY">BODY</option>
							                                <option value="PATH">PATH</option>
							                                <option value="HEADER">HEADER</option>
							                            </select>
							                        </td>
							                        <td>
							                            <select class="console-selectbox console-width-12 ng-pristine ng-valid psType paramChange2">
							                                <option value="${item.paramType!}">${item.paramType!}</option>
							                                <option value="String">String</option>
							                                <option value="Integer">Integer</option>
							                                <option value="Long">Long</option>
							                                <option value="Float">Float</option>
							                                <option value="Double">Double</option>
							                                <option value="Boolean">Boolean</option>
							                                <option value="JsonObject">JsonObject</option>
							                                <option value="JsonArray">JsonArray</option>
							                            </select>
							                        </td>
							                        <td class="console-text-align-center">
							                            <input class="ng-pristine ng-valid  paramPosition" <#if item.isNotNull == true>checked="checked"</#if> <#if item.position == "PATH">disabled="disabled"</#if> type="checkbox">
							                        </td>
							                        <td>
							                            <input value="${item.def!}" class="console-textbox console-width-12 ng-pristine ng-valid paramDefault " type="text">
							                        </td>
							                        <td>
							                            <input value="${item.describe!}" class="console-textbox console-width-12 ng-pristine ng-valid paramDescribe" type="text">
							                        </td>
							                        <td>
							                                <span style="white-space:nowrap">      
							                                  <a id="editMore" href="javascript:;" class="ng-scope  " onclick="senateEditMore(this)">编辑更多</a>| 
							                                  <a href="javascript:;" class="ng-scope  " onclick="delParam(this)">移除</a>
							                                </span>
							                        </td>
							                    </tr>
							                </#list>
							            </#if>
                                        
                                        </tbody>
                                    </table>
                                    <div class="pagination-gird-container console-clearfix">
                                        <span class=" console-button-wrap ng-isolate-scope" icon="add" size="tiny" theme="green" style="width: 100%;">
                                           	<label style="float: left;cursor: pointer;font-weight: normal;margin: 5px 0 0 10px;">
                                           		<input type="checkbox" id="passBody"<#if context.api.passBody?exists><#if context.api.passBody == true >checked="checked"</#if></#if>  onclick="passBodyFun()"> 透传Body
                                       		</label>
                                            <label style="float: left;cursor: pointer;font-weight: normal;margin: 5px 0 0 10px;" title="Body参数可以被Query获取,当Content-Type=application/x-www-form-urlencoded时有效,既query可以获取URL中的参数也可以获取body里面的参数">
                                            	<input type="checkbox" id="bodyAsQuery" <#if context.api.bodyAsQuery?exists><#if context.api.bodyAsQuery == true >checked="checked"</#if></#if>  > Body参数可以被Query获取
                                            </label>
                                            <a class="console-button console-button-green console-button-tiny addParams" style="width: 85px">
                                                <i ng-if="!!icon" class="icon-add"></i>
                                                <span>
                                                    <span class="ng-scope ng-binding">增加一条</span>
                                                </span>
                                            </a>
                                        </span>
                                    </div>
                                    <div class="pagination-gird-container request-param-setter-errormessage ng-binding ng-hide">
                                        <i class="icon-no-2 console-mr1 console-inline-block"></i>
                                    </div>
                                </div>
                            </div>


                        </div>
                    </div>


                    <!-- 定义API后端服务 -->
                    <div class="console-panel console-mt4 createAPI3 hide">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">后端基础定义</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                            <div class="console-form">
                                <table width="100%" class="descriptionTable">
                                    <tr>
                                        <td width="18%" style="padding-right:20px;text-align: right;"
                                            class="ng-binding">
                                         	   后端服务类型
                                        </td>
                                        <td>
                                            <label class="console-radio" onclick="http_httpsOnClick()">
                                                <input type="radio" <#if context.api.serverEntrance.serverType == "HTTP_HTTPS"> checked="checked" </#if> name="backendType"
                                                       value="HTTP_HTTPS"  id="backendType_http_s"
                                                       class="ng-valid ng-dirty">
                                                <span>HTTP/HTTPS</span>
                                            </label>
                                            <label class="console-radio" style="margin-left:10px;"
                                                   onclick="redirectOnClick()">
                                                <input type="radio" name="backendType"  <#if context.api.serverEntrance.serverType == "REDIRECT"> checked="checked" </#if>  value="REDIRECT"
                                                       class="ng-valid ng-dirty">
                                                <span>页面跳转</span>
                                            </label>
                                            <label class="console-radio" style="margin-left:10px;"
                                                   onclick="varsOnClick()">
                                                <input type="radio" name="backendType" <#if context.api.serverEntrance.serverType == "CUSTOM"> checked="checked" </#if> value="CUSTOM"
                                                       class="ng-valid ng-dirty">
                                                <span>自定义服务</span>
                                            </label>
                                        </td>
                                    </tr>
                                    <tbody class="http_https_body">
                                    <tr>
                                        <td align="right" style="padding-right:20px;text-align: right;">HTTP Method</td>
                                        <td>
                                            <select class="console-selectbox console-width-4 ng-pristine ng-valid"
                                                    id="serverMethod">
                                                <#if context.api.serverEntrance.serverType == "HTTP_HTTPS">  <option value="${context.api.serverEntrance.body.method}" selected="selected">${context.api.serverEntrance.body.method}</option></#if>
                                                <option value="GET">GET</option>
                                                <option value="POST">POST</option>
                                                <option value="OPTIONS">OPTIONS</option>
                                                <option value="HEAD">HEAD</option>
                                                <option value="PUT">PUT</option>
                                                <option value="DELETE">DELETE</option>
                                                <option value="TRACE">TRACE</option>
                                                <option value="CONNECT">CONNECT</option>
                                                <option value="PATCH">PATCH</option>
                                                <option value="OTHER">OTHER</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td align="right" style="padding-right:20px;text-align: right;"
                                            class="ng-binding">后端超时
                                        </td>
                                        <td>
                                            <input type="text"  <#if context.api.serverEntrance.serverType == "HTTP_HTTPS"> value="${context.api.serverEntrance.body.timeOut?c}" <#else> value="6000" </#if> class="console-textbox console-width-1 ng-pristine ng-valid" style="border-radius: 0px;" id="serverTimeOut" maxlength="7">
                                            <span>ms</span>
                                        </td>
                                    </tr>
                                    <tr class="balance-hide-show">
                                        <td width="18%" style="padding-right:20px;text-align: right;"
                                            class="ng-binding">
                                          	  负载均衡策略
                                        </td>
                                        <td>
                                            <label class="console-radio">
                                                <input type="radio" checked="checked" name="balanceType"
                                                       value="POLLING_AVAILABLE" class="ng-valid ng-dirty">
                                                <span>轮询可用连接 </span>
                                            </label>
                                            <label class="console-radio" <#if context.api.serverEntrance.serverType == "HTTP_HTTPS"> <#if context.api.serverEntrance.body.balanceType == "IP_HASH"> checked="checked" </#if></#if> style="margin-left:10px;">
                                                <input type="radio" name="balanceType" value="IP_HASH"
                                                       class="ng-valid ng-dirty">
                                                <span>IP哈希化 </span>
                                            </label>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td align="right" style="padding-right:20px;text-align: right;"
                                            class="ng-binding" title="当后台连接失败大于2次时,该服务URL会标记为不可用,系统会根据重试时间测试该URL是否可用,如果可用则改为可用">
                                            	请求服务失败重试时间:
                                        </td>
                                        <td>
                                            <input type="text" 
												   <#if context.api.serverEntrance.serverType == "HTTP_HTTPS"> value="${context.api.serverEntrance.body.retryTime?c}" <#else> value="30000" </#if>
                                                   class="console-textbox console-width-1 ng-pristine ng-valid"
                                                   style="border-radius: 0px;" id="serverRetryTime" maxlength="7" 
                                                   placeholder="请输入当后台连接失败大于2次时的重试时间">
                                            <span>ms</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding-right:20px;text-align: right;border-right: 1px solid #e1e6eb;border-bottom: none;"
                                            class="ng-binding">
                                            <span class="red">* </span>后端服务URL
                                        </td>
                                        <td style="border: none;padding-top: 0px;">
                                            <span class="console-form-tip-block ng-binding">如果有Path请求参数需要加上 : 号,示例:[http://127.0.0.1/test/:name]该URL会填充name位置的值<br></span>
                                       		<span>权重表示该URL被访问的次数,比如A=1,B=3,在4次请求中A被请求1次,B被请求3次,IP哈希化时,A被分配1次,B被分配3次</span>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="border-right: 1px solid #e1e6eb;border-bottom: none;"></td>
                                        <td style="padding-top: 3px; border: none" class="input-server-box">
                                            <input type="text"
                                                   class="console-textbox console-width-4 ng-pristine ng-valid input-server-url"
                                                   placeholder="请输入URL 示例: http://127.0.0.1/test">
                                           	权重: <input type="text" class="console-textbox input-server-weight"  placeholder="权重值" style="width: 60px">
                                            <a class="console-button console-button-green console-button-tiny"
                                               style="width:auto;" onclick='addServerURL()'>
                                            <span>
                                                <span class="ng-scope ng-binding">增加一条</span>
                                            </span>
                                            </a>
                                            <a style="white-space:nowrap"><span style='cursor:pointer;' class="" onclick='testServerURL(this)'> 测试连接</span></a>
                                        </td>
                                    </tr>
                                    <#if context.api.serverEntrance.body.serverUrls?exists>
										<#list context.api.serverEntrance.body.serverUrls as item >
							                <tr>
							                    <td style="border-right: 1px solid #e1e6eb;border-bottom: none;"></td>
							                     <td style="padding-top: 3px; border: none"  class="input-server-box">
							                          <input type="text" value="${item.url}" class="console-textbox console-width-4 ng-pristine ng-valid input-server-url" placeholder="请输入URL 示例: http://127.0.0.1/test"> 
						                           	     权重: <input type="text" value="${item.weight}" class="console-textbox input-server-weight"  placeholder="权重值" style="width: 60px">
							                          <a style="white-space:nowrap"><span style='cursor:pointer;' class="" onclick='delServerURL(this)'> 移除 </span></a> <a style="white-space:nowrap">| <span style='cursor:pointer;' class="" onclick='testServerURL(this)'> 测试连接</span></a>
							                     </td>
							                </tr>
						                </#list>    
					                </#if>                                
                                    </tbody>
                                    <tbody class="vars_body" style="display: none;">
	                                    <tr>
	                                        <td align="right" style="padding-right:20px;text-align: right;">变量类型</td>
	                                        <td>
	                                           <select class="console-selectbox console-width-4 ng-pristine ng-valid"
	                                                    id="custom_server_type">
	                                                <option value="GET_SERVER_UNIX_TIMESTAMP" selected="selected">返回UNIX时间戳</option>
	                                                <option value="GET_CONSTANT_VALUE">返回自定义变量</option>
	                                                <option value="SESSION_TOKEN_GRANT_AUTH">Session-Token授权</option>
	                                                 <#if context.api.serverEntrance.serverType == "CUSTOM">
	                                              		<option value="${context.api.customFactoryName}" selected="selected">${context.api.customFactoryName}</option>
	                                                </#if>
	                                            </select>
	                                            <span class="custom_server_type_tips"></span>
	                                        </td>
	                                    </tr>
	                                    <tr>
	                                        <td align="right" style="padding-right:20px;text-align: right;"
	                                            class="ng-binding">配置文件
	                                        </td>
	                                        <td>
	                                            <textarea id="custom-option" type="text" rows="6"
	                                                      class="console-textarea console-width-6 ng-pristine ng-valid"
	                                                      placeholder="请输入自定义服务处理器需要的配置文件json格式" style="width: 600px;"><#t><#if context.api.serverEntrance.serverType == "CUSTOM"><#t>${context.api.customBody}<#t><#else>{"resultFormat":"$(val)"}</#if></textarea>
	                                        </td>
	                                    </tr>
                                    </tbody>
                                    <tbody class="redirect_body" style="display: none;">
                                    <tr>
                                        <td align="right" style="padding-right:20px;text-align: right;"
                                            class="ng-binding">跳转页面的URL
                                        </td>
                                        <td>
                                            <input type="text" id="redirect_url"
                                            	   value="<#if context.api.serverEntrance.serverType == "REDIRECT">${context.api.serverEntrance.body.url!}</#if>"
                                                   class="console-textbox console-width-1 ng-pristine ng-valid"
                                                   style="border-radius: 0px;" placeholder="请输入跳转的页面"> 特殊文字(例如中文)需要先URL编码
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>

                            </div>
                        </div>


                        <div class="console-panel console-mt4">
                            <div class="console-panel-header">
                                <span class="console-panel-header-line"></span>
                                <div class="console-float-left ng-binding">后端服务参数配置</div>

                            </div>
                            <div class="request-param-setter ng-isolate-scope ng-pristine ng-valid txt-center bParam-config">
                                <table>
                                    <tbody>
                                    <tr>
                                        <th class="ng-binding">后端参数名称</th>
                                        <th class="ng-binding">后端参数位置</th>
                                        <th class="ng-binding">对应入参名称</th>
                                        <th class="ng-binding">对应入参位置</th>
                                        <th class="ng-binding">对应入参类型</th>
                                        <th class="ng-binding">操作</th>
                                    </tr>
                                   
                                    </tbody>
                                </table>
                                <div class="pagination-gird-container request-param-setter-errormessage ng-binding ng-hide">
                                    <i class="icon-no-2 console-mr1 console-inline-block"></i>
                                </div>
                            </div>
                        </div>

                        <div class="console-panel console-mt4">
                            <div class="console-panel-header">
                                <span class="console-panel-header-line"></span>
                                <div class="console-float-left ng-binding clearfloat">透传用户请求参数

                                </div>
                            </div>
                            <div class="api-param-map-setter passParamBody">
                                <table>
                                    <tbody>
                                    <tr>
                                        <th width="20%" class="ng-binding">请求参数名字</th>
                                        <th width="20%" class="ng-binding">请求参数位置</th>
                                        <th width="20%" class="ng-binding">后端参数名字</th>
                                        <th class="ng-binding">后端参数位置</th>
                                        <th width="10%" class="ng-binding">操作</th>
                                    </tr>
                                    <#if context.api.serverEntrance.body.params?exists>
				                    	<#list context.api.serverEntrance.body.params as item>
				                    		<#if item.type == 2>
					                    		<tr class="ng-scope">
			                                        <td><input value="${item.apiParamName!}" class="console-textbox console-width-12 ng-pristine ng-valid ng-valid-pattern"></td>
			                                        <td>
			                                            <select class="console-selectbox console-width-12 ng-pristine ng-valid isPassSelectBody">
				                                            <option value="QUERY" <#if item.apiParamPosition =="QUERY">selected="selected"</#if>>QUERY</option> 
				                                            <option value="PATH" <#if item.apiParamPosition =="PATH">selected="selected"</#if>>PATH</option> 
				                                            <option value="HEADER" <#if item.apiParamPosition =="HEADER">selected="selected"</#if>>HEADER</option>
			                                            </select>
			                                        </td>
			                                        <td><input value="${item.serParamName!}" class="console-textbox console-width-12 ng-pristine ng-valid"></td>
			                                        <td>
			                                        	<select class="console-selectbox console-width-12 ng-pristine ng-valid isPassSelectBody">
				                                        	<option value="QUERY" <#if item.serParamPosition =="QUERY">selected="selected"</#if>>QUERY</option> 
				                                            <option value="PATH" <#if item.serParamPosition =="PATH">selected="selected"</#if>>PATH</option> 
				                                            <option value="HEADER" <#if item.serParamPosition =="HEADER">selected="selected"</#if>>HEADER</option>
			                                        	</select>
			                                        </td>
			                                        <td style="text-align:center;"><a style="white-space:nowrap"><span style="cursor:pointer;" class="" onclick="delParam(this)">移除</span></a></td>
			                                    </tr>
						                    </#if>
					                    </#list>
				                    </#if>
                                    </tbody>
                                </table>
                            </div>
                            <div class="pagination-gird-container request-param-setter-errormessage ng-binding ng-hide">
                                <i class="icon-no-2 console-mr1 console-inline-block"></i>
                            </div>
                            <div class="pagination-gird-container console-clearfix">
                                    <span class="console-button-wrap ng-isolate-scope" theme="green" size="tiny"
                                          icon="add">
                                        <a class="console-button console-button-green console-button-tiny addPassParam">
                                            <span>
                                                <span class="ng-scope ng-binding">增加一条</span>
                                            </span>
                                        </a>
                                    </span>
                            </div>
                        </div>

                        <div class="console-panel console-mt4">
                            <div class="console-panel-header">
                                <span class="console-panel-header-line"></span>
                                <div class="console-float-left ng-binding clearfloat">自定义常量参数

                                </div>
                            </div>

                            <div class="api-param-map-setter ms1">
                                <table>
                                    <tbody>
                                    <tr>
                                        <th width="20%" class="ng-binding">后端参数名称</th>
                                        <th width="20%" class="ng-binding">参数值</th>
                                        <th width="20%" class="ng-binding">参数位置</th>
                                        <th class="ng-binding">描述</th>
                                        <th width="10%" class="ng-binding">操作</th>
                                    </tr>
                                    <#if context.api.serverEntrance.body.params?exists>
				                    	<#list context.api.serverEntrance.body.params as item>
				                    		<#if item.type == 9>
					                    		<tr class="ng-scope">
										          <td><input value="${item.serParamName!}" class="console-textbox console-width-12 ng-pristine ng-valid ng-valid-pattern"></td>
										          <td><input value="${item.paramValue!}" class="console-textbox console-width-12 ng-pristine ng-valid"></td>
										          <td>
										            <select class="console-selectbox console-width-12 ng-pristine ng-valid isPassSelectBody">
										         	    <option value="QUERY" <#if item.serParamPosition =="QUERY">selected="selected"</#if>>QUERY</option> 
			                                            <option value="PATH" <#if item.serParamPosition =="PATH">selected="selected"</#if>>PATH</option> 
			                                            <option value="HEADER" <#if item.serParamPosition =="HEADER">selected="selected"</#if>>HEADER</option>
										            </select>
										          </td>
										          <td><input value="${item.describe!}" class="console-textbox console-width-12 ng-pristine ng-valid"></td>
										          <td style="text-align:center;"><a style="white-space:nowrap"><span style="cursor:pointer;" class="" onclick="delParam(this)">移除</span></a></td>
										        </tr>
						                    </#if>
					                    </#list>
				                    </#if>
                                    </tbody>
                                </table>
                            </div>
                            <div class="pagination-gird-container request-param-setter-errormessage ng-binding ng-hide">
                                <i class="icon-no-2 console-mr1 console-inline-block"></i>
                            </div>
                            <div class="pagination-gird-container console-clearfix">
                                    <span class="console-button-wrap ng-isolate-scope" theme="green" size="tiny"
                                          icon="add">
                                        <a class="console-button console-button-green console-button-tiny addConstant">
                                            <span>
                                                <span class="ng-scope ng-binding">增加一条</span>
                                            </span>
                                        </a>
                                    </span>
                            </div>
                        </div>


                        <div class="console-panel console-mt4">
                            <div class="console-panel-header">
                                <span class="console-panel-header-line"></span>
                                <div class="console-float-left ng-binding">系统参数
                                        <span class="console-form-tip-inline console-grey console-ml2">
                                            <a class="console-mr2" href="javascript:;" style="white-space:nowrap">
                                                <i class="icon-help-2 console-mr1 console-inline-block"></i>
                                            </a>
                                        </span>
                                </div>
                            </div>

                            <div class="api-param-map-setter ms2">
                                <table>
                                    <tbody>
                                    <tr>
                                        <th width="20%" class="ng-binding">系统参数名</th>
                                        <th width="20%" class="ng-binding">后端参数名称</th>
                                        <th width="20%" class="ng-binding">参数位置</th>
                                        <th width="20%" class="ng-binding">描述</th>
                                        <th width="10%" class="ng-binding">操作</th>
                                    </tr>
                                    <#if context.api.serverEntrance.body.params?exists>
				                    	<#list context.api.serverEntrance.body.params as item>
				                    		<#if item.type == 1>
					                    		<tr class="ng-scope">
										          <td>
										            <select class="console-selectbox console-width-12 ng-valid ng-dirty sysPN" onchange="sysParamChange(this)">
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_HOST" <#if item.sysParamType =="CLIENT_HOST">selected="selected"</#if>>CLIENT_HOST</option>
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_PORT" <#if item.sysParamType =="CLIENT_PORT">selected="selected"</#if>>CLIENT_PORT</option>
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_PATH" <#if item.sysParamType =="CLIENT_PATH">selected="selected"</#if>>CLIENT_PATH</option>
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_SESSION_ID" <#if item.sysParamType =="CLIENT_SESSION_ID">selected="selected"</#if>>CLIENT_SESSION_ID</option>
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_ABSOLUTE_URI" <#if item.sysParamType =="CLIENT_ABSOLUTE_URI">selected="selected"</#if>>CLIENT_ABSOLUTE_URI</option>
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_REQUEST_SCHEMA" <#if item.sysParamType =="CLIENT_REQUEST_SCHEMA">selected="selected"</#if>>CLIENT_REQUEST_SCHEMA</option>
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="SERVER_API_NAME" <#if item.sysParamType =="SERVER_API_NAME">selected="selected"</#if>>SERVER_API_NAME</option>
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="SERVER_UNIX_TIME" <#if item.sysParamType =="SERVER_UNIX_TIME">selected="selected"</#if>>SERVER_UNIX_TIME</option>
										              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="SERVER_USER_AGENT" <#if item.sysParamType =="SERVER_USER_AGENT">selected="selected"</#if>>SERVER_USER_AGENT</option>
										            </select>
										          </td>
										          <td><input value="${item.serParamName!}" class="console-textbox console-width-12" type="text"></td>         
										          <td>
										          	<select class="console-selectbox console-width-12 ng-valid isPassSelectBody">
										            	<option value="QUERY" <#if item.serParamPosition =="QUERY">selected="selected"</#if>>QUERY</option> 
			                                            <option value="PATH" <#if item.serParamPosition =="PATH">selected="selected"</#if>>PATH</option> 
			                                            <option value="HEADER" <#if item.serParamPosition =="HEADER">selected="selected"</#if>>HEADER</option>
										            </select>
										          </td>
										          <td class=" sysDescribe" style="text-align:center;">${item.describe!}</td>
										          <td><a href="javascript:;" class=""><span onclick="delParam(this)">移除</span></a></td>
										        </tr>
						                    </#if>
					                    </#list>
				                    </#if>
                                    </tbody>
                                </table>
                            </div>

                            <div class="pagination-gird-container console-clearfix">
                                    <span class="console-button-wrap ng-isolate-scope" theme="green" size="tiny"
                                          icon="add">
                                        <a target=""
                                           class="console-button console-button-green console-button-tiny addSysParam">
                                            <i class="icon-add"></i>
                                            <span>
                                                <span class="ng-scope ng-binding"
                                                      style="display:inline-block;">增加一条</span>
                                            </span>
                                        </a>
                                    </span>
                            </div>
                        </div>
                        <!-- createApiPage End -->
                    </div>
                    <!-- createAPI3 End -->


                    <div class="console-panel console-mt4 createAPI4 hide">
                        <div class="console-panel-header">
                            <span class="console-panel-header-line"></span>
                            <div class="console-float-left ng-binding">返回结果基础定义</div>
                        </div>
                        <div class="console-panel-body console-p4 console-pt6 console-pb6">
                        	<table style="width: 100%">
                                <tbody>
                                	<tr>
	                                	<td class="createApiResultTableTd1">返回ContentType</td>
	                                	<td class="createApiResultTableTd2" colspan="2">
		                                	<select id="content-type"
                                                class="console-selectbox console-width-4 ng-pristine ng-valid">
	                                            <option value="application/json;charset=UTF-8" class="ng-binding">JSON
	                                                (application/json;charset=UTF-8)
	                                            </option>
	                                            <option value="text/plain;charset=UTF-8" class="ng-binding">文本
	                                                (text/plain;charset=UTF-8)
	                                            </option>
	                                            <option value="application/octet-stream;charset=UTF-8" class="ng-binding">
	                                             	   二进制
	                                                (application/octet-stream;charset=UTF-8)
	                                            </option>
	                                            <option value="application/xml;charset=UTF-8" class="ng-binding">XML
	                                                (application/xml;charset=UTF-8)
	                                            </option>
	                                            <option value="text/html;charset=UTF-8" class="ng-binding">HTML
	                                                (text/html;charset=UTF-8)
	                                            </option>
	                                            <option value="custom"  selected="selected"  class="ng-binding">自定义</option>
	                                        </select>	
                                        </td>
                                	</tr>
                                	
                                	<tr id="custom-content-type-hide-show" style="display: none;">
	                                	<td class="createApiResultTableTd1">自定义Content-Type</td>
	                                	<td class="createApiResultTableTd2" colspan="2">
		                                	   <input value="${context.api.contentType!'application/json;charset=UTF-8'}" class="console-textbox ng-pristine ng-valid ng-valid-pattern"
                                               id="custom-content-type" type="text" placeholder="请输入自定义返回类型值">
                                        </td>
                                	</tr>
                                	
                                	<tr>
	                                	<td class="createApiResultTableTd1">透传header值</td>
	                                	<td class="createApiResultTableTd2 result-tran-header-body" colspan="2">
		                                	 <div>
	                                            <input type="text"
	                                                   class="console-textbox console-width-4 ng-pristine ng-valid input-result-header-name"
	                                                   placeholder="请输入header名字">
	                                            <a style="white-space:nowrap"><span style='cursor:pointer;' class=""
	                                                                                onclick='addResultHeader()'>增加一条</span></a>
	                                        </div>
	                                        <#if context.api.result.tranHeaders?exists>
			                                    <#list context.api.result.tranHeaders as item>
			                                    	<div style="margin-top: 3px;"> 
														<input type="text" value="${item}" class="console-textbox console-width-4 ng-pristine ng-valid input-result-header-name"  placeholder="请输入header名字">
								                   		<a style="white-space:nowrap" onclick='delResultHeader(this)'><span style='cursor:pointer;'>移除</span></a>
								                 	</div>
			                                    </#list>
		                                    </#if>
                                        </td>
                                	</tr>
                                	<tr>
	                                	<td class="createApiResultTableTd1">访问限制返回</td>
	                                	<td class="createApiResultTableTd2">
		                                	<textarea placeholder="请输入当访问受限制时要返回的结果"
		                                                  class="console-textarea" rows="3"
		                                                  id="limitExample"><#t>${context.api.result.limitExample!}<#t></textarea>
                                        </td>
	                                	<td>状态码 <input style="width:160px;" value="${context.api.result.limitStatus?c}" class="console-textbox"
                                               id="limitStatus" type="text" placeholder="请输入状态码,默认202"></td>
                                	</tr>
                                	<tr>
	                                	<td class="createApiResultTableTd1">发生异常/失败返回</td>
	                                	<td class="createApiResultTableTd2">
		                                	<textarea placeholder="请输入当发生了异常要返回的结果"
		                                                  class="console-textarea" rows="3"
		                                                  id="failureExample"><#t>${context.api.result.failureExample!}<#t></textarea>
                                        </td>
	                                	<td>状态码 <input style="width:160px;" value="${context.api.result.failureStatus?c}" class="console-textbox"
                                               id="failureStatus" type="text" placeholder="请输入状态码,默认500"></td>
                                	</tr>
                                	<tr>
	                                	<td class="createApiResultTableTd1">网关入口参数检查失败返回</td>
	                                	<td class="createApiResultTableTd2">
		                                	<textarea placeholder="请输入网关入口参数检查失败要返回的结果"
		                                                  class="console-textarea" rows="3"
		                                                  id="apiEnterCheckFailureExample"><#t>${context.api.result.apiEnterCheckFailureExample!}<#t></textarea>
                                        </td>
	                                	<td>状态码 <input style="width:160px;" value="${context.api.result.apiEnterCheckFailureStatus?c}" class="console-textbox"
                                               id="apiEnterCheckFailureStatus" type="text" placeholder="请输入状态码,默认400"></td>
                                	</tr>
                                	<tr>
	                                	<td class="createApiResultTableTd1">无法连接上后台服务器返回</td>
	                                	<td class="createApiResultTableTd2">
		                                	<textarea placeholder="请输入网关无法连接上后台服务器时的返回结果"
		                                                  class="console-textarea" rows="3"
		                                                  id="cantConnServerExample"><#t>${context.api.result.cantConnServerExample!}<#t></textarea>
                                        </td>
	                                	<td>状态码 <input style="width:160px;" value="${context.api.result.cantConnServerStatus?c}" class="console-textbox"
                                               id="cantConnServerStatus" type="text" placeholder="请输入状态码,默认504"></td>
                                	</tr>
                                	<tr>
	                                	<td class="createApiResultTableTd1">返回结果示例</td>
	                                	<td class="createApiResultTableTd2">
		                                	<textarea placeholder="该值只作为展示如果操作成功返回值的例子,没有实际作用"
		                                                  class="console-textarea" rows="3"
		                                                  id="successExample"><#t>${context.api.result.successExample!}<#t></textarea>
                                        </td>
                                	</tr>
                                </tbody>
                            </table>
                        </div>

                        <div class="console-panel console-mt4">
                            <div class="console-panel-header">
                                <span class="console-panel-header-line"></span>
                                <div class="console-float-left ng-binding">可能会发生的错误码提示定义</div>
                            </div>

                            <div class="api-param-map-setter ms3">
                                <table>
                                    <tbody>
                                    <tr>
                                        <th width="25%" class="ng-binding">错误码</th>
                                        <th width="25%" class="ng-binding">错误信息</th>
                                        <th width="35%" class="ng-binding">描述</th>
                                        <th width="10%" class="ng-binding">操作</th>
                                    </tr>
                                    <#if context.api.result.status?exists>
	                                    <#list context.api.result.status as item>
	                                    <tr  class="ng-scope">
						                    <td><input value="${item.code!}" placeholder="必填: 请输入状态码" class="console-textbox console-width-12 ng-pristine ng-valid"></td>
						                    <td><input value="${item.msg!}" placeholder="必填: 请输入错误信息" class="console-textbox console-width-12 ng-pristine ng-valid" ></td>
						                    <td><input value="${item.describe!}" placeholder="必填: 请输入错误描述" class="console-textbox console-width-12 ng-pristine ng-valid"></td>
						                    <td><a class="" ><span onclick='delParam(this)' style='cursor:pointer;'>移除</span></a></td>
					                    </tr>
					                    </#list>
				                    </#if>
                                    </tbody>
                                </table>
                            </div>
                            <div class="pagination-gird-container request-param-setter-errormessage ng-binding ng-hide">
                                <i class="icon-no-2 console-mr1 console-inline-block"></i>
                            </div>
                            <div class="pagination-gird-container console-clearfix">
                                    <span class="console-button-wrap ng-isolate-scope" theme="green" size="tiny"
                                          icon="add">
                                        <a class="console-button console-button-green console-button-tiny errParam">
                                            <i class="icon-add"></i>
                                            <span>
                                                <span class="ng-scope ng-binding">增加一条</span>
                                            </span>
                                        </a>
                                    </span>
                            </div>
                        </div>
                    </div>
                    <!-- createAPI4 End -->


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
                                          onclick="updateAPI()">
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

</div>
<script src="/static/js/main.js"></script>
<script src="/static/js/APISelectChange.js"></script>
</body>

</html>