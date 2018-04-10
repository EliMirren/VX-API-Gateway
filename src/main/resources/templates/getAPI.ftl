<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="webkit|ie=edge">
    <title>VX-API-API</title>
    <link rel="stylesheet" href="/static/framework/bootstrap.min.css">
    <script src="/static/framework/jquery.min.js"></script>
    <script src="/static/framework/bootstrap.min.js"></script>
    <link rel="stylesheet" href="/static/css/main.css">
</head>

<body style="min-width: 1180px;">
<nav class="navbar navbar-inverse">
	<div class="container-fluid">
		<div class="navbar-header">
			<a href="/static/Application.html" class="navbar-brand"
				style="font-family: 微软雅黑; color: white; font-size: 24px;">VX-API</a>
		</div>
		<span class="login-out"><a href="/loginOut" style="color: white">退出</a></span>
	</div>
</nav>
<input type="hidden" id="appName" value="${context.api.appName}">
<div class="show-tips-model" style="display: none;">
    <div class="tips-body">
        <div class="tips-info">
           	 API启动中...
        </div>
        <h1 class="tips-progress"></h1>
    </div>
</div>
<div class="ng-scope container">
    <ol class="breadcrumb">
        <li>
            <a href="/static/Application.html">应用列表</a>
        </li>
        <li>
            <a href="/static/API.html?/${context.api.appName}" style="cursor: pointer;">API管理</a>
        </li>
        <li>API详情</li>
    </ol>
    <div class="ng-scope">
        <div class="console-panel">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <table width="100%">
                    <tr>
                        <td width="70%">名称及描述</td>
                        <#if context.api.online == true>
                        	<td width="10%"><button type="button" class="btn btn-warning right" onclick="stopAPI('${context.api.appName}','${context.api.apiName}')" style="margin-top: 2px;">暂停</button></td>
                        <#else>
                        	<td width="10%"><button type="button" class="btn btn-success right" onclick="startAPI('${context.api.apiName}')" style="margin-top: 2px;">启动</button></td>
                        </#if>
                        <td width="10%"><a href="/static/updtAPI/${context.api.apiName}"><button type="button" class="btn btn-primary right"  style="margin-top: 2px;">修改</button></a></td>
                        <td width="10%"><button type="button" class="btn btn-danger right" style="margin-top: 2px;" onclick="del('${context.api.appName}','${context.api.apiName}')">删除</button></td>
                    </tr>
                </table>
            </div>
            <table class="console-panel-body">
                <tbody>
                <tr>
                    <td width="50%" class="">
                        <span class="console-grey console-mr3 ">API名称 : </span>${context.api.apiName}
                    </td>
                    <td class="">
                        <span class="console-grey console-mr3 ">创建时间 : </span>${context.api.apiCreateTime!}
                    </td>
                </tr>
                <tr>
                    <td class="">
                        <span class="console-grey console-mr3 ">安全认证 : </span><#if context.api.authOptions?exists>${context.api.authOptions.inFactoryName}<#else>不认证</#if>
                    </td>
                    <td class="">
                        <span class="console-grey console-mr3 ">访问限制 : </span><#if context.api.limitUnit?exists>单位 : ${context.api.limitUnit} , API限制 : ${context.api.apiLimit} , IP限制 : ${context.api.ipLimit}<#else>不限制</#if>
                    </td>
                </tr>
                <#if context.api.beforeHandlerOptions?exists || context.api.afterHandlerOptions?exists >
	                <tr>
	                    <td class="">
	                    	<#if context.api.beforeHandlerOptions?exists>
		                        <span class="console-grey console-mr3 ">前置处理器 : </span>${context.api.beforeHandlerOptions.inFactoryName}
	                        </#if>
	                    </td>
	                    <td class="">
	                    	<#if context.api.afterHandlerOptions?exists>
	                        	<span class="console-grey console-mr3 ">后置处理器 : </span>${context.api.afterHandlerOptions.inFactoryName}
	                    	</#if>
	                    </td>
	                </tr>
                </#if>
                <tr>
                    <td colspan="2">
                        <div>
                            <span class="console-grey console-mr3 ">描述 : </span>
                            <br>
                            <br>
                            <pre style="white-space: pre-wrap;word-wrap: break-word;" class="">${context.api.apiDescribe!}</pre>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="console-panel console-mt4">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <div class="console-float-left ">请求API定义</div>
            </div>
            <table class="console-panel-body">
                <tbody>
                <tr>
                    <td width="50%" class="">
                        <span class="console-grey console-mr3">Path : </span>${context.api.path}
                    </td>
                    <td class="">
                        <span class="console-grey console-mr3 ">HTTP Method : </span>${context.api.method}
                    </td>
                </tr>
                <#if context.api.consumes?exists>
	                <tr>
	                    <td colspan="2">
	                        <span class="console-grey console-mr3 ">请求类型/consumes : </span>
	                        	<#list context.api.consumes as item>
									${item} <#sep> , </#sep>
								</#list>
	                    </td>
	                </tr>
                </#if>
                <tr>
                    <td colspan="2">
                        <span class="console-grey console-mr3 ">请求入参信息 : </span>
                    </td>
                </tr>
                </tbody>
            </table>
            <div class="request-param-setter ng-isolate-scope ng-pristine ng-valid">
                <table>
                    <tbody>
                    <tr>
                         <th width="13%" class="">参数名</th>
                        <th width="13%" class="">参数位置</th>
                        <th width="13%" class="">类型</th>
                        <th width="13%" class="">必填</th>
                        <th class="14%">默认值</th>
                        <th class="14%">描述</th>
                        <th class="20%">条件信息</th>
                    </tr>
                    <#if context.api.enterParam?exists>
                   	 	<#list context.api.enterParam as item>
	                    	<tr>
	                        	<td>${item.paramName!}</td>
	                        	<td>${item.position!}</td>
	                        	<td>${item.paramType!}</td>
	                        	<td>${item.isNotNull?c}</td>
	                        	<td>${item.def!}</td>
	                        	<td>${item.describe!}</td>
	                        	<td>
	                        		<#if item.checkOptions?exists>
	                        			<#if item.checkOptions.maxLength?exists>最大长度:${item.checkOptions.maxLength?c} </#if> 
	                        			<#if item.checkOptions.maxValue?exists> 最大值:${item.checkOptions.maxValue?c}</#if>
	                        			<#if item.checkOptions.minValue?exists> , 最小值:${item.checkOptions.minValue?c}</#if>
	                        			<#if item.checkOptions.regex?exists><br>正则:${item.checkOptions.regex}</#if>
	                        			<#if item.checkOptions.enums?exists><br>枚举:
											<#list item.checkOptions.enums as item>${item} <#sep> , </#sep></#list>
										</#if>
	                        		</#if>
	                        	</td>
	                  	  </tr>
	                    </#list>	
                    </#if>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="console-panel console-mt4 ng-scope">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <div class="console-float-left ">后端服务信息</div>
            </div>
            <table class="console-panel-body">
                <tbody>
                <!--HTTP/HTTPS类型-->
                <#if context.api.serverEntrance.serverType == "HTTP_HTTPS">
	                <tr>
	                    <td width="50%" class="">
	                        <span class="console-grey console-mr3 ">后端服务类型 : </span>HTTP/HTTPS
	                    </td>
	                    <td class=""><span class="console-grey console-mr3 ">后端超时 : </span>${context.api.serverEntrance.body.timeOut?c} ms</td>
	                </tr>
	                <tr>
	                    <td class=""><span class="console-grey console-mr3">HTTP Method : </span>${context.api.serverEntrance.body.method}</td>
	                    <td class=""><span class="console-grey console-mr3 ">负载均衡模式 : </span>${context.api.serverEntrance.body.balanceType}</td>
	                </tr>
	                <tr>
	                    <td colspan="2"><span class="console-grey console-mr3 ">后端服务地址 : </span></td>
	                </tr>
					<#list context.api.serverEntrance.body.serverUrls as item >
		                <tr>
		                    <td colspan="2">URL : ${item.url} , 权重 : ${item.weight}</td>
		                </tr>
	                </#list>
                </#if>
                <!--常量/变量服务类型-->
                 <#if context.api.serverEntrance.serverType == "CUSTOM">
	                <tr>
	                    <td width="50%" class="">
	                        <span class="console-grey console-mr3 ">后端服务类型 : </span> 自定义服务
	                    </td>
	                    <td class=""><span class="console-grey console-mr3 ">服务类型 : </span> ${context.api.customFactoryName!}</td>
	                </tr>
	                <tr>
	                    <td colspan="2">
	                        <div>
	                            <span class="console-grey console-mr3 ">配置文件 : </span>
	                            <br>
	                            <br>
	                            <pre style="white-space: pre-wrap;word-wrap: break-word;" class="">
	                            	<#t>${context.api.customBody!}<#t>
	                            </pre>
	                        </div>
	                    </td>
	                </tr>
                </#if>
                <!--页面跳转-->
                <#if context.api.serverEntrance.serverType == "REDIRECT">
	                <tr>
	                    <td width="50%" class="">
	                        <span class="console-grey console-mr3 ">后端服务类型 : </span> 页面跳转
	                    </td>
	                    <td>
	                        <span class="console-grey console-mr3">跳转地址 : </span>${context.api.serverEntrance.body.url!}
	                    </td>
	                </tr>
                </#if>
                </tbody>
            </table>
        </div>
        <div class="console-panel console-mt4 ng-scope">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <div class="console-float-left ">后端服务参数</div>
            </div>
            <div class="request-param-setter ng-isolate-scope ng-pristine ng-valid">
                <table>
                    <tbody>
                    <tr>
                        <th class="" width="20%">请求参数名称</th>
                        <th class="" width="20%">请求参数位置</th>
                        <th class="" width="20%">服务参数名称</th>
                        <th class="" width="20%">服务参数位置</th>
                        <th class="" width="20%">数据类型</th>
                    </tr>
                    <#if context.api.serverEntrance.body.params?exists>
                    	<#list context.api.serverEntrance.body.params as item>
                    		<#if item.type == 0>
			                    <tr>
			                        <td>${item.apiParamName!}</td>
			                        <td>${item.apiParamPosition!}</td>
			                        <td>${item.serParamName!}</td>
			                        <td>${item.serParamPosition!}</td>
			                        <td>${item.paramType!}</td>
			                    </tr>
		                    </#if>
	                    </#list>
                    </#if>
                    </tbody>
                </table>
                <div class="pagination-gird-container request-param-setter-errormessage  ng-hide">
                    <i class="icon-no-2 console-mr1 console-inline-block"></i>
                </div>
            </div>
        </div>

        <div class="console-panel console-mt4 ng-scope">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <div class="console-float-left ">透传参数</div>
            </div>
            <div class="request-param-setter ng-isolate-scope ng-pristine ng-valid">
                <table>
                    <tbody>
                    <tr>
                        <th width="25%" >请求参数名字</th>
                        <th width="25%">请求参数位置</th>
                        <th width="25%">服务参数名字</th>
                        <th class="25%">服务参数位置</th>
                    </tr>
                     <#if context.api.serverEntrance.body.params?exists>
                    	<#list context.api.serverEntrance.body.params as item>
                    		<#if item.type == 2>
			                    <tr>
			                        <td>${item.apiParamName!}</td>
			                        <td>${item.apiParamPosition!}</td>
			                        <td>${item.serParamName!}</td>
			                        <td>${item.serParamPosition!}</td>
			                    </tr>
		                    </#if>
	                    </#list>
                    </#if>
                    </tbody>
                </table>
                <div class="pagination-gird-container request-param-setter-errormessage  ng-hide">
                    <i class="icon-no-2 console-mr1 console-inline-block"></i>
                </div>
            </div>
        </div>
        <div class="console-panel console-mt4 ng-scope">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <div class="console-float-left ">自定义常量参数</div>
            </div>
            <div class="request-param-setter ng-isolate-scope ng-pristine ng-valid">
                <table>
                    <tbody>
                    <tr>
                        <th width="25%">服务参数名称</th>
                        <th width="25%">参数值</th>
                        <th width="25%">服务参数位置</th>
                        <th class="25%">描述</th>
                    </tr>
                     <#if context.api.serverEntrance.body.params?exists>
                    	<#list context.api.serverEntrance.body.params as item>
                    		<#if item.type == 9>
			                    <tr>
			                        <td>${item.serParamName!}</td>
			                        <td>${item.paramValue!}</td>
			                        <td>${item.serParamPosition!}</td>
			                        <td>${item.describe!}</td>
			                    </tr>
		                    </#if>
	                    </#list>
                    </#if>
                    </tbody>
                </table>
                <div class="pagination-gird-container request-param-setter-errormessage  ng-hide">
                    <i class="icon-no-2 console-mr1 console-inline-block"></i>
                </div>
            </div>
        </div>
        <div class="console-panel console-mt4 ng-scope">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <div class="console-float-left ">系统参数</div>
            </div>
            <div class="request-param-setter ng-isolate-scope ng-pristine ng-valid">
                <table>
                    <tbody>
                    <tr>
                        <th width="25%">系统参数名</th>
                        <th width="25%">服务参数名称</th>
                        <th width="25%">参数位置</th>
                        <th class="25%">描述</th>
                    </tr>
                    <#if context.api.serverEntrance.body.params?exists>
                    	<#list context.api.serverEntrance.body.params as item>
                    		<#if item.type == 1>
			                    <tr>
			                        <td>${item.sysParamType!}</td>
			                        <td>${item.serParamName!}</td>
			                        <td>${item.serParamPosition!}</td>
			                        <td>${item.describe!}</td>
			                    </tr>
		                    </#if>
	                    </#list>
                    </#if>
                    </tbody>
                </table>
                <div class="pagination-gird-container request-param-setter-errormessage  ng-hide">
                    <i class="icon-no-2 console-mr1 console-inline-block"></i>
                </div>
            </div>
        </div>
        <div class="console-panel console-mt4 ng-scope">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <div class="console-float-left ">返回结果</div>
            </div>
            <table class="console-panel-body">
                <tbody>
                <tr>
                    <td class="">
                        <span class="console-grey console-mr3 ">透传header值 : </span>
                        <#if context.api.result.tranHeaders?exists>
                        	<#list context.api.result.tranHeaders as item>
                        	 ${item}  <#sep> , </#sep>
                        	</#list>
                        </#if>
                    </td>
                </tr>
                <tr>
                    <td class="">
                        <span class="console-grey console-mr3 ">返回Content-Type值 : </span>${context.api.contentType!}
                    </td>
                </tr>
                <tr>
                    <td>
                        <span class="console-grey console-mr3 ">访问限制返回 : </span>
                        <br>
                        <div>
                            <pre class="">状态码: ${context.api.result.limitStatus?c} </br>内容:  ${context.api.result.limitExample!}</pre>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <span class="console-grey console-mr3 ">发生异常/失败返回 : </span>
                        <br>
                        <div>
                            <pre class="">状态码: ${context.api.result.failureStatus?c} </br>内容: ${context.api.result.failureExample!}</pre>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <span class="console-grey console-mr3 ">网关入口参数检查失败返回 : </span>
                        <br>
                        <div>
                            <pre class="">状态码: ${context.api.result.apiEnterCheckFailureStatus?c} </br>内容: ${context.api.result.apiEnterCheckFailureExample!}</pre>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <span class="console-grey console-mr3 ">无法连接上后台服务器返回 : </span>
                        <br>
                        <div>
                            <pre class="">状态码: ${context.api.result.cantConnServerStatus?c} </br>内容: ${context.api.result.cantConnServerExample!}</pre>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <span class="console-grey console-mr3 ">返回结果示例 : </span>
                        <br>
                        <div>
                            <pre class="">${context.api.result.successExample!}</pre>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <#if context.api.result.status?exists>
        <div class="console-panel console-mt4 ng-scope">
            <div class="console-panel-header">
                <span class="console-panel-header-line"></span>
                <div class="console-float-left ">自定义参数</div>
            </div>
            <div class="request-param-setter ng-isolate-scope ng-pristine ng-valid" style="margin-bottom: 70px">
                <table>
                    <tbody>
                    <tr>
                        <th width="30%">错误码</th>
                        <th width="30%">错误信息</th>
                        <th width="40%">描述</th>
                    </tr>
                    <#list context.api.result.status as item>
	                    <tr>
	                        <td>${item.code!}</td>
	                        <td>${item.msg!}</td>
	                        <td>${item.describe!}</td>
	                    </tr>
                    </#list>
                    </tbody>
                </table>
                <div class="pagination-gird-container request-param-setter-errormessage  ng-hide">
                    <i class="icon-no-2 console-mr1 console-inline-block"></i>
                </div>
            </div>
        </div>
        </#if>
    	
    <div class="console-panel console-mt4" style="margin-bottom: 60px;">
        <div class="console-panel-header">
            <span class="console-panel-header-line"></span>
            <div class="console-float-left ">API请求信息追踪</div>
            <button class="btn btn-primary" style="float: right;margin-top: 2px" onclick="reloadTrack('${context.api.appName}','${context.api.apiName}')">获取数据</button>
        </div>
        <table class="console-panel-body">
            <tbody>
            <tr>
                <td width="20%" class="">
                    <span class="console-grey console-mr3">请求次数 : </span><span
                        class="track-request-count"></span>次
                </td>
                <td width="20%" class="">
                    <span class="console-grey console-mr3 ">成功 : </span><span
                        class="track-succeeded-count"></span>次
                </td>
                <td width="20%">
                    <span class="console-grey console-mr3 ">失败 : </span><span class="track-failure-count"></span>次
                </td>
                <td width="20%">
                    <span class="console-grey console-mr3 ">总处理平均用时 : </span><span class="avg-overall"></span>(ms)
                </td>
                <td  width="20%">
                    <span class="console-grey console-mr3 ">请求后台平均用时 : </span><span class="avg-request"></span>(ms)
                </td>
            </tr>
            </tbody>
        </table>
        <div class="api-param-map-setter">
            <table class="console-panel-body">
                <tr>
                    <th width="20%" style="text-align: center">记录时间</th>
                    <th width="20%" style="text-align: center">总用时</th>
                    <th width="20%" style="text-align: center">请求用时</th>
                    <th width="20%" style="text-align: center">请求长度(byte)</th>
                    <th width="20%" style="text-align: center">响应长度(byte)</th>
                </tr>
                <tbody class="track-info-body">
                </tbody>
            </table>
        </div>
    </div>

    	
    
    </div>
</div>


<script src="/static/js/main.js"></script>
<script >
//删除API
function del(appName,id) {
    if (window.confirm('删除API可能会影响你的业务,确定删除' + id + "吗?")) {
        $.ajax({
            type : "post",
            url : '/static/delAPI/'+appName+'/'+ id,
            async : true,
            dataType : "json",
            success : function(result) {
                if (result.status == 200) {
                    var res = result.data;
                    if (res == 0) {
                        alert("删除失败了");
                        console.log(result)
                    } else {
                        alert("删除成功");
                        window.location.href="/static/API.html?/"+$("#appName").val();
                    }
                } else {
                    alert("删除失败");
                    console.log("删除失败status:" + result.status + " ,msg:"
                            + result.msg + " ,data:");
                    console.log(result.data);
                }
            },
            error : function() {
                alert("删除失败");
            }
        });
    }
}
//启动API
function startAPI(id) {
	$(".show-tips-model").fadeIn(300);
	 $.ajax({
         type : "post",
         url : '/static/startAPI/' + id,
         async : true,
         dataType : "json",
         success : function(result) {
        	 $(".show-tips-model").hide();
             if (result.status == 200) {
                 var res = result.data;
                 if (res == 0) {
                     alert("启动API好像失败了...");
                     console.log(result)
                 }else if(res == -1){
                	 alert("启动API失败,请先启动应用程序后才能启动API");
                 } else {
                     alert("启动API成功");
                     location.reload();
                 }
             } else {
                 alert("启动API失败");
                 console.log("启动API失败status:" + result.status + " ,msg:"
                         + result.msg + " ,data:");
                 console.log(result.data);
             }
         },
         error : function() {
        	 $(".show-tips-model").hide();
             alert("启动API失败");
         }
     });
}

//暂停API
function stopAPI(appName,id) {
	if (window.confirm(('暂停可能影响你的业务,确定暂停吗?'))) {
	 $.ajax({
         type : "post",
         url : '/static/stopAPI/'+appName+'/'+ id,
         async : true,
         dataType : "json",
         success : function(result) {
             if (result.status == 200) {
            	 location.reload();
             } else {
                 alert("暂停API失败");
                 console.log("暂停API失败status:" + result.status + " ,msg:"
                         + result.msg + " ,data:");
                 console.log(result.data);
             }
         },
         error : function() {
             alert("暂停API失败");
         }
     });
	}
}

//刷新API的监控信息
function reloadTrack(appName,apiName) {
    $.ajax({
        type : "post",
        url : '/static/trackInfo/' +appName+ "/" + apiName,
        async : true,
        dataType : "json",
        success : function(result) {
            if (result.status == 200) {
                var item = result.data;
                $(".track-request-count").text(item.rc);
                $(".track-succeeded-count").text(item.rc - item.ec);
                $(".track-failure-count").text(item.ec);
                var track = item.track;
                $(".track-info-body").html('');
                var avgOverallSum=0;
                var avgReuqestSum=0;
                for (var i = 0; i < track.length; i++) {
                	avgOverallSum+=track[i].overallTime;
                	avgReuqestSum+=track[i].requestTime;
                    var txt = "<tr style='text-align: center'><td>" +
                            track[i].time + "</td><td>" +
                            track[i].overallTime + "</td><td>" +
                            track[i].requestTime + "</td><td>" +
                            track[i].requestBodyLen + "</td><td>" +
                            track[i].responseBodyLen + "</td></tr>";
                    $(".track-info-body").append($(txt));
                }
                if(avgOverallSum!=0){
                	console.log(parseInt(avgOverallSum/track.length));
	                $(".avg-overall").text(parseInt(avgOverallSum/track.length));
    	            $(".avg-request").text(parseInt(avgReuqestSum/track.length));
                }
            } else {
                alert("获取失败");
                console.log("获取失败status:" + result.status + " ,msg:"
                        + result.msg + " ,data:");
                console.log(result.data);
            }
        },
        error : function() {
            alert("获取失败");
        }
    });
}
</script>
</body>

</html>