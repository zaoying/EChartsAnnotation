package cn.edu.gdut.zaoying.Charts;

import cn.edu.gdut.zaoying.Option.series.line.DataArray;
import cn.edu.gdut.zaoying.Option.series.line.NameString;
import cn.edu.gdut.zaoying.SingleChart;

/**
 * Created by huang on 2016/2/28 0028.
 */
@SingleChart
public class Line2Chart {
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
