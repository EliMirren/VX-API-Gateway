<!DOCTYPE html>
<html lang="zh-CN">

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta http-equiv="X-UA-Compatible" content="ie=edge">
<title>VX-API-Application</title>
<link rel="stylesheet"
	href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css"
	integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u"
	crossorigin="anonymous">
<script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
<script
	src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"
	integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
	crossorigin="anonymous"></script>
<link rel="stylesheet" href="/static/css/main.css">
<style>
span {
	color: #000000;
}
</style>
</head>
<nav class="navbar navbar-inverse">
	<div class="container-fluid">
		<div class="navbar-header">
			<a href="/static/Application.html" class="navbar-brand"
				style="font-family: 微软雅黑; color: white; font-size: 24px;">VX-API</a>
		</div>
		<span class="login-out"><a href="/loginOut" style="color: white">退出</a></span>
	</div>
</nav>
<div class="show-tips-model" style="display: none;">
    <div class="tips-body">
        <div class="tips-info">
           	 应用启动中...
        </div>
        <h1 class="tips-progress"></h1>
    </div>
</div>
<div class="container">
	<ol class="breadcrumb">
		<li><a href="/static/Application.html">应用列表</a></li>
		<li>应用详情</li>
	</ol>
	<div class="panel panel-default">
		<div class="panel-heading">
			<h3 class="panel-title">
				<div class="row">
					<div class="col-md-9" style="line-height: 30px;">
						${context.app.appName} 【<span class="scope"> <#if context.app.scope==2>正式版<#elseif context.app.scope==1>预览版<#else>测试版</#if></span>】
					</div>
					<div class="col-md-1">
						<#if context.app.online == true>
							<button type="button" class="btn btn-warning right" onclick="unDeploy('${context.app.appName}')" style="margin-top: 2px;">暂停</button>
						<#else>
							<button type="button" class="btn btn-success right" onclick="deploy('${context.app.appName}')" style="margin-top: 2px;">启动</button>
						</#if>
					</div>
					<div class="col-md-1">
						<a href="/static/updtAPP/${context.app.appName}">
							<button type="button" class="btn btn-primary right"  style="margin-top: 2px;">修改</button>
						</a>
					</div>
					<div class="col-md-1">
						<button type="button" class="btn btn-danger right" style="margin-top: 2px;"
							onclick="del('${context.app.appName}')">删除</button>
					</div>
				</div>
			</h3>
		</div>
		<div class="panel-body">${context.app.describe}</div>
	</div>
	<div class="list-group">
		<a class="list-group-item" style="background-color: #eee"> 应用服务集</a> 
		<#if context.app.serverOptions.createHttp== true>
			<a class="list-group-item">HTTP服务 : <span>已开启</span> | 端口号 : <span>${context.app.serverOptions.httpPort?c}</span> </a> 
		</#if>
		 <#if context.app.serverOptions.createHttps== true>
			<a class="list-group-item">HTTPS服务 : <span>已开启</span> | 端口号 : <span>${context.app.serverOptions.httpsPort?c}</span> | 证书类型 :<span>${context.app.serverOptions.certOptions.certType}</span></a>
		</#if>
	</div>
	<div class="list-group">
		<a class="list-group-item" style="background-color: #eee"> 跨域配置 </a>
		<#if context.app.corsOptions?exists> 
			<a class="list-group-item">allowedOrigin : <span>${context.app.corsOptions.allowedOrigin! }</span></a> 
			<a class="list-group-item">allowCredentials : <span>${context.app.corsOptions.allowCredentials?c}</span></a>
			<a class="list-group-item">maxAgeSeconds : <span>${context.app.corsOptions.maxAgeSeconds?c}</span></a>
			<a class="list-group-item">allowedHeaders : 
				<span> 
					<#if context.app.corsOptions.allowedHeaders?exists> 
						<#list context.app.corsOptions.allowedHeaders as header>${header}<#sep>, </#sep></#list> 
					</#if>
				</span>
			</a> 
			<a class="list-group-item">exposedHeaders : 
				<span> 
					<#if context.app.corsOptions.exposedHeaders?exists> 
						<#list context.app.corsOptions.exposedHeaders as header>${header}<#sep>, </#sep></#list> 
					</#if>
				</span>
			</a> 
			<a class="list-group-item">allowedMethods : 
				<span> 
					<#if context.app.corsOptions.allowedMethods?exists> 
						<#list context.app.corsOptions.allowedMethods as method>${method}<#sep>, </#sep></#list> 
					</#if>
				</span>
			</a> 
		<#else> 
			<a class="list-group-item"><span>尚未开启跨域处理</span></a>
		</#if>
	</div>
	<div class="list-group">
		<a class="list-group-item" style="background-color: #eee"> 服务器参数配置
		</a> <a class="list-group-item">请求主体的最大长度 : <span>${context.app.contentLength?c}</span></a>
		<a class="list-group-item">会话超时时间 : <span>${context.app.sessionTimeOut?c} ms</span></a> 
		<a class="list-group-item">会话的cookie名称 : <span>${context.app.sessionCookieName}</span></a>
		<a class="list-group-item">HTTP对象解码器缓冲区大小 : <span>${context.app.decoderInitialBufferSize?c}</span></a>
		<a class="list-group-item">API与后台服务器是否使用keepAlive : <span>${context.app.keepAlive?c}</span>
		</a> <a class="list-group-item">API与后台服务器交互线程数 : <span>${context.app.maxPoolSize?c}</span></a>
		<a class="list-group-item">参数值最大长度 : <span>${context.app.maxInitialLineLength?c}</span></a>
		<a class="list-group-item">Header参数值最大长度 : <span>${context.app.maxHeaderSize?c}</span></a>
	</div>
	<div class="list-group">
		<a class="list-group-item" style="background-color: #eee"> 应用服务拓展配置 </a> 
		<a class="list-group-item">拓展配置 : <span>${context.app.serverOptions.custom!}</span></a>
	</div>
</div>
<script>
function deploy(id) {
	$(".tips-info").text("应用启动中...");
	$(".show-tips-model").fadeIn(300);
	$.ajax({
		type : "post",
		url : '/static/deployAPP/' + id,
		dataType : "json",
		success : function(result) {
			$(".show-tips-model").hide();
			if (result.status == 200) {
				var res = result.data;
				if (res == 0) {
					alert("启动应用失败了");
					console.log(result)
				} else {
					alert("启动应用成功");
					if (window.confirm(('是否启动该应用所有API'))) {
						startAPI(id);
					}else {
						location.reload();
					}
				}
			}else if(result.status == 1111){
				alert("启动应用失败,该端口号已被别的程序占用");
				console.log(result)
			} else {
				alert("启动应用失败");
				console.log("启动应用失败status:" + result.status + " ,msg:"
						+ result.msg + " ,data:");
				console.log(result.data);
			}
		},
		error : function(msg) {
			$(".show-tips-model").hide();
			alert("启动应用失败");
			console.log(msg);
		}
	});
}
//启动所有API
function startAPI(id){
	$(".tips-info").text("所有API启动中...");
	$(".show-tips-model").fadeIn(300);
	$.ajax({
		type : "post",
		url : '/static/startAllAPI/' + id,
		dataType : "json",
		success : function(result) {
			$(".show-tips-model").hide();
			if (result.status == 200) {
				var res = result.data;
				alert(res);
				location.reload();
			}else {
				console.log("启动应用失败status:" + result.status + " ,msg:"
						+ result.msg + " ,data:");
				console.log(result.data);
				alert("启动所有API失败: "+result.data);
				location.reload();
			}
		},
		error : function(msg) {
			$(".show-tips-model").hide();
			alert("启动所有API失败");
			console.log(msg);
		}
	});
}


function unDeploy(id) {
	if (window.confirm(('暂停可能影响你的业务,确定暂停吗?'))) {
		$.ajax({
			type : "post",
			url : '/static/unDeployAPP/' + id,
			dataType : "json",
			success : function(result) {
				if (result.status == 200) {
					var res = result.data;
					if (res == 0) {
						alert("暂停应用失败了");
						console.log(result)
					} else {
						location.reload();
					}
				} else {
					alert("暂停应用失败");
					console.log("暂停应用失败status:" + result.status + " ,msg:"
							+ result.msg + " ,data:");
					console.log(result.data);
				}
			},
			error : function() {
				alert("暂停应用失败");
			}
		});
	}
}

function del(id) {
	if (${context.app.online?c}){
		alert('请先将程序暂停后再执行删除操作');
		return;
	}
	if (window.confirm('确定删除' + id + "吗?")) {
		$.ajax({
			type : "post",
			url : '/static/delAPP/' + id,
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
						window.location.href = "/static/Application.html";
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
</script>
</body>

</html>