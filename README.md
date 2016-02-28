# ECharts的Java注解框架 EChartsAnnotation
  用原生Java注解来映射ECharts的Option类，提供Annotation到JSON的转换功能。
  
## 思路 How it works
* Step 1：在后台使用Annotation来标注Bean类
* Step 2：注解处理器转换成JSON树
* Step 3：使用JSON系列化工具包（fastjson/gson）输出到前端页面

## 注解 Annotation
    基于ECharts3.0制作
    >http://echarts.baidu.com/documents/cn/option.json?_v_=1453695515722
    
    从上述URL得到JSON文件总体上可以解析成树，除了series、dataZoom和visualMap三个节点需要特殊处理，其他非叶子节点均可映射成文件系统的文件夹，而叶子节点则映射成Java源文件即注解文件。
    总共有3700+个注解！
    根据JSON树的叶子节点的Type属性中的不同类型，3700多注解分成6种不同类型:
    <table>
    <thead>
      <tr><th>JS类型</th><th>Java类型</th><th>默认值</th><th>文件名后缀</th><th>备注</th></tr>
    </thead>
    <tbody>
      <tr><td>boolean</td><td>boolean</td><td>false</td><td>Boolean</td><td>布尔类型</td></tr>
      <tr><td>Color</td><td>int</td><td>0</td><td>Hex</td><td>Web颜色映射成Java整型，由于常用16进制表示所以后缀是Hex</td></tr>
      <tr><td>number</td><td>Number</td><td>0</td><td>Number</td><td>抽象类Number是int、double等基本类型的父类，一律转为double</td></tr>
      <tr><td>string</td><td>String</td><td>""</td><td>String</td><td>字符串类型</td></tr>
      <tr><td>Array</td><td>List</td><td>null</td><td>Array</td><td>数组类型一律转成泛型List</td></tr>
      <tr><td>Function</td><td>Object</td><td>null</td><td>Function</td><td>由于Java不支持函数类型，所以需要重载Object的toString方法输出字符串的“函数”</td></tr>
      <tr><td>*</td><td>Object</td><td>null</td><td>All</td><td>参考Function类型</td></tr>
    </tbody>
    </table>
## 注解处理器 AnnotationProcessor

## 如何使用 Get Started

