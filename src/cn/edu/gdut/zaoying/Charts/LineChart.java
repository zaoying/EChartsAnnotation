package cn.edu.gdut.zaoying.Charts;

import cn.edu.gdut.zaoying.Option.title.TextString;
import cn.edu.gdut.zaoying.SingleChart;
import cn.edu.gdut.zaoying.Option.BackgroundColorHex;
import cn.edu.gdut.zaoying.Option.series.line.DataArray;
import cn.edu.gdut.zaoying.Option.series.line.NameString;


/**
 * Created by 祖荣 on 2016/2/7 0007.
 */
@SingleChart(exportTo = "lineChart")
public class LineChart {
    @NameString
    String name;
    @DataArray
    double[] data;

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
