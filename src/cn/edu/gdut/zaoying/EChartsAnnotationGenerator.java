package cn.edu.gdut.zaoying;

import cn.edu.gdut.zaoying.bean.Array;
import cn.edu.gdut.zaoying.bean.Option;
import cn.edu.gdut.zaoying.bean.Single;
import com.alibaba.fastjson.JSON;

import java.io.*;
import java.util.Map;
import java.util.Set;

import static cn.edu.gdut.zaoying.Util.toFirstLetterUpperCase;

/**
 * Created by huang on 2016/3/3 0003.
 */

/**
 * 注解生成器
 */
public class EChartsAnnotationGenerator {
    File current;
    int count=0;
    public EChartsAnnotationGenerator() {
        current=new File("src/cn/edu/gdut/zaoying/Option/");
    }

    /**
     * 生成注解
     */
    public void generateAnnotation(){
        generateAnnotation4Option();
        generateAnnotation4Array();
        System.out.println("共生成"+count+"个注解");
    }

    /**
     * 读取json/array.json并生成注解
     */
    void generateAnnotation4Array(){
        StringBuilder stringBuilder=new StringBuilder(2*1024*1024);
        try {
            BufferedReader br=new BufferedReader(new FileReader("json/array.json"));
            int i=br.read();
            while (i!=-1){
                stringBuilder.append((char)i);
                i=br.read();
            }
            br.close();
            Array array= JSON.parseObject(stringBuilder.toString(),Array.class);
            Single[] anyOf=array.series.items.anyOf;
            current=new File("src/cn/edu/gdut/zaoying/Option/series");
            for (Single single:anyOf){
                Map<String,Single> properties=single.getProperties();
                String type=properties.get("type").getDefaultValue();
                type=type.substring(1,type.length()-1);
                traversalTree(single,type);
            }
            anyOf=array.dataZoom.items.anyOf;
            current=new File("src/cn/edu/gdut/zaoying/Option/dataZoom");
            for (Single single:anyOf){
                Map<String,Single> properties=single.getProperties();
                String type=properties.get("type").getDefaultValue();
                type=type.substring(1,type.length()-1);
                traversalTree(single,type);
            }
            anyOf=array.visualMap.items.anyOf;
            current=new File("src/cn/edu/gdut/zaoying/Option/visualMap");
            for (Single single:anyOf){
                Map<String,Single> properties=single.getProperties();
                String type=properties.get("type").getDefaultValue();
                type=type.substring(0,type.length()-1);
                traversalTree(single,type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取json/option.json并生成注解
     */
    void generateAnnotation4Option(){
        StringBuilder stringBuilder=new StringBuilder(1024*1024);
        try {
            BufferedReader br=new BufferedReader(new FileReader("json/option.json"));
            int i=br.read();
            while (i!=-1){
                stringBuilder.append((char)i);
                i=br.read();
            }
            br.close();
            Option option=JSON.parseObject(stringBuilder.toString(),Option.class);
            traversalTree(option.getOption(),null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 递归遍历树并生成注解
     * @param single
     * @param name
     */
    void traversalTree(Single single, String name){
        Map<String,Single> properties=single.getProperties();
        if(properties!=null&&!properties.isEmpty()){
            try {
                if(name!=null)current=createFolder(name+"/");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Set<String> keySet=properties.keySet();
            for(String key:keySet){
                traversalTree(properties.get(key),key);
            }
            current=current.getParentFile();
        }
        else {
            String singleType="package &;\n" +
                    "\n" +
                    "import java.lang.annotation.ElementType;\n" +
                    "import java.lang.annotation.Retention;\n" +
                    "import java.lang.annotation.RetentionPolicy;\n" +
                    "import java.lang.annotation.Target;\n"+
                    "@Retention(RetentionPolicy.RUNTIME)\n"+
                    "@Target(ElementType.FIELD)\n" +
                    "public @interface $ {\n" +
                    "    # value() default *;\n" +
                    "}";
            String multiType="package &;\n" +
                    "\n" +
                    "import java.lang.annotation.ElementType;\n" +
                    "import java.lang.annotation.Retention;\n" +
                    "import java.lang.annotation.RetentionPolicy;\n" +
                    "import java.lang.annotation.Target;\n"+
                    "@Retention(RetentionPolicy.RUNTIME)\n" +
                    "@Target(ElementType.FIELD)\n" +
                    "public @interface $ {\n" +
                    "}";
            String content;
            String[] types=single.getType();
            for(String type:types){
                String fileName=toFirstLetterUpperCase(name);
                switch (type){
                    case "boolean":
                        content=singleType.replace("$",fileName+="Boolean");
                        content=content.replace("#","boolean");
                        content=content.replace("*","true");
                        break;
                    case "Color":
                        content=singleType.replace("$",fileName+="Hex");
                        content=content.replace("#","int");
                        content=content.replace("*","0");
                        break;
                    case "number":
                        content=singleType.replace("$",fileName+="Number");
                        content=content.replace("#","double");
                        content=content.replace("*","0");
                        break;
                    case "string":
                        content=singleType.replace("$",fileName+="String");
                        content=content.replace("#","String");
                        content=content.replace("*","\"\"");
                        break;
                    case "Array":
                        content=multiType.replace("$",fileName+="Array");
                        break;
                    case "Function":
                        content=multiType.replace("$",fileName+="Function");
                        break;
                    case "*":
                        content=multiType.replace("$",fileName+="All");
                        break;
                    default:
                        System.out.println("未知类型："+current.getPath()+"\\"+fileName+":"+type);
                        continue;
                        //content=multiType.replace("$",fileName+=type);
                }
                try {
                    File file=createFile(fileName+".java");
                    FileWriter fileWriter=new FileWriter(file);
                    String packageName=current.getPath().substring(4).replace("\\",".");
                    //System.out.println(packageName);
                    content=content.replace("&",packageName);
                    fileWriter.write(content);
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 创建文件夹
     * @param fileName 文件夹路径
     * @return File
     * @throws IOException
     */
    File createFolder(String fileName) throws IOException {
        File newFile=new File(current.getPath()+"/"+fileName);
        //System.out.println(newFile.getPath());
        newFile.mkdir();
        return newFile;
    }

    /**
     * 创建文件
     * @param fileName 文件夹路径
     * @return File
     * @throws IOException
     */
    File createFile(String fileName) throws IOException {
        File newFile=new File(current.getPath()+"/"+fileName);
        //System.out.println(newFile.getPath());
        newFile.createNewFile();
        count++;
        return newFile;
    }
}
