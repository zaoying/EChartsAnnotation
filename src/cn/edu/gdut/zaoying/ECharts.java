package cn.edu.gdut.zaoying;

import cn.edu.gdut.zaoying.Charts.CombinedChart;
import com.alibaba.fastjson.JSON;

public class ECharts {
    public static void main(String[] args) {
//        LineChart lineChart=new LineChart();
//        lineChart.setName("线性表一");
//        lineChart.setData(new double[]{1,2,3,4});
//        Object json=EChartsAnnotationProcessor.parseChart(lineChart);
//        System.out.print(JSON.toJSONString(json));
        Object json=EChartsAnnotationProcessor.parseChart(new CombinedChart("组合图表"));
        System.out.print(JSON.toJSONString(json));
    }
}
