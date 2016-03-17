package cn.edu.gdut.zaoying;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sun.istack.internal.NotNull;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 *
 * Simple To Introduction
 * 项目名称:  [${project_name}]
 * 包:        [${package_name}]
 * 类名称:    [${type_name}]
 * 类描述:    [ECharts注解处理器]
 * 创建人:    [${user}]
 * 创建时间:  [${date} ${time}]
 * 修改人:    [${user}]
 * 修改时间:  [${date} ${time}]
 * 修改备注:  [增加中文文档]
 * 版本:      [v1.0]
 *
 */
public class EChartsAnnotationProcessor {
    public enum Type{BOOLEAN,COLOR,NUMBER,ARRAY,STRING,FUNCTION,ALL}//注解类型
    private List<Map<String,Object>> series = null;//系列
    private List<Map<String,Object>> dataZoom = null;//数据缩放
    private List<Map<String,Object>> visualMap = null;//可视化地图
    Map<String,Object> head;//JSON树根

    /**
     * 注解处理器无参构造方法
     */
    public EChartsAnnotationProcessor() {
        head=new HashMap<>();
    }

    /**
     * 解析参数object中的注解并返回JSON树
     * @param object 使用了SingleChart或者DuplexChart注解标记的类
     * @return 返回JSON树的根，节点结构为Map<String,Object>。但series、dataZoom和visualMap节点为List<String,Object>
     */
    public static Map<String,Object> parseChart(@NotNull Object object){
        Map<String,Object> head = new HashMap<>();//JSON树根
        EChartsAnnotationProcessor processor=new EChartsAnnotationProcessor();
        String exportTo=null,extendFrom;//exportTo是导出JSON文件的路径+文件名，extendFrom则是“继承”的JSON文件
        Class clazz=object.getClass();
        Annotation singleChartAnnotation=clazz.getAnnotation(SingleChart.class);
        Annotation duplexChartAnnotation=clazz.getAnnotation(DuplexChart.class);
        if(singleChartAnnotation!=null){//检测是否存在SingleChart注解
            SingleChart singleChart= (SingleChart) singleChartAnnotation;
            exportTo=singleChart.exportTo();
            extendFrom=singleChart.extendFrom();
            processor.preParseChart(extendFrom);//预处理
            head=processor.parseSingleChart(object);//正式处理
        }
        else if(duplexChartAnnotation!=null){//检测是否存在DuplexChart注解
            DuplexChart duplexChart= (DuplexChart) duplexChartAnnotation;
            exportTo=duplexChart.exportTo();
            extendFrom=duplexChart.extendFrom();
            processor.preParseChart(extendFrom);//预处理
            head=processor.parseDuplexChart(object);//正式处理
        }
        if(!"".equals(exportTo)){
            processor.exportToFile(exportTo);//如果exportTo是有效的路径和文件名就输入JSON文件到磁盘
        }
        return head;
    }

    /**
     * JSON字符串的持久化方法
     * @param exportTo 导出JSON文件的路径+文件名
     */
    public void exportToFile(String exportTo){
        String json=JSON.toJSONString(head);
        try {
            FileWriter fileWriter=new FileWriter(exportTo);
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("文件："+exportTo+"不存在！");
        }
    }

    /**
     * 预处理方法：“继承”的功能主要实现
     * 调用parseFile方法把JSON文件反序列化成JSON树
     * 再把series、dataZoom和visualMap节点先备份到类变量中再删除
     * @param extendFrom 被反序列化的JSON文件
     */
    private void preParseChart(String extendFrom){
        if(!"".equals(extendFrom)){
            try {
                head= parseFile(new File(extendFrom));
                series = (List<Map<String, Object>>) head.get("series");//备份series
                dataZoom = (List<Map<String, Object>>) head.get("dataZoom");//备份dataZoom
                visualMap = (List<Map<String, Object>>) head.get("visualMap");//备份visualMap
                if(series!=null)head.remove("series");//删除series
                if(dataZoom!=null)head.remove("dataZoom");//删除dataZoom
                if(visualMap!=null)head.remove("visualMap");//删除visualMap
            } catch (FileNotFoundException e) {
                System.out.println("文件："+extendFrom+"不存在！");
            }
        }
    }
    /**
     * 后处理方法
     * 把series、dataZoom和visualMap节点合并后重新放回JSON树
     */
    private void postParseChart(){
        List<Map<String, Object>> list= (List<Map<String, Object>>) head.get("series");
        if(list==null)head.put("series",series);
        else if(series!=null)list.addAll(series);

        list= (List<Map<String, Object>>) head.get("dataZoom");
        if(list==null)head.put("dataZoom",dataZoom);
        else if(dataZoom!=null)list.addAll(dataZoom);

        list= (List<Map<String, Object>>) head.get("visualMap");
        if(list==null)head.put("visualMap",visualMap);
        else if(visualMap!=null)list.addAll(visualMap);
    }

    /**
     * 调用主解析方法parse()解析SingleChart注解
     * 再调用turnMapIntoList()方法把series、dataZoom和visualMap节点分别转换成List<Map<String,Object>>
     * @param object 使用了SingleChart注解标记的类
     * @return 返回JSON树的根
     */
    private Map<String,Object> parseSingleChart(@NotNull Object object){
        head= parse(head,object);
        head.put("series",turnMapIntoList((Map<String, Object>) head.get("series")));
        head.put("dataZoom",turnMapIntoList((Map<String, Object>) head.get("dataZoom")));
        head.put("visualMap",turnMapIntoList((Map<String, Object>) head.get("visualMap")));
        postParseChart();
        return head;
    }

    /**
     * 调用主解析方法parse()解析DuplexChart注解
     * 再调用parse()方法处理被AddSeries、AddDataZoom和AddVisualMap标记的Field
     * 提取Field中的series、dataZoom和visualMap节点并转为List，最后把它们放进JSON树中
     * @param object 使用了DuplexChart注解标记的类
     * @return 返回JSON树的根
     */
    private Map<String,Object> parseDuplexChart(@NotNull Object object){
        Class clazz=object.getClass();
        head= parse(head,object);
            Field[] fields=clazz.getDeclaredFields();
            for(Field field:fields)
            {
                Object value=getFieldValue(field,object);
                Annotation addSeries=field.getAnnotation(AddSeries.class);
                if(addSeries!=null){
                    Map<String,Object> hashMap= (Map<String, Object>) parse(new HashMap<String, Object>(),value).get("series");
                    if(series==null)series=turnMapIntoList(hashMap);
                    else series.addAll(turnMapIntoList(hashMap));
                    continue;
                }
                Annotation addDataZoom=field.getAnnotation(AddDataZoom.class);
                if(addDataZoom!=null){
                    Map<String,Object> hashMap= (Map<String, Object>) parse(new HashMap<String, Object>(),value).get("dataZoom");
                    if(dataZoom==null)dataZoom=turnMapIntoList(hashMap);
                    else dataZoom.addAll(turnMapIntoList(hashMap));
                    continue;
                }
                Annotation addVisualMap=field.getAnnotation(AddVisualMap.class);
                if(addVisualMap!=null){
                    Map<String,Object> hashMap=(Map<String, Object>) parse(new HashMap<String, Object>(),value).get("visualMap");
                    if(visualMap==null)visualMap=turnMapIntoList(hashMap);
                    else visualMap.addAll(turnMapIntoList(hashMap));
                }
            }
            head.put("series",series);
            head.put("dataZoom",dataZoom);
            head.put("visualMap",visualMap);
        return head;
    }

    /**
     * 把series、dataZoom和visualMap节点的Map根据Type字段的不同转换为List
     * @param map 只能是series、dataZoom和visualMap节点的Map
     * @return 返回List<Map<String,Object>>
     */
    static List<Map<String,Object>> turnMapIntoList(Map<String,Object>map){
        List<Map<String,Object>> newList=new LinkedList<>();
        if(map==null)return null;
        Set<String> keys=map.keySet();
        for(String key:keys){
            Map<String,Object> element= (Map<String, Object>) map.get(key);
            element.put("type",Util.toFirstLetterLowerCase(key));
            newList.add(element);
        }
        return newList;
    }

    /**
     *按照keys给key逐层查找元素，如果找不到则返回null
     * @param tree Map<String,Object>树
     * @param keys 键数组，按层级排列
     * @return Map<String,Object>或者Null
     */
    public static Map<String,Object> getElementByKeyFromTree(Map<String,Object> tree, String[] keys){
        Map<String,Object> current=tree;
        for(String key:keys){
            if(current==null)return null;
            current= (Map<String, Object>) current.get(key);
        }
        return current;
    }

    /**
     * 读取指定JSON文件，并反序列化成JSON树
     * @param file JSON文件
     * @return 返回JSON树
     * @throws FileNotFoundException
     */
    public static Map<String,Object> parseFile(File file) throws FileNotFoundException {
        FileReader fileReader=new FileReader(file);
        StringBuilder stringBuilder=new StringBuilder(1024);
        try {
            int i=fileReader.read();
            while (i!=-1){
                stringBuilder.append((char)i);
                i=fileReader.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSON.parseObject(stringBuilder.toString(),new TypeReference<Map<String, Object>>(){});
    }

    /**
     * 主解析方法
     * 解析注解并附加到已有的JSON树（继承）
     * @param head JSON树的根
     * @param object 被解析的类
     * @return 合并后的JSON树根
     */
    private Map<String,Object> parse(Map<String,Object> head,@NotNull Object object){
        Class clazz=object.getClass();
            Field[] fields=clazz.getDeclaredFields();
            for(Field field:fields)
            {
                Object value=getFieldValue(field,object);
                Annotation[] annotations=field.getDeclaredAnnotations();
                for(Annotation an:annotations){
                    if(!checkClass(an))continue;
                    if(value==null) addAnnotationToMap(head,an,getAnnotationValue(an));
                    else addAnnotationToMap(head,an,value);
                }
            }
        return head;
    }

    /**
     * 获取被注解的Field的值
     * @param field 成员变量Filed
     * @param object 类的实例
     * @return 如果Field是基本类型且等于默认值则返回null，否则返回object
     */
    static Object getFieldValue(Field field,Object object) {
        Object value = null;
        field.setAccessible(true);
        try {
            value = field.get(object);
            if (value != null) {
                if(value instanceof Number){
                    Number number= (Number) value;
                    if(number.doubleValue()==0.0)value = null;
                }
                else if(value instanceof Boolean){if(!((boolean) value))value = null;}
                else if(value instanceof Character&&value.equals('\0')) value = null;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     *通过检查注解的包名是否以"cn.edu.gdut.zaoying.Option."开头
     * 如果是则返回true，否则返回false
     * @param annotation cn.edu.gdut.zaoying.Option包下的注解
     * @return 布尔值
     */
    public static boolean checkClass(Annotation annotation){
        String canonical=annotation.annotationType().getCanonicalName();
        return canonical.startsWith("cn.edu.gdut.zaoying.Option.");
    }

    /**
     * 通过解析Annotation.toString()得到的字符串来获取注解的参数值
     * @param annotation 注解
     * @return String
     */
    String getAnnotationValue(Annotation annotation){
        boolean b=checkClass(annotation);
        if(b){
            String string=annotation.toString();
            int start=string.indexOf('(');
            int end=string.lastIndexOf(')');
            if(end-start>1){
                int equal=string.indexOf('=')+1;
                return string.substring(equal,end);
            }
        }
        return null;
    }

    /**
     * 根据Annotation的包名路径逐渐深入JSON树的节点，最终把value赋值给叶子节点
     * @param head JSON树的根节点
     * @param annotation 注解
     * @param value 一般是注解参数的值，但是参数值若为空则用被标记的Field代替
     */
    void addAnnotationToMap(Map<String,Object> head, Annotation annotation, Object value){
        String name=annotation.annotationType().getCanonicalName();
        String keys[]=name.substring(27).split("\\.");
        Map<String,Object> current = head;
        for (int i=0;i<keys.length-1;i++){
            String key=keys[i];
            if(key.equals("p0"))key="0";//原为Option.series.markLine.data.0，由于Java语法不允许以数字开头，所以改成p0,现在再改成0
            else if(key.equals("p1"))key="1";//原为Option.series.markLine.data.1，由于Java语法不允许以数字开头，所以改成p1,现在再改成1
            if(current.containsKey(key)){//如果节点存在就进入下一循环
                Object object=current.get(key);
                current= (Map<String, Object>)object;
            }
            else {//如果节点不存在就新建节点，然后继续循环
                Map<String,Object>newMap= new HashMap<>();
                current.put(key,newMap);
                current=newMap;
            }
        }
        String key=keys[keys.length-1];
        Object fit=value;
        //--------根据注解参数的类型去除后缀-----///
        switch (getAnnotationTypeByEndWord(key)){
            case BOOLEAN:
                key=key.substring(0,key.length()-7);
                break;
            case FUNCTION:
                key=key.substring(0,key.length()-8);
                break;
            case NUMBER:
            case STRING:
                key=key.substring(0,key.length()-6);
                break;
            case ARRAY:
                key=key.substring(0,key.length()-5);
                break;
            case COLOR:
                fit="#"+Integer.toHexString(Integer.parseInt((String) value));//生成16进制颜色表达式
                key=key.substring(0,key.length()-3);
                break;
        }
        current.put(Util.toFirstLetterLowerCase(key),fit);//在叶子节点保存注解参数值
    }

    /**
     * 根据注解名称的后缀确定注解的参数类型
     * @param name 注解名称
     * @return Type枚举类型
     */
    static Type getAnnotationTypeByEndWord(String name){
        if(name.endsWith("Boolean"))return Type.BOOLEAN;
        else if(name.endsWith("Hex"))return Type.COLOR;
        else if(name.endsWith("Number"))return Type.NUMBER;
        else if(name.endsWith("Array"))return Type.ARRAY;
        else if(name.endsWith("String"))return Type.STRING;
        else if(name.endsWith("Function"))return Type.FUNCTION;
        else return Type.ALL;
    }
}
