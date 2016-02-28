# ECharts的Java注解框架 EChartsAnnotation
  用原生Java注解来映射ECharts的`Option`类，提供Annotation到`JSON`的转换功能。
  
## 思路 How it works
`1`在后台使用Annotation来标注Bean类  
`2`注解处理器转换成JSON树  
`3`使用JSON系列化工具包（fastjson/gson）输出到前端页面  

## 注解 Annotation
基于ECharts3.0制作
[Option定义](http://echarts.baidu.com/documents/cn/option.json?_v_=1453695515722 "点击下载JSON文件")  
从上述URL得到JSON文件总体上可以解析成树，除了series、dataZoom和visualMap三个节点需要特殊处理，  
其他非叶子节点均可映射成文件系统的文件夹，而叶子节点则映射成Java源文件即注解文件。  
总共有3700+个注解！  
根据JSON树的叶子节点的Type属性中的不同类型，3700多注解分成6种不同类型:  

JS类型|Java类型|默认值|文件名后缀|备注
-------|--------|------|----------|----
boolean|boolean|false|Boolean|布尔类型
Color|int|0|Hex|Web颜色映射成Java整型，由于常用16进制表示所以后缀是Hex
number|Number|0|Number|抽象类Number是int、double等基本类型的父类，一律转为double
string|String|""|String|字符串类型
Array|List|没有默认值|Array|数组类型一律转成泛型List
Function|Object|没有默认值|Function|由于Java不支持函数类型，所以需要重载Object的toString方法输出字符串的“函数”
*|Object|没有默认值|All|参考Function类型

## 注解处理器 AnnotationProcessor

## 如何使用 Get Started

