package cn.edu.gdut.zaoying.Charts;

import cn.edu.gdut.zaoying.Option.series.line.DataArray;
import cn.edu.gdut.zaoying.Option.series.line.NameString;
import cn.edu.gdut.zaoying.Option.xAxis.TypeString;
import cn.edu.gdut.zaoying.SingleChart;


/**
 * Created by 祖荣 on 2016/2/7 0007.
 */
@SingleChart(exportTo = "templates/lineChart.json")
public class LineChart {
    @NameString
    String name;
    @DataArray
    double[] data;
    @cn.edu.gdut.zaoying.Option.xAxis.NameString("横轴")
    String xAxisName;
    @TypeString("category")
    String type;
    @cn.edu.gdut.zaoying.Option.xAxis.DataArray
    int[] xAxisData=new int[]{1,2,3,4};
    @cn.edu.gdut.zaoying.Option.yAxis.NameString("纵轴")
    String yAxisName;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }

}
