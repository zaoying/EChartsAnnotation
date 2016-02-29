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
![](https://github.com/zaoying/EChartsAnnotation/blob/master/doc/Option_Annotation.png)  
总共有3700+个注解！这里生成的注解只能用于标记Bean类的域`Field`  
根据JSON树的叶子节点的Type属性中的不同类型，3700多注解分成6种不同类型:  

JS类型|Java类型|默认值|文件名后缀|备注
-------|--------|------|----------|----
boolean|boolean|false|Boolean|布尔类型
Color|int|0|Hex|Web颜色映射成Java整型，由于常用16进制表示所以后缀是Hex
number|Number|0|Number|抽象类Number是int、double等基本类型的父类，一律转为double
string|String|""|String|字符串类型
Array|List|无|Array|数组类型一律转成泛型List
Function|Object|无|Function|由于Java不支持函数类型，所以需要重载Object的toString方法输出字符串的“函数”
*|Object|无|All|参考Function类型

按注解的参数个数分，可以分为两种：  
######标记注解
标记注解没有参数。如果被标记的元素为`null`或者等于默认值，注解处理器将不会输出任何东西  
######单值注解
单值注解只有一个参数。该参数的类型跟注解名件名末尾的单词有关。  
由于许多叶子节点有多种类型，所以一个叶子节点可能生成多个注解，但是注解处理器把这些注解当作同一个注解输出。  
在单值注解的参数不为空和被标记的元素不为`null`且不等于默认值的情况下，注解处理器会优先输出被注解元素的值。  
## 注解处理器 AnnotationProcessor
######注解处理器专用的注解：  
`SingleChart`和`DuplexChart`用来标记需要被转换成JSON的Java Bean类，  
这两个注解不同之处在于处理`series`、`dataZoom`和`visualMap`下面的注解时，  
同一个类多次同个注解文件，前者把重复出现的注解合并处理；  
后者通过`AddSeries`、`AddDataZoom`和`AddVisualMap`三种注解提取被`SingleChart`标记的Java文件中的  
`series`、`dataZoom`和`visualmap`,并添加到当前被`DouplexChart`标记的文件中。
######SingleChart
参数 `exportTo` 默认值是"",不等于默认值时向本地磁盘写入"exportTo".json  
参数 `extendsFrom` 默认值是"",不等于默认值时,将继承自"extendsFrom.json"  
######DupelxChart
参数 `exportTo` 默认值是"",不等于默认值时向本地磁盘写入"exportTo".json  
参数 `extendsFrom` 默认值是"",不等于默认值时,将继承自"extendsFrom.json"  
######AddSeries
只能用于`DuplexChart`的域`Field`，若域不为null且域的类源文件被`SingleChart`注解标记，  
将会提取源文件中`series`下的注解并添加到`DuplexChart`  
######AddDataZoom
只能用于`DuplexChart`的域`Field`，若域不为null且域的类源文件被`SingleChart`注解标记，  
将会提取源文件中`dataZoom`下的注解并添加到`DuplexChart`   
######AddVisualMap
只能用于`DuplexChart`的域`Field`，若域不为null且域的类源文件被`SingleChart`注解标记，  
将会提取源文件中`visualMap`下的注解并添加到`DuplexChart`   
## 如何使用 Get Started
`1`[添加EChartsAnnotation到项目](https://github.com/zaoying/EChartsAnnotation/blob/master/out/artifacts/EChartsAnnotaion/EChartsAnnotaion.jar)
`2`增加LineChart.java如下
```Java
@SingleChart(exportTo = "lineChart")
public class LineChart {
    @NameString
    String name;
    @DataArray
    double[] data;
}
```
`3`调用注解处理器
```Java
import cn.edu.gdut.zaoying.Charts.CombinedChart;

public class EChartsTest {
    public static void main(String[] args) {
      LineChart lineChart=new LineChart();
      lineChart.name="线性表一";
      lineChart.data=new double[]{1,2,3,4};
      Object json=EChartsAnnotationProcessor.phraseSingleChart(lineChart);
      System.out.print(JSON.toJSONString(json));
    }
}
```
