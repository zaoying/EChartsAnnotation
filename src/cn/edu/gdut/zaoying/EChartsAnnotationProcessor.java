package cn.edu.gdut.zaoying;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by 祖荣 on 2016/2/9 0009.
 */
public class EChartsAnnotationProcessor {
    public enum Type{BOOLEAN,COLOR,NUMBER,ARRAY,STRING,FUNCTION,ALL}
    static HashMap<String,Object> phraseSingleChart(Object object){
        HashMap<String,Object> head=phrase(object);
        head.put("series",turnMapIntoList((HashMap<String, Object>) (head.get("series")),new LinkedList<HashMap<String, Object>>()));
        head.put("series",turnMapIntoList((HashMap<String, Object>) (head.get("dataZoom")),new LinkedList<HashMap<String, Object>>()));
        head.put("series",turnMapIntoList((HashMap<String, Object>) (head.get("visualMap")),new LinkedList<HashMap<String, Object>>()));
        return head;
    }
    static HashMap<String,Object> phraseDuplexChart(Object object){
        HashMap<String,Object> head=phrase(object);

        Class clazz=object.getClass();
        Annotation annotation=clazz.getAnnotation(DuplexChart.class);
        if(annotation!=null){
            DuplexChart duplexChart= (DuplexChart) annotation;
            System.out.println(duplexChart.exportTo());
            List<HashMap<String,Object>> series = null;
            List<HashMap<String,Object>> dataZoom = null;
            List<HashMap<String,Object>> visualMap = null;
            Field[] fields=clazz.getDeclaredFields();
            for(Field field:fields)
            {
                Object value=getFieldValue(field,object);
                Annotation addSeries=field.getAnnotation(AddSeries.class);
                if(addSeries!=null){
                    HashMap<String,Object> hashMap=getElementByKeyFromTree(phrase(value),new String[]{"series"});
                    if(series==null)series=new LinkedList<>();
                    turnMapIntoList(hashMap,series);
                    continue;
                }
                Annotation AddDataZoom=field.getAnnotation(AddDataZoom.class);
                if(AddDataZoom!=null){
                    HashMap<String,Object> hashMap=getElementByKeyFromTree(phrase(value),new String[]{"dataZoom"});
                    if(dataZoom==null)dataZoom=new LinkedList<>();
                    turnMapIntoList(hashMap,dataZoom);
                    continue;
                }
                Annotation AddVisualMap=field.getAnnotation(AddVisualMap.class);
                if(AddVisualMap!=null){
                    HashMap<String,Object> hashMap=getElementByKeyFromTree(phrase(value),new String[]{"visualMap"});
                    if(visualMap==null)visualMap=new LinkedList<>();
                    turnMapIntoList(hashMap,visualMap);
                }
            }
            head.put("series",series);
            head.put("dataZoom",dataZoom);
            head.put("visualMap",visualMap);
        }
        return head;
    }
    private static List<HashMap<String,Object>> turnMapIntoList(HashMap<String,Object>map,List<HashMap<String,Object>> list){
        if(map==null)return null;
        Set<String> keys=map.keySet();
        for(String key:keys){
            HashMap<String,Object> element= (HashMap<String, Object>) map.get(key);
            element.put("type",Util.toFirstLetterLowerCase(key));
            list.add(element);
        }
        return list;
    }
    static HashMap<String,Object> getElementByKeyFromTree(HashMap<String,Object> tree, String[] keys){
        HashMap<String,Object> current=tree;
        for(String key:keys){
            if(current==null)return null;
            current= (HashMap<String, Object>) current.get(key);
        }
        return current;
    }
    static HashMap<String,Object> phrase(Object object)throws NullPointerException{
        if(object==null)throw new NullPointerException();
        HashMap<String,Object> head=new HashMap<>();
        Class clazz=object.getClass();
        Annotation singleChart=clazz.getAnnotation(SingleChart.class);
        Annotation duplexChart=clazz.getAnnotation(DuplexChart.class);
        if(singleChart!=null||duplexChart!=null){
            String exportTo=null;
            if(singleChart!=null)exportTo=((SingleChart)singleChart).exportTo();
            else exportTo=((DuplexChart)duplexChart).exportTo();
            System.out.println(exportTo);
            Field[] fields=clazz.getDeclaredFields();
            for(Field field:fields)
            {
                Object value=getFieldValue(field,object);
                Annotation[] annotations=field.getDeclaredAnnotations();
                for(Annotation an:annotations){
                    if(!checkClass(an))continue;
                    if(value==null)addAnnotationToHashMap(head,an,getAnnotationValue(an));
                    else addAnnotationToHashMap(head,an,value);
                }
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
    static String getAnnotationValue(Annotation annotation){
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
    static void addAnnotationToHashMap(HashMap<String,Object> head,Annotation annotation,Object value){
        String name=annotation.annotationType().getCanonicalName();
        String keys[]=name.substring(27).split("\\.");
        HashMap<String,Object> current = head;
        for (int i=0;i<keys.length-1;i++){
            String key=keys[i];
            if(current.containsKey(key)){
                Object object=current.get(key);
                current= (HashMap<String, Object>)object;
            }
            else {
                HashMap<String,Object>newMap=new HashMap<String,Object>();
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
