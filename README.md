# 2.0升级说明
VX-API已经在本人公司运行了236天(2018-11-23),核心处理器处理了9000多万次,网关对于我们这种访问量不大的公司来说基本不是问题,但是VX-API当前1.0版本性能还有很大的控件没有压榨出来,部分设计也不是很人性化,所以本人已经在安排时间做2.0版,2.0版性能将大大提升,也会更人性化,推荐大家先加入群里或者关注该项目后等2.0版再使用,2.0版不打算兼容1.0版,但是会出工具将1.0的接口迁移到2.0

# VX-API-Gateway
![logo](https://raw.githubusercontent.com/shenzhenMirren/MyGithubResources/master/image/VX-API-Gateway-Logo_small.png)<br/>
VX-API-Gateway是基于Vert.x(java)开发的API网关,是一个分布式,全异步,高性能,可扩展 ,轻量级的API网关<br/>
VX-API-Gateway的部分流程与页面设计灵感来自阿里云的API网关<br/>
QQ交流群 : 440306757<br/>
### 软件使用说明
[VX-API-Gateway使用帮助文档](http://mirren.gitee.io/vx-api-gateway-doc/)<br/>
# VX-API 执行流程
![flowchart](https://raw.githubusercontent.com/shenzhenMirren/MyGithubResources/master/image/VX-API-Gateway-flowchart.png)
<br>
绿线代表一定会执行,黑线代表当存在时执行,当用户请求的时候,完整的流程按组件顺序由1开始执行到7,如果不满足任意一个组件时请求结束并响应(fail-end-response),当任意组件出现异常时统一进入异常组件(Exception Handler)请求结束并响应错误信息
<br>
API的执行流程参考组件介绍
## 组件介绍
### 1.黑名单检查 
该组件永远会被执行! 用户请求时第一步先经过黑名单检查,VX-API会读取请求中的remote Address并获取用户的host(也就是获取用户的IP地址),如果用户的IP地址在全局黑名单中,结束请求并响应状态码:404,状态信息:you can't access this service;反则继续执行处理...
### 2.访问限制
当创建API时开启了访问限制,该组件会被执行! 访问限制单位分别为:天/小时/分;可以设置API与IP限制,IP的限制不能大于API的限制;<br>
假设开启API限制:1分钟可以访问1000次;如果1分钟内访问次数大于1000将结束请求并响应请求(响应内容为:创建API中定义API返回结果:访问限制返回);反则继续执行处理...
### 3.参数检查
当创建API时如果在定义API请求中添加了入参定义,该组件会被执行! 组件会根据入参定义规定的格式检查请求中的参数;<br>
如果参数不符合入参定义结束请求并响应请求(响应内容为:创建API中定义API返回结果:网关入口参数检查失败返回);反则继续执行处理...
### 4.权限认证
当创建API时开启了安全认证,该组件会被执行! 组件会将流程交给权限认证插件,权限认证插件负责做相关处理后决定将流程交给下一个组件处理或结束请求
### 5.前置处理器
当创建API时开启了前置处理器,该组件会被执行! 组件会将流程交给前置处理器插件,前置处理器插件负责做相关处理后决定将流程交给下一个组件处理或结束请求
### 6.中心处理器(主处理器)
当前面的组件都执行通过时,该组件永远会被执行! 组件会根据服务类型做相应的处理,处理完毕后组件会判断是否开启了后置处理器,如果开启了后置处理器,组件会将流程交给后置处理器,并传递一个标识告诉后置处理器当前组件处理的结果;反则结束请求并返回服务结果;
### 7.后置处理器
当创建API时开启了后置处理器,该组件会被执行! 该组件是正常流程的最后一个组件,组件会收到主处理器的执行结果,组件做完相应操作后必须做对请求的响应用户的请求;
### 8.异常处理器
当以上任意组件在执行的过程中出现了异常,该组件会被执行! 组件会结束请求并响应请求(响应内容为:创建API中定义API返回结果:发生异常/失败返回);
## 服务类型介绍
服务类型指网关主处理器要处理的类型,详情参考类型以下的介绍
### HTTP/HTTPS
表示后台服务是常见的HTTP/HTTPS,也就是网关要将用户的请求发送的目的地,该服务支持带权重的负载均衡支持轮询与哈希策略(默认轮询),自动断路并重试连接;当网关请求后台服务地址失败时会提交给策略,如果同一个服务地址失败两次及以上时,会被策略移除,网关会检测策略中是否存在不可用的服务地址,如果存在会根据设定的重试时间进行重试,重试后如果服务地址可用则将服务地址重新添加到策略中
### 页面跳转 
该服务用于做链接重定向,当用户请求该服务的API时会,网关会返回状态302,告诉浏览器跳转到指定的URL;
### 自定义服务 
自定义服务其实跟前置处理器实现的功能差不多,因为前置处理器可以做完相应处理后响应用户请求,自定义服务中提供了三个默认的实现,分别为获取网关时间戳,返回常量值与Session认证的授权

# 执行方式与环境要求
该项目基于vert.x 3.5.1开发,开发环境jdk1.8_121,理论上只要带有JDK/JRE 1.8以上都可以运行该项目<br/>
可以在发行版中下载已经打包好的或者自己编译打包该项目,方法如下:<br/>
执行mvn clean package appassembler:assemble 对项目进行编译打包<br/>
```html
mvn clean package appassembler:assemble
打包完毕后进入target/VX-API-Gateway/bin执行相应bat/sh文件
```
如果机器上没有JDK8环境,可以自己下载一个JRE/JDK环境并在脚本中指定软件使用哪个JRE/JDK启动<br/>
 修改示例(Windows环境):<br/>
假设jre在D盘/java目录下,可以在start.bat找到%JAVACMD% %JAVA_OPTS% -Dfile....<br/>
在以上语句前cd到jre的所在bin目录,也就是在执行java之前切换到D:/java/jreXXX/bin目录在执行java,相当于设置了一个运行环境<br/>
```html
cd D:/java/jreXXX/bin
%JAVACMD% %JAVA_OPTS% -Dfile.encoding=UTF-8 ...
```
### 软件目录说明:
bin 执行脚本<br/>
conf 配置文件与客户端静态文件<br/>
lib 项目依赖的架包<br/>
logs 日志文件<br/>
temp 缓存/临时文件<br/>
## 项目目录说明
src/main/java中core包存放核心代码,spi包存放用户可以自定义插件的接口与工厂,自定义插件时实现相应的接口并在工场中添加获取该实现的名字与实现类,同时在客户端静态文件中添加相应的名字便可<br/>
## 性能测试
 后台服务输出hello<br/>
Nginx Stable1.12.0 配置<br/>
![nginx-conf](http://mirren.gitee.io/vx-api-gateway-doc/image/other/nginx-conf.png)
<br>
阿里云 centos 7 1G内存1核1兆带宽环境 ab -n 1000 -c 1000 执行结果<br/>
Nginx:ab结果<br/>
![nginx-linux-ab](http://mirren.gitee.io/vx-api-gateway-doc/image/other/linux-1c1n1gnqcq-nginx.png)<br>
VX-API: JVM -Xms512m -Xmx512m 其他默认 ab结果<br>
![VX-API-linux-ab](http://mirren.gitee.io/vx-api-gateway-doc/image/other/linux-1c1n1gnqcq-vx-xsm-512m.png)<br>
Windows 10 8核8G内存 环境 ab -n 100000 -c 2000 执行结果<br>
Nginx:ab结果<br>
![nginx-win-ab](http://mirren.gitee.io/vx-api-gateway-doc/image/other/win-8g8cn10wc2q-nginx.png)<br>
VX-API: JVM -Xms2G -Xmx2G 其他默认 ab结果<br/>
![VX-API-win-ab](http://mirren.gitee.io/vx-api-gateway-doc/image/other/win-8g8cn10wc2q-vx-xsm-2g.png)<br>
