package cn.edu.gdut.zaoying.Charts;

import cn.edu.gdut.zaoying.AddSeries;
import cn.edu.gdut.zaoying.DuplexChart;
import cn.edu.gdut.zaoying.Option.BackgroundColorHex;
import cn.edu.gdut.zaoying.Option.title.TextString;

/**
 * Created by huang on 2016/2/26 0026.
 */
@DuplexChart
public class CombinedChart {
    @TextString
    String title;
    @BackgroundColorHex(value = 0xfff)
    int backgroundColor;
    @AddSeries
    LineChart lineChart;
    @AddSeries
    BarChart barChart;

    public CombinedChart(String title) {
        this.title = title;
        lineChart = new LineChart();
        lineChart.setName("折线图");
        lineChart.setData(new double[]{1,2,3,4});
        barChart = new BarChart();
        barChart.setName("条形图");
        barChart.setData(new double[]{5,6,7,8});
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
