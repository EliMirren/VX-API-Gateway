function init() {

    // 入参定义
    $('.addParams').on('click', toSenate.bind(this));
    $('.addPassParam').on('click', passParam.bind(this));
    $('.addConstant').on('click', constantParam.bind(this));
    $('.addSysParam').on('click', systemParam.bind(this));
    $('.errParam').on('click', errCodeParam.bind(this));
    $('.cp-nPage').on('click', apiNextPage.bind(this));
    $('.cp-lPage').on('click', apiLastPage.bind(this));
}
function modalClose(ev) {
    $(ev).closest('#senateEdit').hide();
}

// 新增入参定义
function toSenate() {
    var isMap = $('.SenateYs option:selected').val(),
        option = isMap === 'PASSTHROUGH' ? `<option value="QUERY" selected="selected">QUERY</option>` : `<option value="QUERY" selected="selected">QUERY</option>
      <option value="PATH">PATH</option><option value="HEADER">HEADER</option>`;
    var html =
        `<tr class="ng-scope">
  <td><input class="console-textbox console-width-12 ng-pristine ng-valid-pattern ng-invalid ng-invalid-required paramName" type="text"></td>
  <td>
    <select class="console-selectbox console-width-12 ng-pristine ng-valid paramChange" onchange='paramPsHandle(this)'>
     ${option}
    </select>
  </td>
  <td>
    <select class="console-selectbox console-width-12 ng-pristine ng-valid psType paramChange2">
      <option value="String" selected="selected">String</option>
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
    <input type="checkbox" class="ng-pristine ng-valid  paramPosition" checked='checked'>
  </td>
  <td>
    <input class="console-textbox console-width-12 ng-pristine ng-valid paramDefault " type="text">
  </td>
  <td>
    <input class="console-textbox console-width-12 ng-pristine ng-valid paramDescribe" type="text">
  </td>
  <td>
    <span style="white-space:nowrap" >      
      <a id='editMore' href="javascript:;" class="ng-scope  " onclick='senateEditMore(this)'>编辑更多</a>| 
      <a href="javascript:;" class="ng-scope  " onclick='delParam(this)' >移除</a>
     
    </span>

  </td>
</tr>`;
    $('.newParam-setter tbody').append(html);
    $('.SenateYs').on('change', SenateHd.bind(this));
}
function SenateHd(ev) {
    let $SenateYs = $('.SenateYs option:selected').val();
    if ($SenateYs === 'PASSTHROUGH') {
        $('.paramChange').html(' <option value="QUERY" selected="selected">QUERY</option>');
        $('.paramPosition').attr('disabled', 'disabled');
        $('.paramPosition').prop({ checked: true });
    } else {
        $('.paramChange').html(` <option value="QUERY" selected="selected">QUERY</option>
      <option value="PATH">PATH</option>
      <option value="HEADER">HEADER</option>`);
    }
}
function paramPsHandle(ev) {
    var val = ev.value,
        parent = ev.parentNode.parentNode,
        $paramPosition = $(parent).find('.paramPosition');
    if (val === 'PATH') {
        $paramPosition.attr('disabled', 'disabled');
        $paramPosition.prop({ checked: true });
    } else {
        $paramPosition.removeAttr('disabled');
        $paramPosition.removeAttr('checked');
    }
}


// HTTP_HTTPS服务类型点击事件
function http_httpsOnClick() {
    $(".redirect_body").hide();
    $(".vars_body").hide();
    $(".http_https_body").show();
}
// 自定义服务类型点击事件
function varsOnClick() {
    $(".http_https_body").hide();
    $(".redirect_body").hide();
    $(".vars_body").show();
}
// 页面跳转服务类型点击事件
function redirectOnClick() {
    $(".http_https_body").hide();
    $(".vars_body").hide();
    $(".redirect_body").show();
}
// 页面加载完毕后显示相应的服务类型
$(function () {
    var type = $('input:radio[name="backendType"]:checked').val();
    if (type == 'CUSTOM') {
        varsOnClick();
    } else if (type == 'REDIRECT') {
        redirectOnClick();
    } else {
    	$("#backendType_http_s").attr("checked","checked");
        http_httpsOnClick();
    }
});

var serverURLCount = 1;
// 添加后台服务的URL
function addServerURL() {
    serverURLCount++;
    $(".balance-hide-show").fadeIn(300);
    var html = `<tr>
                    <td style="border-right: 1px solid #e1e6eb;border-bottom: none;"></td>
                    <td style="padding-top: 3px; border: none"  class="input-server-box">
                          <input type="text" class="console-textbox console-width-4 ng-pristine ng-valid input-server-url" placeholder="请输入URL 示例: http://127.0.0.1/test">  权重: <input type="text" class="console-textbox input-server-weight"  placeholder="权重值" style="width: 60px">
                          <a style="white-space:nowrap"><span style='cursor:pointer;' class="" onclick='delServerURL(this)'> 移除 </span></a> <a style="white-space:nowrap">| <span style='cursor:pointer;' class="" onclick='testServerURL(this)'> 测试连接</span></a>
                     </td>
                </tr>`;
    $(".http_https_body").append(html);
}
// 删除后台服务的URL
function delServerURL(ev) {
    serverURLCount--;
    if (serverURLCount == 1) {
        $(".balance-hide-show").hide();
    }
    delParam(ev);
}
// 测试连接是否可用
function testServerURL(ev) {
    var path = $(ev).parent().parent().find(".input-server-url").val()+"".trim();
    if (path == '') {
        alert("地址不能为空")
        return;
    }
    window.open(path);
}

// 添加透传参数
function passParam() {
	var bodySelect = isPassBody == true ? "" : "<option value='BODY'>BODY</option>";
    var html = `<tr  class="ng-scope">
          <td><input class="console-textbox console-width-12 ng-pristine ng-valid ng-valid-pattern" ></td>
           <td>
            <select class="console-selectbox console-width-12 ng-pristine ng-valid isPassSelectBody" >
            <option value="QUERY" selected="selected">QUERY</option> 
    		<option value="PATH">PATH</option> 
            <option value="HEADER">HEADER</option></select>
          </td>
          <td ><input class="console-textbox console-width-12 ng-pristine ng-valid" ></td>
          <td><select class="console-selectbox console-width-12 ng-pristine ng-valid isPassSelectBody" >
          <option value="QUERY" selected="selected">QUERY</option> 
          ${bodySelect}
    	  <option value="PATH">PATH</option> 
          <option value="HEADER">HEADER</option></select></td>
          <td style='text-align:center;'><a style="white-space:nowrap" ><span style='cursor:pointer;'  class="" onclick='delParam(this)'>移除</span></a></td>
        </tr>`;
    $('.passParamBody tbody').append(html);
}
// 添加常量参数
function constantParam() {
	var bodySelect = isPassBody == true ? "" : "<option value='BODY'>BODY</option>";
    var html = `<tr  class="ng-scope">
          <td><input class="console-textbox console-width-12 ng-pristine ng-valid ng-valid-pattern" ></td>
          <td ><input class="console-textbox console-width-12 ng-pristine ng-valid" ></td>
          <td>
            <select class="console-selectbox console-width-12 ng-pristine ng-valid isPassSelectBody" >
            <option value="QUERY" selected="selected">QUERY</option> 
            ${bodySelect}
    		<option value="PATH">PATH</option> 
            <option value="HEADER">HEADER</option></select>
          </td>
          <td><input class="console-textbox console-width-12 ng-pristine ng-valid"></td>
          <td style='text-align:center;'><a style="white-space:nowrap" ><span style='cursor:pointer;'  class="" onclick='delParam(this)'>移除</span></a></td>
        </tr>`;
    $('.ms1 tbody').append(html);
}
// 添加一行系统参数
function systemParam() {
	var bodySelect = isPassBody == true ? "" : "<option value='BODY'>BODY</option>";
    var html = `<tr  class="ng-scope">
          <td>
            <select class="console-selectbox console-width-12 ng-valid ng-dirty sysPN" onchange='sysParamChange(this)'>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_HOST">CLIENT_HOST</option>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_PORT">CLIENT_PORT</option>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_PATH">CLIENT_PATH</option>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_SESSION_ID">CLIENT_SESSION_ID</option>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_ABSOLUTE_URI">CLIENT_ABSOLUTE_URI</option>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="CLIENT_REQUEST_SCHEMA">CLIENT_REQUEST_SCHEMA</option>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="SERVER_API_NAME">SERVER_API_NAME</option>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="SERVER_UNIX_TIME">SERVER_UNIX_TIME</option>
              <option ng-repeat="option in systemParameterList" ng-disabled="selectedSystemParameters[option.ParamName] &amp;&amp; option.ParamName != param.ParameterName" class="ng-scope " value="SERVER_USER_AGENT">SERVER_USER_AGENT</option>
            </select>
          </td>
          <td ><input type='text' class="console-textbox console-width-12" ></td>         
          <td>
            <select class="console-selectbox console-width-12 ng-valid isPassSelectBody" >
            <option value="QUERY" selected="selected">QUERY</option> 
            ${bodySelect}
    		<option value="PATH">PATH</option> 
            <option value="HEADER">HEADER</option></select>
          </td>
          <td class=" sysDescribe" style='text-align:center;' >请求客服端的ip地址</td>
          <td><a href="javascript:;" class=""><span onclick='delParam(this)'>移除</span></a></td>
        </tr>`;
    $('.ms2 tbody').append(html);
}
// 添加透传的header
function addResultHeader() {
	var html = `  <div style="margin-top: 3px;"> 
					<input type="text" class="console-textbox console-width-4 ng-pristine ng-valid input-result-header-name"  placeholder="请输入header名字">
                    <a style="white-space:nowrap" onclick='delResultHeader(this)'><span style='cursor:pointer;'>移除</span></a>
                  </div>`;
    $(".result-tran-header-body").append(html);
}
function delResultHeader(ev) {
    $(ev).parent().remove();
}

// 错误状态码
function errCodeParam() {
    var html = `<tr  class="ng-scope">
                    <td><input placeholder="必填: 请输入状态码" class="console-textbox console-width-12 ng-pristine ng-valid"></td>
                    <td><input placeholder="必填: 请输入错误信息" class="console-textbox console-width-12 ng-pristine ng-valid" ></td>
                    <td><input placeholder="必填: 请输入错误描述" class="console-textbox console-width-12 ng-pristine ng-valid"></td>
                    <td><a class="" ><span onclick='delParam(this)' style='cursor:pointer;'>移除</span></a></td>
                  </tr>`;
    $('.ms3 tbody').append(html);
}

function sysParamChange(ev) {
    let $sysParam = $(ev).find('option:selected').text(),
        $describe = $(ev).closest('tr').find('.sysDescribe');
    switch ($sysParam) {
        case 'CLIENT_HOST':
            $describe.text('请求客服端的ip地址');
            break;
        case 'CLIENT_PORT':
            $describe.text('请求客户端的端口');
            break;
        case 'CLIENT_PATH':
            $describe.text('请求客户端的PATH');
            break;
        case 'CLIENT_SESSION_ID':
            $describe.text('请求客户端的sessionId');
            break;
        case 'CLIENT_ABSOLUTE_URI':
            $describe.text('用户请求的完整路径');
            break;
        case 'CLIENT_REQUEST_SCHEMA':
            $describe.text('用户请求的模式');
            break;
        case 'SERVER_API_NAME':
            $describe.text('获得api的名字');
            break;
        case 'SERVER_UNIX_TIME':
            $describe.text('获得API服务器的unix时间戳');
            break;
        case 'SERVER_USER_AGENT':
            $describe.text('获得网关USER_AGENT');
            break;
        default:
            break;
    }
}
// 删除tr td tag tag
function delParam(ev) {
	if (confirm("确定移除该条定义吗?")) {
    	$(ev).parent().parent().parent().remove();
    }
}
// 是否透传body
function passBodyFun() {
    var isSelect=document.getElementById("passBody").checked;
    isPassBody=isSelect;
    var serverMethodOptions = document.getElementById("serverMethod").children;
    // 如果透传Body则默认请求后端服务为post
    if(isPassBody){
    	serverMethodOptions[1].selected=true;
    }
    isNeedClearSelect=true;
}
// 标记是否需要清除select中的body
var isNeedClearSelect=false;
// 当透传body是清除所有body选项
function clearSelectBodyOption() {
    var bodySelect=isPassBody==true?"":"<option value='BODY' >BODY</option>";
    var selects = $(".isPassSelectBody");
    for (var i = 0; i < selects.length; i++) {
        var $select=$(selects[i]);
        var option=$(selects[i]).val();
        $select.html("");
        var txt=bodySelect+"<option value='QUERY'>QUERY</option> <option value='PATH'>PATH</option> <option value='HEADER'>HEADER</option>";
        $select.html($(txt));
        var options =$select.find('option');
        for(var j = 0; j < options.length; j++) {
          if(options[j].value == option){
              options[j].selected = true;
              break;
          }
        }
    }
}

// API信息定义
var apiInfo = new Object();
// 下一页
function apiNextPage(ev) {
    let cur = parseInt($('#apiListModal').attr('cur'));
    if (cur === 1) {
        if (!basicInfo()) {
            return;
        }
    }
    if (cur === 2) {
        if (!apiEnterInfo()) {
            return;
        }
    }
    if (cur === 3) {
        if (!apiSerEnterInfo()) {
            return;
        }
    }
    if (cur === 4) {
        if (!apiSerEnterInfo()) {
            return;
        }
    }
    if (cur <= 5) {
        $('.createAPI' + cur).addClass('hide');
        // 导航
        $('.cApiNav' + cur).removeClass('guidebar-current');
        $('#apiListModal').attr('cur', ++cur);
        let newCur = $('#apiListModal').attr('cur');
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
        if(isNeedClearSelect){
            clearSelectBodyOption();
        }
        $('.bParam-config tbody tr').not(':first').remove();
        let $nps = $('.newParam-setter table tr');
        var bodySelect = isPassBody == true ? "" : "<option value='BODY'>BODY</option>";
        $nps.each(function (index, e) {
            if (index !== 0) {
                let $parName = $(this).find('.paramName').val(),
                    $paramChange = $(this).find('.paramChange option:selected').text(),
                    $paramChange2 = $(this).find('.paramChange2 option:selected').text(),
                    serPosition = '<option value="QUERY">QUERY</option><option value="PATH" >PATH</option><option value="HEADER" >HEADER</option>';
                html = `<tr class="ng-scope">
                    <td ><input maxlength="99" value="${$parName}"  class="console-textbox console-width-12 ng-pristine ng-valid ng-valid-pattern" type="text"></td>
                    <td><select class="console-selectbox console-width-12 ng-pristine ng-valid" isPassSelectBody" >${serPosition}${bodySelect}</select></td>
                    <td class="" style="text-align: center;">${$parName}</td>
                    <td class="" style="text-align: center;">${$paramChange}</td>
                    <td class="" style="text-align: center;">${$paramChange2}</td>
                    <td style="text-align: center;cursor: pointer;">
				    	<span style="white-space:nowrap" >      
				      	<a class="ng-scope  " onclick='delParam(this)' >移除</a>
				    	</span>
                	</td>
                    </tr> `;
                $('.bParam-config tbody').append(html);
            }
        });

    }
}
// 上一页
function apiLastPage(ev) {
    let cur = parseInt($('#apiListModal').attr('cur'));
    if (cur >= 2) {
        $('.createAPI' + cur).addClass('hide');
        $('.cApiNav' + cur).removeClass('guidebar-current');
        $('#apiListModal').attr('cur', --cur);
        let newCur = $('#apiListModal').attr('cur');
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


// 基本信息检查与装数据
function basicInfo() {
    if ($("#apiName").val() == '') {
        alert('API名称不能为空');
        return false;
    }
    if ($("#apiDescribe").val() == '') {
        alert('API描述不能为空');
        return false;
    }
    apiInfo = {};
    apiInfo.apiName = $("#apiName").val().toString().trim().replace(/\//g,"_");
    apiInfo.apiDescribe = $("#apiDescribe").val().toString().trim();

    if ($("#auth-options-name").val() != 'none') {
        apiInfo.authOptions = new Object();
        apiInfo.authOptions.inFactoryName = $("#auth-options-name").val();
        var str = $("#auth-options-config").val()+"".trim();
        if (isJSON(str)) {
            apiInfo.authOptions.option = JSON.parse(str);
        } else {
            alert('认证配置文件必须为json格式');
            return false;
        }
    }

    if ($("#limitUnit").val() != 'none') {
        apiInfo.limitUnit = $("#limitUnit").val().toString().trim();
        apiInfo.apiLimit = parseInt($("#apiLimit").val().toString().trim());
        apiInfo.ipLimit = parseInt($("#ipLimit").val().toString().trim());
        if (isNaN(apiInfo.apiLimit)) {
            apiInfo.apiLimit = -1;
        }
        if (isNaN(apiInfo.ipLimit)) {
            apiInfo.ipLimit = -1;
        }
        if ((apiInfo.apiLimit <= -1 && apiInfo.ipLimit <= -1)) {
            alert('访问限制没有任何意义,请输入大于-1的值');
            return false;
        }
        if (apiInfo.apiLimit < apiInfo.ipLimit && apiInfo.apiLimit != -1) {
            alert('IP访问限制不能大于API的流量限制');
            return false;
        }
    }

    // 前置处理器
    if ($("#beforeHandler").val() != 'none') {
        apiInfo.beforeHandlerOptions = new Object();
        apiInfo.beforeHandlerOptions.inFactoryName = $("#beforeHandler").val();
        var str = $("#beforeHandlerOptions").val()+"".trim();
        if (isJSON(str)) {
            apiInfo.beforeHandlerOptions.option = JSON.parse(str);
        } else {
            alert('前置处理器配置文件必须为json格式');
            return false;
        }
    }
    // 前置处理器
    if ($("#afterHandler").val() != 'none') {
        apiInfo.afterHandlerOptions = new Object();
        apiInfo.afterHandlerOptions.inFactoryName = $("#afterHandler").val();
        var str = $("#afterHandlerOptions").val()+"".trim();
        if (isJSON(str)) {
            apiInfo.afterHandlerOptions.option = JSON.parse(str);
        } else {
            alert('后置处理器配置文件必须为json格式');
            return false;
        }
    }
    console.log(apiInfo)
    return true;
}
// 检查api入口参数
function apiEnterInfo() {
    if ($("#path").val() == '') {
        alert('Path不能为空');
        return false;
    }
    if ($("#path").val().charAt(0) != '/') {
        alert('Path必须以 / 开头');
        return false;
    }
    apiInfo.path = $("#path").val().toString().trim();
    // 检查path中参数名字
    var paths = apiInfo.path.split("/");
    var pathsVars = [];
    for (var i = 0; i < paths.length; i++) {
        if (paths[i].indexOf(":") == 0) {
            pathsVars.push(paths[i].replace(":", ""));
        }
    }
    delete apiInfo.consumes;
    if ($("#consumes").val() != '') {
        var cs = $("#consumes").val();
        cs = cs.replace("，", ",");
        var item = cs.split(",");
        var cns = [];
        for (var i = 0; i < item.length; i++) {
            if (item[i] != '') {
                cns.push(item[i].trim());
            }
        }
        apiInfo.consumes = cns;
    }
    apiInfo.method = $("#method").val();
    var checkIsTrue = true;
    var enterParam = [];
    let $nps = $('.newParam-setter table tr');
    if ($nps.length > 1) {
        $nps.each(function (index, e) {
            if (index !== 0) {
                let $parName = $(this).find('.paramName').val().trim(),
                    $paramChange = $(this).find('.paramChange option:selected').text(),
                    $paramChange2 = $(this).find('.paramChange2 option:selected').text(),
                    $isNotNull = $(this).find('.paramPosition').is(':checked'),
                    $paramDescribe = $(this).find('.paramDescribe').val().trim(),
                    $paramDefault = $(this).find('.paramDefault').val().trim(),
                    $maxlen = $(this).closest('tr.ng-scope').attr('data-maxlen'),
                    $enums = $(this).closest('tr.ng-scope').attr('data-enum'),
                    $maxv = $(this).closest('tr.ng-scope').attr('data-maxv'),
                    $minv = $(this).closest('tr.ng-scope').attr('data-minv'),
                    $paramvify = $(this).closest('tr.ng-scope').attr('data-paramvify');

                var data = {};
                if ($parName == '') {
                    alert('所以参数的名字都为必填');
                    checkIsTrue = false;
                    return false;
                }
                if ($paramChange == 'PATH') {
                    if (apiInfo.path.indexOf("/:" + $parName) < 0) {
                        alert('如果你需要一个PATH位置的参数,那么path中就必须带有/:' + $parName);
                        checkIsTrue = false;
                        return false;
                    }
                }
                data.paramName = $parName;
                data.position = $paramChange;
                data.paramType = $paramChange2;
                data.isNotNull = $isNotNull;
                if ($paramDescribe != '') {
                    data.describe = $paramDescribe;
                }
                if ($paramDefault != '') {
                    data.def = $paramDefault;
                }
                if ($maxlen != null && $minv != null) {
                    var checkOptions = {};
                    var falg = false;// 标记是否修改过
                    if ($maxlen != '') {
                        checkOptions.maxLength = parseInt($maxlen);
                        falg = true;
                    }
                    if ($minv != '') {
                    	if($minv.indexOf(".") >=0 ){
                    		checkOptions.minValue = parseFloat($minv);
                    	}else{
                    		checkOptions.minValue = parseInt($minv);
                    	}
                        falg = true;
                    }
                    if ($maxv != '') {
                        if($maxv.indexOf(".") >=0 ){
                    		checkOptions.maxValue = parseFloat($maxv);
                    	}else{
                    		checkOptions.maxValue = parseInt($maxv);
                    	}
                        falg = true;
                    }
                    if ($paramvify != '') {
                        checkOptions.regex = $paramvify;
                        falg = true;
                    }
                    if ($enums != '') {
                        var eh = $enums + "";
                        eh = eh.replace("，", ",");
                        var enumitem = eh.split(",");
                        var item = [];
                        for (var i = 0; i < enumitem.length; i++) {
                            if (enumitem[i] != '') {
                                item.push(enumitem[i].trim());
                            }
                        }
                        if (item.length > 0) {
                            checkOptions.enums = item;
                        }
                        falg = true;
                    }
                    if (falg) {
                        data.checkOptions = checkOptions;
                    }
                }
                enterParam.push(data);
            }
        }
        );
    }
    for (var i = 0; i < pathsVars.length; i++) {
        var falg = false;
        for (var j = 0; j < enterParam.length; j++) {
            if (enterParam[j].paramName == pathsVars[i] && enterParam[j].position == "PATH") {
                falg = true;
                break;
            }
        }
        if (falg == false) {
            alert('参数中必须有一个名叫:' + pathsVars[i] + ",并且参数位置为PATH,或者取消path中的路径参数/:" + pathsVars[i]);
            checkIsTrue = false;
            return;
        }
    }
    delete apiInfo.enterParam;
    if (enterParam.length > 0) {
        apiInfo.enterParam = [];
        apiInfo.enterParam = enterParam;
    }
    isPassBody = document.getElementById("passBody").checked;
    apiInfo.passBody = document.getElementById("passBody").checked;
    apiInfo.bodyAsQuery = document.getElementById("bodyAsQuery").checked;
    return checkIsTrue;
}
// 入口参数编辑更多
function senateEditMore(ev) {
    var tr = ev.closest('tr'),
        $senateType = $(ev).closest('tr').find('.psType option:selected').text(),
        $senateMaxv = $('#senateEdit .senateMaxv'),
        $senateMinv = $('#senateEdit .senateMinv'),
        $senateVify = $('#senateEdit .senateVify'),
        $senateMaxlength = $('#senateEdit .senateMaxlength'),
        $senateEnum = $('#senateEdit .senateEnum'),
        $tit = $('#senateEdit .senateTit');
    $tit.html($senateType);
    if ($senateType == 'String') {
        $senateMaxv.addClass('ng-hide');
        $senateMinv.addClass('ng-hide');
        $senateVify.removeClass('ng-hide');
        $senateMaxlength.removeClass('hide');
        $senateEnum.removeClass('ng-hide');
        $('#senateEdit .senateVify textarea').val($(tr).attr('data-paramVify'));
        $('#senateEdit .senateMaxlength input').val($(tr).attr('data-maxlen'));
        $('#senateEdit .senateEnum input').val($(tr).attr('data-enum'));

    } else if ($senateType == 'Boolean' || $senateType == 'JsonObject' || $senateType == 'JsonArray') {
        $senateEnum.addClass('ng-hide');
        $senateMaxv.addClass('ng-hide');
        $senateMinv.addClass('ng-hide');
        $senateVify.addClass('ng-hide');
        $senateMaxlength.addClass('hide');
    } else {
        $senateEnum.removeClass('ng-hide');
        $senateMaxv.removeClass('ng-hide');
        $senateMinv.removeClass('ng-hide');
        $senateVify.addClass('ng-hide');
        $senateMaxlength.addClass('hide');
        $('#senateEdit .senateEnum input').val($(tr).attr('data-enum'));
        $('#senateEdit .senateMaxv input').val($(tr).attr('data-Maxv'));
        $('#senateEdit .senateMinv input').val($(tr).attr('data-Minv'));
    }
    $('#senateEdit').show();


    $('#senateEdit .senateEnter').off('click');
    $('#senateEdit .senateEnter').on('click', function (e) {
        let maxl = $('.senateMaxlength input').val(),
            senum = $('.senateEnum input').val(),
            svify = $('.senateVify textarea').val();
        $(tr).attr({
            'data-maxlen': '',
            'data-enum': '',
            'data-paramVify': '',
            'data-jsonVify': '',
            'data-Minv': '',
            'data-Maxv': ''
        });
        if ($senateType === 'String') {
            $(tr).attr('data-type', $senateType);
            $(tr).attr('data-maxlen', maxl);
            $(tr).attr('data-enum', senum);
            $(tr).attr('data-paramVify', svify);
            $('#senateEdit').hide();
        } else if ($senateType === 'Boolean') {
            $('#senateEdit').hide();
            return;
        } else {
            $(tr).attr('data-type', $senateType);
            $(tr).attr('data-enum', $('.senateEnum input').val());
            $(tr).attr('data-Minv', $('.senateMinv input').val());
            $(tr).attr('data-Maxv', $('.senateMaxv input').val());
            $('#senateEdit').hide();
        }

    });

}
// 服务参数检查
function apiSerEnterInfo() {
    var isCheckTrue = true;// 检查参数是否正确
    var type = $('input:radio[name="backendType"]:checked').val();
    var body = {};
    var isHavePathParam = false;// 用于判断后端服务地址是否有Path参数
    var pathParamName = [];// 用于存储path中的参数名字
    if (type == 'HTTP_HTTPS') {
        var serverUrlBox = $(".input-server-box");
        var serverURL = [];
        for (var i = 0; i < serverUrlBox.length; i++) {
            var url = $(serverUrlBox[i]).find(".input-server-url").val().trim();
            if (url != '') {
                var strRegex = '(https|http)://.+';
                var rex = new RegExp(strRegex);
                if (!rex.test(url)) {
                    alert("你输入的后端服务地址存在不正确的URL请检查");
                    isCheckTrue = false;
                    return false;
                }
                if (url.indexOf("/:") > -1) {
                    isHavePathParam = true;
                    var pathst = url.split("/");
                    var pathstVars = [];
                    for (var j = 0; j < pathst.length; j++) {
                        if (pathst[j].indexOf(":") == 0) {
                            pathstVars.push(pathst[j].replace(":", ""));
                        }
                    }
                    for (var j = 0; j < pathstVars.length; j++) {
                        pathParamName.push(pathstVars[j]);
                    }
                }
                var weight = $(serverUrlBox[i]).find(".input-server-weight").val();
                if(url.indexOf("/",url.indexOf("://")+3) < 0){
                	url+="/";
                }
                su = {};
                su.url = url;
                su.weight = parseInt(weight==''?0:weight);
                serverURL.push(su);
            }
        }
        if (serverURL.length < 1) {
            alert("最少需要有一个可用的后端服务地址");
            isCheckTrue = false;
            return false;
        }
        body.method = $("#serverMethod").val();
        body.balanceType = $('input:radio[name="balanceType"]:checked').val();
        body.serverUrls = serverURL;
        if ($("#serverTimeOut").val().trim() != '') {
            body.timeOut = parseInt($("#serverTimeOut").val().trim());
        }
        if ($("#serverRetryTime").val().trim() != '') {
        	body.retryTime = parseInt($("#serverRetryTime").val().trim());
        }
    } else if (type == 'CUSTOM') {
        var inFactoryName=$("#custom_server_type").val();
        var obj=$("#custom-option").val();
        if (isJSON(obj)){
            body=JSON.parse(obj);
            body.inFactoryName=inFactoryName;
        }else {
            alert("配置文件必须为JSON格式");
            return false;
        }
    } else if (type == 'REDIRECT') {
        var url = $("#redirect_url").val().trim();
        var strRegex = '(https|http)://.+';
        var rex = new RegExp(strRegex);
        if (!rex.test(url)) {
            alert("你输入的跳转URL在不正确的URL请检查");
            isCheckTrue = false;
            return false;
        }
        body.url = url;
    }
    body.params = [];
    let $type0 = $('.bParam-config table tr'),
        $type1 = $('.ms2 table tr'),
        $type2 = $('.passParamBody table tr'),
        $type9 = $('.ms1 table tr');

    // 获得前端映射参数
    if ($type0.length > 1) {
        $type0.each(function (index, e) {
            if (index !== 0) {
                let $parName = $(this).find('td').eq(0).find('input').val().trim(),
                    $paramPosition = $(this).find('td').eq(1).find('option:selected').text(),
                    $nameEqu = $(this).find('td').eq(2).text(),
                    $positionEqu = $(this).find('td').eq(3).text(),
                    $typeEqu = $(this).find('td').eq(4).text();
                var data = {};
                if ($nameEqu != '') {
                    data.apiParamName = $nameEqu;
                }
                if ($positionEqu != '') {
                    data.apiParamPosition = $positionEqu;
                }
                if ($parName != '') {
                    data.serParamName = $parName;
                }
                if ($paramPosition != '') {
                    data.serParamPosition = $paramPosition;
                }
                if ($typeEqu != '') {
                    data.paramType = $typeEqu;
                }
                data.type = 0;
                body.params.push(data);
            }
        });
    }


    // 获得透传参数
    if ($type2.length > 1) {
        $type2.each(function (index, e) {
            if (index !== 0) {
                let $apiParamName = $(this).find('td').eq(0).find('input').val().trim(),
                    $apiParamPosition = $(this).find('td').eq(1).find('option:selected').text(),
                    $serParamName = $(this).find('td').eq(2).find('input').val().trim(),
                    $serParamPosition = $(this).find('td').eq(3).find('option:selected').text();
                var data = {};
                if ($apiParamName == '' || $serParamName == '') {
                    alert("透传参数: 所有名字都为必填选");
                    isCheckTrue = false;
                    return false;
                }
                data.apiParamName = $apiParamName;
                data.apiParamPosition = $apiParamPosition;
                data.serParamName = $serParamName;
                data.serParamPosition = $serParamPosition;
                data.type = 2;
                body.params.push(data);
            }
        });
    }

    // 获得自定义常量参数
    if ($type9.length > 1) {
        $type9.each(function (index, e) {
            if (index !== 0) {
                let $serParamName = $(this).find('td').eq(0).find('input').val().trim(),
                    $paramValue = $(this).find('td').eq(1).find('input').val().trim(),
                    $apiParamPosition = $(this).find('td').eq(2).find('option:selected').text(),
                    $describe = $(this).find('td').eq(3).find('input').val().trim();
                var data = {};
                if ($serParamName == '' || $paramValue == '') {
                    alert("自定义常量参数:名字与值都为必填选项");
                    isCheckTrue = false;
                    return false;
                }
                data.serParamName = $serParamName;
                data.paramValue = $paramValue;
                data.serParamPosition = $apiParamPosition;
                if ($describe != '') {
                    data.describe = $describe;
                }
                data.type = 9;
                body.params.push(data);
            }
        });
    }
    // 获得系统参数
    if ($type1.length > 1) {
        $type1.each(function (index, e) {
            if (index !== 0) {
                let $sysParamName = $(this).find('td').eq(0).find('option:selected').text(),
                    $serParamName = $(this).find('td').eq(1).find('input').val().trim(),
                    $apiParamPosition = $(this).find('td').eq(2).find('option:selected').text(),
                    $describe = $(this).find('td').eq(3).text();
                var data = {};
                if ($serParamName == '') {
                    alert("系统参数: 后端参数名字为必填项");
                    isCheckTrue = false;
                    return false;
                }
                data.sysParamType = $sysParamName;
                data.serParamName = $serParamName;
                data.serParamPosition = $apiParamPosition;
                data.describe = $describe;
                data.type = 1;
                body.params.push(data);
            }
        });
    }
    if (isHavePathParam == true) {
        for (var i = 0; i < pathParamName.length; i++) {
            var isCheckHave=false;
            for (var j = 0; j < body.params.length; j++) {
                if(pathParamName[i] == body.params[j].serParamName && body.params[j].serParamPosition=="PATH"){
                    isCheckHave=true;
                    break;
                }
            }
            if(isCheckHave == false){
                alert("如果你的后端服务地址需要Path参数,那么你的参数里面就必须有一个名字与Path相等,并且参数的位置为PATH的参数");
                isCheckTrue =false;
                return false;
            }
        }
    }
    if (body.params.length < 1) {
        delete body.params;
    }
    apiInfo.serverEntrance = {};
    apiInfo.serverEntrance.serverType = type;
    apiInfo.serverEntrance.body = body;
    return isCheckTrue;
}
// 获得返回值类型
function apiResultInfo() {
    if ($("#content-type").val() == 'custom') {
        if ($("#custom-content-type").val().trim() == '') {
            alert('自定义返回Content-Type值不能为空');
            return false;
        }
        apiInfo.contentType = $("#custom-content-type").val().trim();
    } else {
        apiInfo.contentType = $("#content-type").val();
    }
    var item = $(".input-result-header-name");
    var tranHeaders = [];
    for (var i = 0; i < item.length; i++) {
        if ($(item[i]).val() != '') {
            tranHeaders.push($(item[i]).val());
        }
    }
    var data = {};
    if (tranHeaders.length > 0) {
        data.tranHeaders = tranHeaders;
    }
    data.successExample = $("#successExample").val() == '' ? "" : $("#successExample").val();
    data.apiEnterCheckFailureExample = $("#apiEnterCheckFailureExample").val() == '' ? "" : $("#apiEnterCheckFailureExample").val();
    data.apiEnterCheckFailureStatus = parseInt($("#apiEnterCheckFailureStatus").val());
    data.limitExample = $("#limitExample").val() == '' ? "" : $("#limitExample").val();
    data.limitStatus = parseInt($("#limitStatus").val());
    data.failureExample = $("#failureExample").val() == '' ? "" : $("#failureExample").val();
    data.failureStatus = parseInt($("#failureStatus").val());
    data.cantConnServerExample = $("#cantConnServerExample").val() == '' ? "" : $("#cantConnServerExample").val();
    data.cantConnServerStatus = parseInt($("#cantConnServerStatus").val());
    
    let $type0 = $('.ms3 table tr');
    var checkIsTrue = true;
    if ($type0.length > 1) {
        data.status = [];
        $type0.each(function (index, e) {
            if (index !== 0) {
                let $code = $(this).find('td').eq(0).find('input').val().trim(),
                    $msg = $(this).find('td').eq(1).find('input').val().trim(),
                    $describe = $(this).find('td').eq(2).find('input').val().trim();
                var status = {};
                if ($code == '' || $msg == '' || $describe == '') {
                    alert("错误状态码全都为必填选项");
                    checkIsTrue = false;
                    return false;
                }
                status.code = $code;
                status.msg = $msg;
                status.describe = $describe;
                data.status.push(status);
            }
        });
    }
    apiInfo.result = data;
    return checkIsTrue;
}
// 保存API
function saveAPI() {
    if (!apiResultInfo()) {
        return;
    }
    
    apiInfo.appName = $("#appName").val();
    console.log(JSON.stringify(apiInfo));
    $.ajax({
        type: "post",
        url: '/static/addAPI',
        async: true,
        data: JSON.stringify(apiInfo),
        dataType: "json",
        success: function (result) {
            if (result.status == 200) {
                var res = result.data;
                if (res == 0) {
                    alert("创建失败了"+result);
                    console.log(result);
                } else {
                	if (confirm("创建成功,是否继续创建API")) {
                    	window.location.replace(window.location.href);
                    }else{
                    	window.location.href = "/static/getAPI/" + apiInfo.apiName;
                    }
                }
            } else {
                if (result.status == 1444) {
                    alert("创建失败:已经存在同名的API");
                } else {
                    alert("创建失败"+result.data);
                    console.log("创建失败status:" + result.status + " ,msg:"
                        + result.msg + " ,data:");
                    console.log(result.data);
                }
            }
        },
        error: function () {
            alert("创建失败");
        }
    });
}

// 更新API
function updateAPI(){
    if (!apiResultInfo()) {
        return;
    }
    apiInfo.apiCreateTime = $("#apiCreateTime").val();
    apiInfo.appName = $("#appName").val();
    console.log(JSON.stringify(apiInfo));
    $.ajax({
        type: "post",
        url: '/static/updtAPI',
        async: true,
        data: JSON.stringify(apiInfo),
        dataType: "json",
        success: function (result) {
            if (result.status == 200) {
                var res = result.data;
                if (res == 0) {
                    alert("修改失败了"+result);
                    console.log(result);
                } else {
                    alert("修改成功,如果需要该的API生效,请重启该API");
                    window.location.href = "/static/getAPI/" + apiInfo.apiName;
                }
            } else {
                if (result.status == 1444) {
                    alert("创建失败:已经存在同名的API");
                } else {
                    alert("创建失败"+result.data);
                    console.log("创建失败status:" + result.status + " ,msg:"
                        + result.msg + " ,data:");
                    console.log(result.data);
                }
            }
        },
        error: function () {
            alert("创建失败");
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

init();