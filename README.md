<p align="center">
  <img src="./doc/UEH.png" alt="ueh-logo" width="85" />
  <img src="./doc/jiezhi-logo.png" alt="jiezhi-logo" width="300" />
  <h2 align="center">可对各种监控工具产生的告警进行汇聚，统一处理、集中展示，并通知</h3>
  <p align="center">业务都在公有云上的客户，可以选择 SaaS 事件管理平台，如国外的PagerDuty、DataDog、国内的有Flashduty等。</p>
  <p align="center">但对告警不能外发的场景，显然SaaS方案不可采用，必须有一套自建的、私有化部署的“统一事件平台”</p>
  <p align="center">竭峙统一事件平台UEH（Unified Event Handler）以事件为纽带，从可用性管理角度，将人、工具、流程连接起来，建立 IT 故障管理的标准化过程和运营体系，大幅提升了IT运维的质量和效率。竭峙的创始团队深耕智能监控领域20年，早年是IBM Omnibus 产品实施交付团队，积累了大量金融、国央企IT故障管理的行业经验，</p>
</p>
<p align="center">
    <img alt="GitHub Release" src="https://img.shields.io/github/v/release/china-alert/ueh"> </a>
    <img alt="Github Stars" src="https://img.shields.io/github/stars/china-alert/ueh?color=%231890FF&style=flat-square"> </a>
    <img alt="License" src="https://img.shields.io/github/license/china-alert/ueh?color=purple"> </a>
</p>



# 快速了解
## 产品介绍

随着分工越来越细，数据中心使用的监控工具越来越多，常见有基础架构监控Zabbix、擅长容器监控Prometheus、私有云的各种云管平台，数据库监控工具、带外监控、BPC旁路协议解析监控工具、Ebpf、APM应用监控等、还有些软件产品会内置自己独立的监控能力，比如联想的scom只提供邮件的通知方式，OceanBase，达梦等都有自己独立的监控体系和告警通知方式。告警事件分散在非常多的地方，形成一个又一个的告警孤岛。如何对IT故障进行更加高效、安全、低成本的管理，成为一个急需解决的问题。


告警事件分散在多处，会带来以下几个问题：
** 1、告警分散、配置分散：比如通知人的配置散落在各处，手机号、邮箱等每个监控工具都要配一遍。通知媒介（短信平台、电话呼叫、企业微信等接口）每个监控工具都需要配置，且有的监控工具可能API需要购买维保才提供服务，或者原厂已经不提供老旧版本的技术支持，或者原厂在国内已经没有服务。<br>

2、事件处理功能不全：大部分监控工具在事件处理功能上较弱，或者需要额外付费才能获得更高级的功能使用，想要做统一的变更窗口告警通知屏蔽（静默）、收敛降噪、筛选过滤、事件信息丰富等，非常痛苦。


3、分散的告警不方便故障定位：临近触发的告警事件在时间维度上是有关联性的，如果把所有的告警放到一个地方展示，辅助故障定位，对于排障分析、故障定位非常有用。

告警带有敏感信息，需要安全措施防范数据泄露。业务都在公有云上的客户，可以选择 SaaS 事件管理平台，如国外的PagerDuty、DataDog、国内的有Flashduty等。对数据不能外出的场景，显然SaaS方案不可采用，必须有一套自建的、私有化的“统一事件管理平台”，竭峙信息的创始团队深耕智能监控领域20年，早年是IBM Omnibus 产品实施交付团队，积累了大量金融、国央企IT故障管理的行业经验，自研产品竭峙统一事件平台UEH（Unified Event Handler）以事件为纽带，从可用性管理角度，将人、工具、流程连接起来，建立 IT 故障管理的标准化过程和运营体系，大幅提升了IT运维的质量和效率。

## 竭峙统一事件平台UEH（Unified Event Handler）可对各种监控工具产生的告警进行汇聚，统一处理、集中展示，并通知。
## 主要功能包括：  
- 事件展示、查询、确认
- 事件接入
- 事件处理（事件丰富、去重、合并、事件分类、事件升降级、通知屏蔽）
- 告警通知（通知媒介、通知组、通知人）

## 使用场景
竭峙统一事件平台UEH（Unified Event Handler）可作为独立的事件平台使用；
也可以作为部门级的事件处理工具，对监控工具产生的告警预处理后（事件丰富、去重、合并、事件分类、事件升降级、通知屏蔽），再推送至上一级事件管理工具，由上一级事件平台再进行告警通知等动作，从而有效降噪，精准告警。

## 产品特点
- 可靠性高、管理容量大、轻量化、低成本以及开放集成
- 预集成对Zabbix、Prometheus、Skyworking、等主流监控工具告警接入，以及阿里云、华为云、腾讯云等第三方主流监控软件的告警集成
- 开放式架构，支持Webhook、HTTP协议 、REST API规范、，方便不同监控软件告警对接集成。监控系统很多，每个监控系统几乎都提供了调用第三方接口的能力，即通过 Webhook 把告警事件推给UEH。但是各个监控系统的推送协议、字段定义都不一样，需要逐个对接，我们提供方便的字段映射功能、和告警级别映射配置。

## 逻辑架构
![image](https://github.com/user-attachments/assets/135e29c9-c5b0-4474-b050-1cdd5be26a67)

## 功能展示
## 运行总览
![image](https://github.com/user-attachments/assets/bf562872-6740-4cb4-b665-0df3ac002020)
## 事件查看
![image](https://github.com/user-attachments/assets/f1e1ae61-e5f3-4f26-afdb-c83037f73286)
## 事件接入
![71e3c094a7e951d5894ce77150bf272](https://github.com/user-attachments/assets/5b0c2bc4-25b6-4cc3-9727-f10ba47c7f59)
## 通知服务
![image](https://github.com/user-attachments/assets/695a8771-0a66-41ee-a58a-c10dcdc1b1d1)

## 外部接口
竭峙统一事件平台UEH（Unified Event Handler）对外提供接口

## 技术栈
### 相关组件版本
| **序号** | **数据库名称** | **版本** | **用途**                    |
| -------- | -------------- | -------- | ------------------------- |
| 1.       | PostgreSql     | 15.2     | 存储告警事件               |
| 2.       | jdk            | 8u231    | JAVA 开发环境              |

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
[点击跳转到在线体验](http://demo.china-alert.com/ )

用户名:admin 密码：123456
## 在线文档
[点击跳转到详细文档](http://doc.china-alert.com/)
## 联系我们
TEL:18001261978

# 快速安装
## 安装前准备
### 介质下载

| **安装顺序**       | **安装文件**                   | **用途**                  |**下载地址**                  |
| ------------------ | ------------------------------ | ------------------------- | ------------------------- |
| 1. 事件处理程序        |  setup-backend.zip| 后台事件处理 |http://download.s21i.co99.net/29000100/0/0/ABUIABBPGAAg0P34vgYo0KablgU.zip?f=setup-backend.zip&v=1742618392             |
| 2. jdk        |  setup-jdk.zip| java环境 |http://download.s21i.co99.net/29000100/0/0/ABUIABBPGAAgjOydvAYo0vXL9gE.zip?f=setup-jdk.zip&v=1736930919            |

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
unzip -d /app/ueh "*.zip"
```
## 数据库导入
### postgresql数据库导入
已有Postgresql可以直接导入，[点击跳转到Postgresql参考安装](http://ueh.china-alert.com:18181/docs/uehueh-1g6jq090f6elj/Install-PostgreSQL)

使用建库脚本ueh.sql执行导入数据库操作。

```shell
cd /app/ueh
# 切换到数据库脚本所在目录
su - postgres
# 切换到postgres用户
psql -dpostgres -Uroot -W -fueh.sql
# 导入数据库
exit
# 退出
```

### 修改事件处理ueh数据库参数

连接postgresql的ueh_admin数据库，修改t_view_page_datasource表里的数据库的IP地址、用户名、密码。
```shell
su - postgres
# 切换到postgres用户
psql
# 进入PostgreSQL数据库
update ueh_admin.t_view_page_datasource set url=replace(url,'127.0.0.1','postgresql数据库地址'),password=replace(password,'123456','postgresql数据库密码') where database_type='postgresql';
INSERT INTO ueh_admin.t_view_page_dataset (name, ds_group, label_text, dataset_type, columns, label_texts, data_types, return_type, exec_type, exec_sql, filter_param_names, filter_values, main_datasource_name, union_dataset_names, union_condition, is_batch, batch_setting, is_enable, project_id, remark, lm_timestamp, union_filter_values) VALUES ('getCandidateByProblemId', '事件通知程序', '根据告警事件ID查询是否自动通知', 'S', 'candidate', '通知人', 'string', 'MRSC', 'QUERY', 'select candidate from t_event_notification_log where "event_id"=''${eventId}''', null, null, 'event_data', null, null, 'N', null, 'Y', 10, null, '2025-01-21 10:44:55.000000', null);
# 修改数据源中postgresql数据库连接和密码
quit
# 退出PostgreSQL命令行客户端
```

## 环境安装
### 包括如下程序安装

| **安装顺序**       | **安装文件**                   | **用途**                  |
| ------------------ | ------------------------------ | ------------------------- |
| 1. JDK安装         | jdk-8u201-linux-x64.tar.gz<br>setup-jdk.sh | JAVA 运行环境             |

### 安装JDK
| **顺序** | **参数**                              | **说明**                       |
| -------- | ------------------------------------- | ------------------------------ |
| 1.       | install_base="/app" |install_base:Java环境JDK安装目录 |
```shell
cd /app/images/
# 进入jdk安装目录
unzip setup-jdk.zip
# 解压
sh setup-jdk.sh install
# 安装jdk
```

## UEH处理程序安装，UEH（Unified Event Handler）可对各种监控工具产生的告警进行汇聚和处理

### UEH模块安装

1、通过FTP方式上传安装文件ueh.zip至/app/images/目录下，并解压：
```shell
cd /app/ueh
# 切换到安装目录
unzip "ueh*.zip"
# 解压分类安装包
```

2、修改数据库连接、用户名、密码
```shell
find . -iname application.yml|xargs sed -i 's/127.0.0.1:5432/新postgresql数据库地址:新postgresql数据库端口/g'
find . -iname application-druid-pg.yml|xargs sed -i 's/127.0.0.1:5432/新postgresql数据库地址:新postgresql数据库端口/g'
# 修改数据库地址
find . -iname application.yml|xargs sed -i 's/postgres/新postgresql用户名/g'
find . -iname application-druid-pg.yml|xargs sed -i 's/postgres/新postgresql用户名/g'
# 修改数据库用户名
find . -iname application.yml|xargs sed -i 's/123456/新postgresql密码/g'
find . -iname application-druid-pg.yml|xargs sed -i 's/123456/新postgresql密码/g'
# 修改数据库密码
```

### 维护服务

```shell
sh ueh_start.sh .
# 启动事件服务
sh ueh_status.sh .
# 查看事件服务状态
sh ueh_stop.sh .
# 停止事件服务
```
### 服务检查

1、在浏览器中输入http://127.0.0.1/umc 当出现如下界面，则表示操作安装成功，登录用户名admin,密码123456。

# 快速事件接入
[Zabbix事件接入](http://ueh.china-alert.com:18181/docs/uehueh-1g6jq090f6elj/ueh-1g0d89jk0b751)

[API接口事件接入](http://ueh.china-alert.com:18181/docs/uehueh-1g6jq090f6elj/ueh-1g3u2vo84cv9q)
