package com.cookandroid.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class statistics_apg_day extends AppCompatActivity {

    private LineChart chart;
    private Button ok;
    private List<Float> averageList;
    private List<String> entries;
    private List<Entry> dataSetEntries;
    private LineDataSet dataSet;
    private LineData lineData;
    private TextView average1;

    private static final String SHARED_PREFS_KEY = "APG_STATS";
    private static final String AVERAGE_LIST_KEY = "averageList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_apg_day);

        chart = findViewById(R.id.Chart1);
        ok = findViewById(R.id.ok);
        average1 = findViewById(R.id.average1);

        int pinkColor = ContextCompat.getColor(this, R.color.pink);
        ok.setBackgroundColor(pinkColor);

        averageList = (List<Float>) getIntent().getSerializableExtra("averageList");

        entries = new ArrayList<>();
        for (int i = 0; i < averageList.size(); i++) {
            String formattedValue = String.format("%.2f", averageList.get(i));
            entries.add(formattedValue);
        }

        dataSetEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            float value = Float.parseFloat(entries.get(i));
            String formattedValue = String.format("%.2f", value);
            dataSetEntries.add(new Entry(i+1, Float.parseFloat(formattedValue)));
        }


        dataSet = new LineDataSet(dataSetEntries, "Average List");
        dataSet.setDrawValues(true);
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f", value);
            }
        });
        lineData = new LineData(dataSet);

        chart.setData(lineData);

        setupChart();

        float totalSum = 0;
        for (Float value : averageList) {
            totalSum += value;
        }
        float average = totalSum / averageList.size();
        String formattedAverage = String.format("%.2f", average);
        average1.setText(formattedAverage);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(statistics_apg_day.this, function_main.class);
                intent.putExtra("averageList", (ArrayList<Float>) averageList);
                startActivity(intent);
                finish();
            }
        });
    }


    private void setupChart() {
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        int visibleRange = 10;
        chart.setVisibleXRangeMaximum(visibleRange+1);
        chart.getXAxis().setGranularity(1f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        float minValue = -1.56f;
        float maxValue = -0.52f;
        YAxis leftYAxis = chart.getAxisLeft();
        leftYAxis.setAxisMinimum(minValue);
        leftYAxis.setAxisMaximum(maxValue);
        leftYAxis.setDrawGridLines(true);

        YAxis rightYAxis = chart.getAxisRight();
        rightYAxis.setEnabled(false);

        chart.invalidate();
    }

    private void addDataPoint(float newDataPoint) {
        int newIndex = entries.size();
        String formattedValue = String.format("%.2f", newDataPoint);
        entries.add(formattedValue);
        dataSet.notifyDataSetChanged();
        lineData.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();

        averageList.add(newDataPoint);
        saveAverageListToSharedPreferences(averageList);
    }

    private void saveAverageListToSharedPreferences(List<Float> dataList) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(AVERAGE_LIST_KEY + "_size", dataList.size());
        for (int i = 0; i < dataList.size(); i++) {
            editor.putFloat(AVERAGE_LIST_KEY + "_" + i, dataList.get(i));
        }
        editor.apply();
    }

    private List<Float> getAverageListFromSharedPreferences() {
        List<Float> dataList = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        int dataSize = sharedPreferences.getInt(AVERAGE_LIST_KEY + "_size", 0);
        for (int i = 0; i < dataSize; i++) {
            dataList.add(sharedPreferences.getFloat(AVERAGE_LIST_KEY + "_" + i, 0));
        }
        return dataList;
    }
}