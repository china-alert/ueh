# 快速了解
## 产品介绍

信息中心拥有大量的IT基础设施和应用，并采购了多套监控工具，每套监控工具都有自己独立的监控体系和告警通知方式，竭峙统一事件平台UEH（Unified Event Handler）可对各监控工具产生的告警进行汇聚，统一处理、集中展示，并通知。

主要功能包括：

- 事件接入
- 事件处理（事件丰富、去重、合并、事件分类、事件升降级、通知屏蔽）
- 告警通知（通知媒介、通知组、通知人）

## 定位
竭峙统一事件平台UEH（Unified Event Handler）可作为独立的事件平台使用；
也可以作为部门级的事件处理工具，对监控工具产生的告警预处理后（事件丰富、去重、合并、事件分类、事件升降级、通知屏蔽），再推送至上一级事件管理工具，由上一级事件平台再进行告警通知等动作，从而有效降噪，精准告警。

## 产品特点
- 可靠性高、管理容量大、轻量化、低成本以及开放集成
- 预集成对Zabbix、广州云新、新数等第三方主流监控软件的管理能力
- 开放式架构，支持HTTP协议 、REST API规范、Webhook接口，方便不同监控软件告警对接集成。

## 逻辑架构
![image](https://github.com/user-attachments/assets/0c8c4908-4009-4ea2-9a78-30613c9eb147)

## 外部接口
竭峙统一事件平台UEH（Unified Event Handler）对外提供接口

## 技术栈
### 相关组件版本
| **序号** | **数据库名称** | **版本** | **用途**                    |
| -------- | -------------- | -------- | ------------------------- |
| 1.       | PostgreSql     | 15.2     | 存储告警事件               |
| 2.       | Tomcat         | 9.0.74   | Web应用服务                |
| 3.       | jdk            | 8u231    | JAVA 开发环境              |
| 4.       | Mysql          | 8.0.32   | 存放集中监控系统的配置数据   |

### 运行环境

| **序号** | **操作系统** | **版本**   | **备注** |
| -------- | ------------ | ---------- | -------- |
| 1        | Centos       | 7 及以上   |          |
| 2        | redhat       | 7   及以上 |          |
| 3        | Ubuntu lTL   |  16.04及以上|          |
| 4        | 统信 UOS     |    V10  |          |
| 5         |    麒麟          |  V10          |          |

### 服务器建议配置
| **CPU** | **内存** | **硬盘容量** |
| ------- | -------- | ------------ |
| 4核     |    8G      |    500G （ 系统盘和数据盘分开 ）  |

## 在线体验
[点击跳转到在线体验](http://123.60.39.174:8890/ueh)

用户名:admin 密码：123456
## 在线文档
[点击跳转到详细文档](http://123.60.39.174:18181/docs/ueh/32)
## 联系我们
TEL:18001261978


# 快速安装
## 安装前准备
### 介质下载

| **安装顺序**       | **安装文件**                   | **用途**                  |**下载地址**                  |
| ------------------ | ------------------------------ | ------------------------- | ------------------------- |
| 1. Web服务   | setup-web.zip   | Web服务Tomcat             |http://download.s21i.co99.net/29000100/0/0/ABUIABBPGAAg5NrZugYo2eflsQE.zip?f=setup-web.zip&v=1733717374             |
| 2. 数据库文件       | setup-script.zip                     | 门户UMC程序数据库文件 |http://download.s21i.co99.net/29000100/0/0/ABUIABBPGAAgn9vZugYo4O3H0gE.zip?f=setup-script.zip&v=1733717407             |
| 3. 事件处理程序        |  setup-backend.zip| 后台事件处理 |http://download.s21i.co99.net/29000100/0/0/ABUIABBPGAAg9pPZugYopaLtuwc.zip?f=setup-backend.zip&v=1733708326             |
| 4. jdk        |  setup-jdk.zip| java环境 |http://download.s21i.co99.net/29000100/0/0/ABUIABBPGAAg1ZHaugYojIP90AM.zip?f=setup-jdk.zip&v=1733724451            |

### 创建/app/images目录，用于临时存放安装文件

```shell
mkdir -p /app/images/
```
### 上传安装介质到/app/images目录

```shell
使用命令或者上传工具上传
```
### 解压安装介质

```shell
cd /app/images/
unzip "*.zip"
```
## 数据库导入
### Mysql数据库导入
已有Mysql可以直接导入，[点击跳转到Mysql参考安装](http://123.60.39.174:18181/docs/ueh/ueh-1g0ru1s3jt6ub)

```shell
mysql -uroot -ppassword -h127.0.0.1
# 登录到门户mysql数据库
create database umc
# 创建umc数据库
use umc
#切换到umc数据库
source /app/images/umc.sql
# 导入数据库脚本
```
### postgresql数据库导入
已有Postgresql可以直接导入，[点击跳转到Postgresql参考安装](http://123.60.39.174:18181/docs/ueh/Install-PostgreSQL)

使用建库脚本ueh.gz执行导入数据库操作。

```shell
su - postgres
# 切换到postgres用户
/app/images/ueh.sql | psql ueh
# 导入数据库
exit
# 退出
```

### 修改门户umc数据库参数

连接MySQL的umc数据库，修改t_view_page_datasource表里的数据库的IP地址、用户名、密码。
```shell
mysql -uroot -p
# 登录到mysql数据库
use umc
# 切换到umc数据库
update t_view_page_datasource set url=replace(url,'127.0.0.1','postgresql数据库地址'),password=replace(password,'123456','postgresql数据库密码') where database_type='postgresql';
# 修改数据源中postgresql数据库连接和密码
update t_view_page_datasource set url=replace(url,'127.0.0.1','umc门户数据库地址'),password=replace(password,'123456','umc门户数据库密码')  where database_type='mysql';
# 修改数据源中Mysql数据库连接和密码
quit
# 退出
```

### 修改事件处理ueh数据库参数

连接postgresql的ueh_admin数据库，修改t_view_page_datasource表里的数据库的IP地址、用户名、密码。
```shell
su - postgres
# 切换到postgres用户
psql
# 进入PostgreSQL数据库
update ueh_admin.t_view_page_datasource set url=replace(url,'127.0.0.1','umc门户数据库地址'),password=replace(password,'123456','umc门户数据库密码')  where database_type='mysql';
# 修改数据源中Mysql数据库连接和密码
update ueh_admin.t_view_page_datasource set url=replace(url,'127.0.0.1','postgresql数据库地址'),password=replace(password,'123456','postgresql数据库密码') where database_type='postgresql';
# 修改数据源中postgresql数据库连接和密码
quit
# 退出PostgreSQL命令行客户端
```

## 前端操作界面安装
### 包括如下2部分程序安装

| **安装顺序**       | **安装文件**                   | **用途**                  |
| ------------------ | ------------------------------ | ------------------------- |
| 1. JDK安装         | jdk-8u201-linux-x64.tar.gz<br>setup-jdk.sh | JAVA 运行环境             |
| 2. Web服务Tomcat   | apache-tomcat-9.0.74.tar.gz <br>setup-tomcat.sh   | Web服务Tomcat             |

### 安装JDK
| **顺序** | **参数**                              | **说明**                       |
| -------- | ------------------------------------- | ------------------------------ |
| 1.       | install_base="/app" |install_base:Java环境JDK安装目录 |
```shell
cd /app/images/
# 进入jdk安装目录
vi setup-jdk.sh
# 修改JDK安装脚本参数,(见setup-jdk.sh 参数说明)
sh setup-jdk.sh install
# 安装jdk
```
### 安装tomcat
| **序号** | **参数**            | **说明**                    |
| -------- | ------------------- | --------------------------- |
| 1.       | install_base="/app" | install_base:Tomcat安装目录 |
```shell
cd /app/images/
# 进入tomcat安装目录
vi setup-tomcat.sh
# 修改TOMCAT安装脚本参数,(见setup-tomcat.sh 参数说明)
cd /app/apache-tomcat-9.0.74/webapps/umc/WEB-INF/classes
# 进入umc文件夹
vi application-druid.yml
# 编辑数据库连接配置文件，并保存退出
url: jdbc:mysql://127.0.0.1:3306/umc?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
username: root
password: 123456
# application-druid.yml 文件内容
/app/apache-tomcat-9.0.74/bin/startup.sh
# 启动tomcat
```
### 服务检查
1、使用命令`ps -ef |grep tomcat`查看Tomcat服务，如出现tomcat字样代表服务已启动

2、在浏览器中输入http://127.0.0.1/umc 当出现如下界面，则表示操作安装成功，登录用户名admin,密码123456。
![image](https://github.com/user-attachments/assets/fc7e0a87-b202-4ec5-a218-ba15435161e7)


## 后端处理程序安装，UEH（Unified Event Handler）可对各种监控工具产生的告警进行汇聚和处理

### UEH模块安装

1、通过FTP方式上传安装文件ueh.zip至/app/images/目录下，并解压：
```shell
unzip -d /app/ueh /app/images/setup-backend.zip
# 解压总安装包
cd /app/ueh
# 切换到安装目录
unzip "ueh*.zip"
# 解压分类安装包
```

2、修改数据库连接、用户名、密码
```shell
find . -iname application.yml|xargs sed -i 's/127.0.0.1:5432/新postgresql数据库地址:新postgresql数据库端口/g'
# 修改数据库地址
find . -iname application.yml|xargs sed -i 's/root/新postgresql用户名/g'
# 修改数据库用户名
find . -iname application.yml|xargs sed -i 's/123456/新postgresql密码/g'
# 修改数据库密码
```

### 维护服务

```shell
ueh_start.sh .
# 启动事件服务
ueh_status.sh .
# 查看事件服务状态
ueh_stop.sh .
# 停止事件服务
```

# 快速事件接入
[Zabbix事件接入](http://123.60.39.174:18181/docs/ueh/ueh-1g0d89jk0b751)

[API接口事件接入](http://123.60.39.174:18181/docs/ueh/ueh-1g0d89jk0b751)
