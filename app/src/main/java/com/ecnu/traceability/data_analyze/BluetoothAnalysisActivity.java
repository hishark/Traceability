package com.ecnu.traceability.data_analyze;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AAChartModel;
import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AAChartView;
import com.ecnu.traceability.AAChartCoreLib.AAChartCreator.AASeriesElement;
import com.ecnu.traceability.AAChartCoreLib.AAChartEnum.AAChartType;
import com.ecnu.traceability.R;

public class BluetoothAnalysisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_analysis);
        AAChartView aaChartView = findViewById(R.id.AAChartView_bluetooth);
        AAChartModel aaChartModel = new AAChartModel()
                .chartType(AAChartType.Area)
                .title("14天接触人数统计")
                .subtitle("")
                .backgroundColor("#ffffff")//#4b2b7f
                .categories(new String[]{"第1天", "第2天", "第3天", "第4天", "第5天", "第6天", "第7天", "第8天", "第9天",
                        "第10天", "第11天", "第12天", "第13天", "第14天"})
                .dataLabelsEnabled(false)
                .yAxisGridLineWidth(0f)
                .series(new AASeriesElement[]{
                        new AASeriesElement()
                                .name("人数")
                                .data(new Object[]{5, 7, 9, 14, 18,
                                21, 25, 26, 23, 18,
                                13, 9, 26, 23}),
                });
        //The chart view object calls the instance object of AAChartModel and draws the final graphic
        aaChartView.aa_drawChartWithChartModel(aaChartModel);
    }


}