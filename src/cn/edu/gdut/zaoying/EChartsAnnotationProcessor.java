package cn.edu.gdut.zaoying;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sun.istack.internal.NotNull;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by 祖荣 on 2016/2/9 0009.
 */
public class EChartsAnnotationProcessor {
    public enum Type{BOOLEAN,COLOR,NUMBER,ARRAY,STRING,FUNCTION,ALL}
    private List<Map<String,Object>> series = null;
    private List<Map<String,Object>> dataZoom = null;
    private List<Map<String,Object>> visualMap = null;
    Map<String,Object> head;

    public EChartsAnnotationProcessor() {
        head=new HashMap<>();
    }
    public static Map<String,Object> parseChart(@NotNull Object object){
        Map<String,Object> head = new HashMap<>();
        EChartsAnnotationProcessor processor=new EChartsAnnotationProcessor();
        String exportTo=null,extendFrom;
        Class clazz=object.getClass();
        Annotation singleChartAnnotation=clazz.getAnnotation(SingleChart.class);
        Annotation duplexChartAnnotation=clazz.getAnnotation(DuplexChart.class);
        if(singleChartAnnotation!=null){
            SingleChart singleChart= (SingleChart) singleChartAnnotation;
            exportTo=singleChart.exportTo();
            extendFrom=singleChart.extendFrom();
            processor.preParseChart(extendFrom);
            head=processor.parseSingleChart(object);
        }
        else if(duplexChartAnnotation!=null){
            DuplexChart duplexChart= (DuplexChart) duplexChartAnnotation;
            exportTo=duplexChart.exportTo();
            extendFrom=duplexChart.extendFrom();
            processor.preParseChart(extendFrom);
            head=processor.parseDuplexChart(object);
        }
        if(exportTo!=null){
            processor.exportToFile(exportTo);
        }
        return head;
    }
    public void exportToFile(String exportTo){
        String json=JSON.toJSONString(head);
        try {
            FileWriter fileWriter=new FileWriter(exportTo);
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void preParseChart(String extendFrom){
        if(!"".equals(extendFrom)){
            try {
                head= parseFile(new File(extendFrom));
                series = (List<Map<String, Object>>) head.get("series");
                dataZoom = (List<Map<String, Object>>) head.get("dataZoom");
                visualMap = (List<Map<String, Object>>) head.get("visualMap");
                if(series!=null)head.remove("series");
                if(dataZoom!=null)head.remove("dataZoom");
                if(visualMap!=null)head.remove("visualMap");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
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
    
    private Map<String,Object> parseSingleChart(@NotNull Object object){
        head= parse(head,object);
        head.put("series",turnMapIntoList((Map<String, Object>) head.get("series")));
        head.put("dataZoom",turnMapIntoList((Map<String, Object>) head.get("dataZoom")));
        head.put("visualMap",turnMapIntoList((Map<String, Object>) head.get("visualMap")));
        postParseChart();
        return head;
    }

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
                    System.out.println(JSON.toJSONString(hashMap));
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

    public static Map<String,Object> getElementByKeyFromTree(Map<String,Object> tree, String[] keys){
        Map<String,Object> current=tree;
        for(String key:keys){
            if(current==null)return null;
            current= (Map<String, Object>) current.get(key);
        }
        return current;
    }

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
    public static boolean checkClass(Annotation annotation){
        String canonical=annotation.annotationType().getCanonicalName();
        return canonical.startsWith("cn.edu.gdut.zaoying.Option.");
    }
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
    void addAnnotationToMap(Map<String,Object> head, Annotation annotation, Object value){
        String name=annotation.annotationType().getCanonicalName();
        String keys[]=name.substring(27).split("\\.");
        Map<String,Object> current = head;
        for (int i=0;i<keys.length-1;i++){
            String key=keys[i];
            if(current.containsKey(key)){
                Object object=current.get(key);
                current= (Map<String, Object>)object;
            }
            else {
                Map<String,Object>newMap= new HashMap<>();
                current.put(key,newMap);
                current=newMap;
            }
        }
        String key=keys[keys.length-1];
        Object fit=value;
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
                fit="#"+Integer.toHexString(Integer.parseInt((String) value));
                key=key.substring(0,key.length()-3);
                break;
        }
        current.put(Util.toFirstLetterLowerCase(key),fit);
    }

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
