function init() {
    $('.cp-nPage').on('click', apiNextPage.bind(this));
    $('.cp-lPage').on('click', apiLastPage.bind(this));
}
// 下一页
function apiNextPage(ev) {
    var cur = parseInt($('#apiListModal').attr('cur'));
    if(cur == 1){
        if (!basicInfoCheck()){
            return;
        }
    }
    if(cur == 2){
        if (!serverInfoCheck()){
            return;
        }
    }
    if(cur == 3){
    	if (!resultInfoCheck()){
    		return;
    	}
    }

    if (cur <= 5) {
        $('.createAPI' + cur).addClass('hide');
        // 导航
        $('.cApiNav' + cur).removeClass('guidebar-current');
        $('#apiListModal').attr('cur', ++cur);
        var newCur = $('#apiListModal').attr('cur');
        $('.createAPI' + newCur).removeClass('hide');
        $('.cApiNav' + newCur).addClass('guidebar-current');
    }
    if (cur == 4) {
        $('.cp-nPage').addClass('hide');
        $('.cp-sPage').removeClass('hide');
    } else {
        $('.cp-nPage').removeClass('hide');
        $('.cp-sPage').addClass('hide');
    }
    if (cur > 1) {
        $('.cp-lPage').removeClass('hide');
    } else {
        $('.cp-lPage').addClass('hide');
    }

    if (cur === 3) {
        $('.bParam-config tbody tr').not(':first').remove();
    }
}
// 上一页
function apiLastPage(ev) {
    var cur = parseInt($('#apiListModal').attr('cur'));
    if (cur >= 2) {
        $('.createAPI' + cur).addClass('hide');
        $('.cApiNav' + cur).removeClass('guidebar-current');
        $('#apiListModal').attr('cur', --cur);
        var newCur = $('#apiListModal').attr('cur');
        $('.createAPI' + newCur).removeClass('hide');
        $('.cApiNav' + newCur).addClass('guidebar-current');
    }
    if (cur === 4) {
        $('.cp-sPage').removeClass('hide');
    } else {
        $('.cp-sPage').addClass('hide');
        $('.cp-nPage').removeClass('hide');
    }
    if (cur > 1) {
        $('.cp-lPage').removeClass('hide');
    } else {
        $('.cp-lPage').addClass('hide');
    }
}
//app的配置信息
var data = {};
//基本信息检查
function basicInfoCheck() {
    data.appName = $("#app-name").val();
    if (data.appName == '') {
        alert("请输入应用名称");
        return false;
    }
    data.describe = $("#app-describe").val();
    if (data.describe == '') {
        alert("请输入应用描述");
        return false;
    }
    data.scope = parseInt($('input:radio[name="scope"]:checked').val());
    return true;
}
//信息定义检查
function resultInfoCheck() {
	  if ($("#notFoundContentType").val() != 'custom') {
	    	data.notFoundContentType = $("#notFoundContentType").val();
	  }else{
		  data.notFoundContentType = $("#notFoundContentTypeCustom").val();
	  }
	  if ($("#notFoundResult").val() != '') {
		 data.notFoundResult = $("#notFoundResult").val();
	  }
	  
	  if ($("#blacklistIpCode").val() != '') {
		 var code= parseInt($("#blacklistIpCode").val());
		  if(isNaN(code)){
			  alert('状态码必须为数字');
			  return false;
		  }
		  data.blacklistIpCode = code;
	  }
	  
	  if ($("#blacklistIpContentType").val() != 'custom') {
	    	data.blacklistIpContentType = $("#blacklistIpContentType").val();
	  }else{
		  data.blacklistIpContentType = $("#blacklistIpContentTypeCustom").val();
	  }
	  if ($("#blacklistIpResult").val() != '') {
		  data.blacklistIpResult =$("#blacklistIpResult").val();
	  }
	return true;
}

//服务器信息配置检查
function serverInfoCheck() {
    var serverOptions = {};
    serverOptions.createHttp = $("#is-create-http").is(":checked");
    serverOptions.createHttps = $("#is-create-https").is(":checked");
    serverOptions.httpPort = parseInt($("#http-port").val());
    serverOptions.httpsPort = parseInt($("#https-port").val());
    data.serverOptions = serverOptions;
    if (!(serverOptions.createHttp || serverOptions.createHttps)) {
        alert("必须最少选择一种服务HTTP或者HTTPS");
        return false;
    }
    if (serverOptions.createHttps) {
        var cert_type = $('input:radio[name="cert-type"]:checked')
            .val();
        if (cert_type == null || cert_type == '') {
            alert("请选择证书类型!");
            return false;
        }
        var cert_key = $("#cert-key").val();
        if (cert_key == '') {
            alert("请输入证书的key值或者路径");
            return false;
        }
        var cert_path = $("#cert-path").val();
        if (cert_path == '') {
            alert("请输入证书所在路径");
            return false;
        }
        var certOptions ={};
        certOptions.certType = cert_type;
        certOptions.certKey = cert_key;
        certOptions.certPath = cert_path;
        serverOptions.certOptions = certOptions;
    }
    if ($("#is-create-cors").is(":checked")) {
        var corsOptions = {};
        corsOptions.allowedOrigin = $("#allowedOrigin").val();
        if (corsOptions.allowedOrigin == '') {
            alert("跨域请求请输入允许请求源,如果运行所有可以输入*号");
            return false;
        }
        corsOptions.allowCredentials = $(
            'input:radio[name="allowCredentials"]:checked').val();
        if(corsOptions.allowCredentials == "true" && "*" == corsOptions.allowedOrigin){
        	alert("跨域请求源,如果为*,allowCredentials不允许为true,需要非*才可以为true");
        	return false;
        }
        corsOptions.maxAgeSeconds = parseInt($("#maxAgeSeconds").val());
        var ah = $("#allowedHeaders").val();
        if (ah != '') {
            ah = ah.replace("，", ",");
            var item=ah.split(",");
            var items=[];
            for(var i=0;i<item.length;i++){
                if(item[i] != ''){
                    items.push(item[i]);
                }
            }
            if (items.length>0){
                corsOptions.allowedHeaders =items;
            }
        }
        var eh = $("#exposedHeaders").val();
        if (eh != '') {
            eh = eh.replace("，", ",");
            var item=eh.split(",");
            var items=[];
            for(var i=0;i<item.length;i++){
                if(item[i] != ''){
                    items.push(item[i]);
                }
            }
            if (items.length>0){
                corsOptions.exposedHeaders =items;
            }
        }
        var method = new Array();
        var item = $('input[name="allowedMethods"]');
        for (var i = 0; i < item.length; i++) {
            if ($(item[i]).is(":checked")) {
                method.push($(item[i]).val())
            }
        }
        corsOptions.allowedMethods = method;
        data.corsOptions = corsOptions;
    }
    return true;
}
//应用信息检查
function appConfigCheck() {
    if($("#custom").val() != ''){
        if(!isJSON($("#custom").val())){
            alert("拓展配置必须为JSON格式");
            return false;
        }else{
            data.serverOptions.custom=$("#custom").val();
        }
    }
    if($("#webClientCustom").val() != ''){
    	if(!isJSON($("#webClientCustom").val())){
    		alert("WebClient拓展配置必须为JSON格式");
    		return false;
    	}else{
    		data.webClientCustom=$("#webClientCustom").val();
    	}
    }
    if ($("#contentLength").val() != '') {
        data.contentLength = parseInt($("#contentLength").val());
    }
    if ($("#sessionTimeOut").val() != '') {
        data.sessionTimeOut = parseInt($("#sessionTimeOut").val());
    }
    if ($("#sessionCookieName").val() != '') {
        data.sessionCookieName = $("#sessionCookieName").val();
    }
    data.keepAlive = $('input:radio[name="keepAlive"]:checked').val();
    if ($("#decoderInitialBufferSize").val() != '') {
    	data.decoderInitialBufferSize = parseInt($("#decoderInitialBufferSize").val());
    }
    if ($("#maxPoolSize").val() != '') {
        data.maxPoolSize = parseInt($("#maxPoolSize").val());
    }
    if ($("#maxInitialLineLength").val() != '') {
        data.maxInitialLineLength = parseInt($("#maxInitialLineLength").val());
    }
    if ($("#maxHeaderSize").val() != '') {
        data.maxHeaderSize = parseInt($("#maxHeaderSize").val());
    }
    return true;
}

//是否开启HTTPS服务
$("#is-create-https").click(function () {
    isCreateHTTPS();
});
function isCreateHTTPS() {
    if ($("#is-create-https").is(":checked")) {
        $(".https-hide-show").show();
    } else {
        $(".https-hide-show").hide();
    }
}

//是否开启HTTP服务
$("#is-create-http").click(function () {
    isCreateHTTP();
});
function isCreateHTTP() {
    if ($("#is-create-http").is(":checked")) {
        $(".http-hide-show").show();
    } else {
        $(".http-hide-show").hide();
    }
}
//是否开启跨域
$("#is-create-cors").click(function () {
    isCreateCORS();
});
function isCreateCORS() {
    if ($("#is-create-cors").is(":checked")) {
        $(".cors-hide-show").show();
    } else {
        $(".cors-hide-show").hide();
    }
}
$(function () {
    isCreateHTTPS();
    isCreateHTTP();
    isCreateCORS();
});

//notFoundContentType 找不到路径(404) Content-Type:下拉框改变事件
$("#notFoundContentType").change(function() {
	if($(this).val()=="custom"){
		$("#notFoundContentTypeCustom").show();
	}else{
		$("#notFoundContentTypeCustom").hide();
	}
});
//blacklistIpContentType 在黑名单列表中返回Content-Type:下拉框改变事件
$("#blacklistIpContentType").change(function() {
	if($(this).val()=="custom"){
		$("#blacklistIpContentTypeCustom").show();
	}else{
		$("#blacklistIpContentTypeCustom").hide();
	}
});



//初始化事件
init();

//确定创建
function createAPP() {
	if(!appConfigCheck()){
		return;
	}
    $.ajax({
        type : "post",
        url : '/static/addAPP',
        async : true,
        data : JSON.stringify(data),
        dataType : "json",
        success : function(result) {
            if (result.status == 200) {
                var res = result.data;
                if (res == 0) {
                    alert("创建失败了");
                    consol.log(result);
                } else {
                    alert("创建成功");
                    window.location.href = "/static/Application.html";
                }
            } else {
                if(result.status == 1444){
                    alert("创建应用失败:已经存在同名的应用");
                }else{
                    alert("创建应用失败");
                    console.log("创建应用失败status:" + result.status + " ,msg:"
                        + result.msg + " ,data:");
                    console.log(result.data);
                }
            }
        },
        error : function() {
            alert("创建应用失败");
        }
    });
}

function updateAPP() {
	if(!appConfigCheck()){
		return;
	}
    data.time=$("#time").val();
    $.ajax({
        type : "post",
        url : '/static/updtAPP',
        async : true,
        data : JSON.stringify(data),
        dataType : "json",
        success : function(result) {
            if (result.status == 200) {
                var res = result.data;
                if (res == 0) {
                    alert("修改失败了");
                    consol.log(result);
                } else {
                    alert("修改成功,如果需要该应用生效,请重启该应用");
                    window.location.href = "/static/getAPP/"+data.appName;
                }
            } else {
                alert("修改应用失败");
                console.log("修改应用失败status:" + result.status + " ,msg:"
                    + result.msg + " ,data:");
                console.log(result.data);
            }
        },
        error : function() {
            alert("修改应用失败");
        }
    });
}

// 检查参数是否为json格式
function isJSON(str) {
    if (typeof str == 'string') {
        try {
            var obj = JSON.parse(str);
            if (typeof obj == 'object' && obj) {
                return true;
            } else {
                return false;
            }
        } catch (e) {
            console.log('error：' + str + '!!!' + e);
            return false;
        }
    }
    console.log('It is not a string!')
}