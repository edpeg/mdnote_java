# Markdown笔记系统 ——后端

[前端链接](https://github.com/edpeg/mdnote_vue.git)

## Markdown笔记系统简介

* 类似有道云笔记的一个Markdown格式笔记系统，使用前后端分离设计。
* 支持图片的本地上传与七牛云上传两种方式
* 支持笔记关键字搜索与高亮展示

## 编译运行

1. 创建MySQL数据库`mdnote`,导入表结构 `sql/mdnote.sql`
2. 替换application-profile-template.yml里的mysql、redis、es、七牛云等配置参数
3. maven编译运行 `mvn spring-boot:run`
[nginx.conf](..%2F..%2F..%2Fnginx%2Fnginx-1.24.0%2Fconf%2Fnginx.conf)
## 系统设计

### 依赖服务

* `MySQL 8.0.28` 用于存储用户和笔记数据
* `Redis 7.0.6` 结合SpringSession 用于保存用户登录会话
* `Elasticsearch` 用于实现笔记的查询和高亮反显
* `七牛云` 用于支持笔记内插入图片，现支持七牛云与本地存储两种方式。

### 模块划分

```
top.openfbi.mdnote 
├── MkDownJavaApplication.java // 启动入口
├── common // 统一异常处理、统一返回值
├── config // Spring配置类，登录拦截器与内部接口拦截器，以及Es等组件配置
├── note // 笔记检索及高亮
├── user // 用户注册及登录
└── utils // 字符串，UUID，MD5等常用组件
```
   