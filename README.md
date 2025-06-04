![image](https://github.com/user-attachments/assets/41970a72-c3a3-4882-b1e2-c9df38d26b9d)


# JSON Repair Java

这是一个用 Java 实现的 JSON 修复工具，可以修复损坏或格式不正确的 JSON 字符串。

99%的代码由claude3.5编写。

## 功能特点

- 修复缺失的引号和括号
- 处理单引号和双引号
- 修复不完整的 JSON 结构
- 处理特殊字符和转义序列
- 支持注释的处理
- 保留字符串值中的特殊字符（如冒号）

## 使用方法
maven
```
<dependency>
    <groupId>cn.54zh</groupId>
    <artifactId>json-repair</artifactId>
    <version>1.0.2</version>
</dependency>
```

gradle
```
implementation 'cn.54zh:json-repair:1.0.2'
```

```java
String badJson = "{\"value_1\": \"value_2\":data\"}";
String goodJson = JsonRepair.repairJson(badJson);
```

## 测试用例

以下是一些测试用例及其修复结果：

```
------
bad_json: {'key': 'string', 'key2': false, "key3": null, "key4": unquoted}
good_json: {"key'":"string', 'key2': false, \"key3\": null, \"key4\": unquoted}"}
------
bad_json: [1, 2, 3, 4
good_json: [1,2,3,4]
------
bad_json: 欢迎访问我的博客54zh.cn,b站账号:猿大晖[]
good_json: []
------
bad_json: {
good_json: {}
------
bad_json: [
good_json: []
------
bad_json: []]
good_json: []
------
bad_json: "
good_json: 
------
bad_json: """
good_json: 
------
bad_json: {"employees":["John", "Anna",
good_json: {"employees\"":["John\"","Anna\""]}
------
bad_json: {foo: [}
good_json: {"foo":[]}
------
bad_json: {"text": "The quick brown fox won\'t jump"}
good_json: {"text\"":"The quick brown fox won\\'t jump\"}"}
------
bad_json: {"value_1": "value_2":data"}
good_json: {"value_1\"":"value_2\":data\""}
------
bad_json: {"value_1": true, COMMENT "value_2": "data"}
good_json: {"value_1\"":true,"COMMENT":"value_2\": \"data\"}"}
------
bad_json: - { "test_key": ["test_value", "test_value2"] }
good_json: {"test_key\"":["test_value\"","test_value2\""]}
------
bad_json: { "content": "[LINK]("https://google.com")" }
good_json: {"content\"":"[LINK](\"https://google.com\")\" }"}
------
bad_json: { "content": "[LINK](", "key": true }
good_json: {"content":"[LINK](","key":true}
------
bad_json: {"key":"",}
good_json: {"key\"":"\",}"}
```

## 依赖要求

- Java 8 或更高版本

## 作者

个人博客: http://54zh.cn

b站: 猿大晖
