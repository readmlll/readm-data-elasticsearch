# readm-data-elasticsearch(轻量级spring boot  集成 elasticsearch restClient方式连接框架)
### 轻量级elasticsearch restClient方式连接框架/阿里云elasticsearch连接框架



### （实测阿里云es 6.7.0没问题，注意开启公网访问，用户名密码，以及设置好白名单）

#### 基于spring boot,(spring-data-es部分注解)、elasticsearch高低版本client

#### 封装的rest版本es客户端. 仿照spring-data 一贯的使用方式，只需少量代码即可转换transport连接方式到restClient连接

## 单独依赖版本，spring boot自动配置，使用方引入只需定义好文档类，自动创建es文档，定义好仓库类开箱即用常用操作，自动配置
## 实现全部ElasticsearchRepository接口


###### 优点:

目前最新spring-data-es 是3.1.0  Elasticsearch模板还是不支持restClient连接 （据说3.2.0 会支持）

使用本框架
小项目，轻量级使用es的项目，快速方便的转换transport连接方式到restClient连接，还是使用spring-data-es的习惯，一套代码两种方式连接方式可跑。 

我自己的是从自己搭建的es迁移到阿里云es 6.7.0 ，阿里云不支持transport
常用操作的封装，创建文档，查询总数，批量增加修改，批量删除，高亮显示

###### 缺点：

自由度不够，更多属性需要自己修改源码。

于spring-data-es不同点总结：
1.实体对象需要继承EsEntity，必须有id字段且其为string类型 且该字段需要标注@Field注解。
2.仓库对象 不再使用继承接口，需要继承EsRepository类。 
3.没有动态生成方法。 但支持ElasticsearchRepository所有接口声明的方法



###### 关于反射的冗余：

本来是想写工具类的，所以EsRestClient有反射代码的冗余 介意的话自己修改。 

有点功能洁癖写着写着 还是当demo级别框架写
我自己是使用编译jar包 在别的项目中引入本"demo框架" 当做正常框架使用。
你可以抽离代码放到你的项目中，也可以选择和我一样编译jar，引入依赖。 
我会在github上提供一份编译好的jar


本框架主要类目录结构 (详细目录结构在下方)

![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/dir1.png)




###### 项目示例和框架源码目录结构:

common_parent为框架父级总管理项目
  data为次级管理项目
    es-rest为框架项目

paper_site为 使用本框架示例 
	打开idea编译好common_parent项目 安装jar到本地

​	再打开paper_site项目(添加好依赖)
​	填写好yml配置信息 运行readm.paper.PaperApplicationTests.contextLoads 测试方法，
​	即可自动创建es文档 和相关测试代码  (注意打开相关注释等等)





![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/dir2.png)
![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/dir3.png)




文档对象声明 和 其字段声明



![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/U0XXWZV6%24G%40%7BMBYH2~%24WH14.png)



仓库对象定义



![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/D3%5BKBZ39E37NWRC6XMK5MX6.png)





测试仓库方法 



![U0XXWZV6$G@{MBYH2~$WH14](https://github.com/readmlll/readm-data-elasticsearch/blob/master/assets/test.png)
