/**
 * Created by Mirren on 2018/2/13.
 */
// 权限认证改变事件
$("#auth-options-name").change(function () {
    if ($(this).val() == 'none') {
        $(".auth-hide-show").hide();
    } else if ($(this).val() == 'sessionTokenAuth') {
        $("#auth-options-config").val('{\n"apiTokenName":"vxApiSessionToken",\n"userTokenName":"vxApiUserToken",\n"userTokenScope":"HEADER",\n"authFailContentType":"HTML_UTF8",\n"authFailResult":"Unauthorized"\n}');
        $(".auth-hide-show").show();
    } else {
    	$("#auth-options-config").val('');
    	$("#auth-options-config").val('{\n"apiTokenName":"apiToken",\n"userTokenName":"apiName",\n"secretKeys":[{"admin":"secret"},{"test":"123456"}]}');
        $(".auth-hide-show").show();
    }
});
//访问限制改变事件
$("#limitUnit").change(function () {
 if ($(this).val() == 'none') {
     $(".limit-hide-show").hide();
 } else {
     $(".limit-hide-show").show();
 }
});
$(function () {
 if ($("#auth-options-name").val() == 'none') {
     $(".auth-hide-show").hide();
 } else {
     $(".auth-hide-show").show();
 }
 if ($("#limitUnit").val() == 'none') {
     $(".limit-hide-show").hide();
 } else {
     $(".limit-hide-show").show();
 }
});
//前置处理器改变事件
$("#beforeHandler").change(function () {
 if ($(this).val() == 'none') {
     $(".beforeHandler-hide-show").hide();
 } else {
     $(".beforeHandler-hide-show").show();
 }
});
//后置处理器改变事件
$("#afterHandler").change(function () {
 if ($(this).val() == 'none') {
     $(".afterHandler-hide-show").hide();
 } else {
     $(".afterHandler-hide-show").show();
 }
});

//前置与后置处理器改变事件
$(function () {
 if ($("#beforeHandler").val() == 'none') {
     $(".beforeHandler-hide-show").hide();
 } else {
     $(".beforeHandler-hide-show").show();
 }
 if ($("#afterHandler").val() == 'none') {
     $(".afterHandler-hide-show").hide();
 } else {
     $(".afterHandler-hide-show").show();
 }
});


//后端服务类型改变事件
$("#custom_server_type").change(function () {
    if($(this).val() == 'GET_SERVER_UNIX_TIMESTAMP'){//获得服务器时间戳
        $(".custom_server_type_tips").text(" $(val) 为返回结果值的占位符,VX-API会将其替换为时间戳");
        $("#custom-option").val('{"resultFormat":"$(val)"}');
    }else if ($(this).val() == 'GET_CONSTANT_VALUE'){//获得常量值
        $(".custom_server_type_tips").text(" $(val) 为返回结果值的占位符,VX-API会将其替换为值");
        $("#custom-option").val('{"value":"null","resultFormat":"$(val)"}');
    }else if ($(this).val() == 'SESSION_TOKEN_GRANT_AUTH'){//获得基于Session-token的授权
        $(".custom_server_type_tips").text('');
        $("#custom-option").val('{"saveTokenName":"vxApiSessionToken","getTokenName":"vxApiUserToken","balanceType":"POLLING_AVAILABLE","method":"GET","timeOut":6000,"retryTime":30000,"serverUrls":[{"url":"http://127.0.0.1/test","weight":0}]}');
    }
});



//返回结果Conten-Type改变事件
$("#content-type").change(function () {
 if ($(this).val() == 'custom') {
     $("#custom-content-type-hide-show").show();
 } else {
     $("#custom-content-type-hide-show").hide();
 }
});
$(function () {
 if ($("#content-type").val() == 'custom') {
     $("#custom-content-type-hide-show").show();
 } else {
     $("#custom-content-type-hide-show").hide();
 }
});
