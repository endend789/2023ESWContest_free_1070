package com.cookandroid.myapplication;

import static com.cookandroid.myapplication.MainActivity.MESSAGE_READ;
import static com.cookandroid.myapplication.MainActivity.connectedThread;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class home extends AppCompatActivity {
    private static final String TAG = "home";
    private TextView textViewData;
    private Handler handler;
    private TextView textview3;
    private TextView text;
    private LineChart mChart;
    private Thread thread;
    private boolean plotData = true;
    private float latestPpgValue = 0;
    private Button startButton;
    private Button saveButton;
    private boolean isDataReceiving = false;
    private boolean isAutoStopEnabled = false;

    private List<Float> averageList;
    private DatabaseHelper dbHelper;
    private TextView textViewResults;
    private Button resetButton;

    private static final String PREF_NAME = "MyPrefs";
    private static final String PREF_KEY_AVERAGE_LIST = "AverageList";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        averageList = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);

        loadAverageListFromSharedPreferences();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewData = findViewById(R.id.apg);
        text = findViewById(R.id.text);

        saveButton = findViewById(R.id.saveButton);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);

        View progressBar1 = findViewById(R.id.progressBar1);

        int pinkColor = ContextCompat.getColor(this, R.color.pink);
        saveButton.setBackgroundColor(pinkColor);
        startButton.setBackgroundColor(pinkColor);
        resetButton.setBackgroundColor(pinkColor);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString();
                        float receivedPpgValue = Float.parseFloat(arduinoMsg);
                        switch ((int)receivedPpgValue) {
                            case 1111111:
                                text.setText("혈관 탄성도 측정 완료");
                                textViewData.setText(arduinoMsg);
                                textViewData.setVisibility(View.VISIBLE);
                                progressBar1.setVisibility(View.INVISIBLE);
                                textview3.setVisibility(View.VISIBLE);
                                String cmdText=null;
                                cmdText = "0";
                                connectedThread.write(cmdText);
                                break;
                            case 2222222:
                                text.setText("재측정이 필요합니다.");
                                textViewData.setVisibility(View.INVISIBLE);
                                progressBar1.setVisibility(View.INVISIBLE);
                                cmdText = "0";
                                connectedThread.write(cmdText);
                                break;
                            case 3333333:
                                textViewData.setVisibility(View.INVISIBLE);
                                progressBar1.setVisibility(View.VISIBLE);
                                textview3.setVisibility(View.INVISIBLE);
                                text.setText("혈관 탄성도 측정중");
                                cmdText = "0";
                                connectedThread.write(cmdText);
                                break;
                        }
                        textViewData.setText(arduinoMsg);
                        String dataValue= (String) textViewData.getText();
                        float Value=Float.parseFloat(dataValue);
                        if(Value<=-1.25 && Value>-1.56)
                            textview3.setText("20대");
                        else if(Value<=-1.03 && Value>-1.25)
                            textview3.setText("30대");
                        else if(Value<=-0.84 && Value>-1.03)
                            textview3.setText("40대");
                        else if(Value<=-0.52 && Value>-0.84)
                            textview3.setText("50대");
                        else
                            textview3.setText("주의 필요");
                        latestPpgValue = receivedPpgValue;
                        break;
                }
                if (plotData) {
                    addEntry(latestPpgValue);
                    plotData = false;
                }
            }
        };


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mmSocket != null && MainActivity.mmSocket.isConnected()) {
                    startButton.setText("재측정");
                    String cmdText = "1";
                    startDataReceiving();
                    connectedThread.write(cmdText);
                    textViewData.setVisibility(View.INVISIBLE);
                    progressBar1.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(home.this, "먼저 블루투스 연결을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.mmSocket == null || !MainActivity.mmSocket.isConnected()) {
                    Toast.makeText(home.this, "먼저 블루투스 연결을 해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (startButton.getText().toString().equals("측정 시작")) {
                    Toast.makeText(home.this, "측정 시작한 후 데이터 저장 가능합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String ppgValue = textViewData.getText().toString();
                float receivedPpgValue = Float.parseFloat(ppgValue);

                if (receivedPpgValue == 2222222) {
                    Toast.makeText(home.this, "재측정한 후 저장 가능합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentText = text.getText().toString();
                if (currentText.equals("혈관 탄성도 측정중")) {
                    Toast.makeText(home.this, "측정이 완료된 후 저장 가능합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                builder.setMessage("데이터를 저장하시겠습니까?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveDataToDatabase(receivedPpgValue);
                                long timestamp = System.currentTimeMillis();
                                saveTimestampToDatabase(timestamp);
                                averageList.add(receivedPpgValue);
                                saveAverageListToSharedPreferences();

                                Toast.makeText(home.this, "혈관 탄성수치: " + receivedPpgValue +" 저장 완료", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(home.this, statistics_apg_day.class);
                                intent.putExtra("averageList", new ArrayList<>(averageList));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
                builder.setMessage("지금까지 측정한 모든 데이터를 초기화 하시겠습니까?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetData();
                                Intent intent = new Intent("RESET_AVERAGE_LIST");
                                sendBroadcast(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        textViewResults = findViewById(R.id.apg);
        textview3 = findViewById(R.id.textView3);
        mChart = findViewById(R.id.Chart1);
        mChart.setDrawGridBackground(false);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setGridBackgroundColor(Color.WHITE);

        mChart.getDescription().setEnabled(false);
        Description des = mChart.getDescription();
        des.setEnabled(false);
        des.setText("Real-Time DATA");
        des.setTextSize(15f);
        des.setTextColor(Color.BLACK);

        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.RED);

        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(false);
        xl.setEnabled(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);
        feedMultiple();
    }

    private void saveTimestampToDatabase(long timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.DataEntry.COLUMN_NAME_DATE, timestamp);
        db.insert(DatabaseContract.DataEntry.TABLE_NAME, null, values);
    }

    private void resetData() {
        averageList.clear();
        saveAverageListToSharedPreferences();
        Toast.makeText(this, "모든 데이터가 초기화 되었습니다", Toast.LENGTH_SHORT).show();
    }

    private void startDataReceiving() {
        isDataReceiving = true;
        isAutoStopEnabled = true;
        plotData = true;
    }

    private void stopDataReceiving() {
        isDataReceiving = false;
        isAutoStopEnabled = false;
        plotData = false;
    }

    private void addEntry(float ppgValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LineData data = mChart.getData();
                if (data == null) {
                    data = new LineData();
                    mChart.setData(data);
                }
                ILineDataSet set = data.getDataSetByIndex(0);
                if (set == null) {
                    set = createSet();
                    if (set != null) {
                        data.addDataSet(set);
                    }
                }
                Entry entry = new Entry((float) set.getEntryCount(), ppgValue);
                set.addEntry(entry);
                data.notifyDataChanged();
                mChart.notifyDataSetChanged();
                mChart.moveViewToX(data.getEntryCount());
                int visibleRange = 50;
                if (data.getEntryCount() > visibleRange) {
                    mChart.setVisibleXRangeMaximum(visibleRange);
                } else {
                    mChart.setVisibleXRangeMaximum(data.getEntryCount());
                }
                mChart.invalidate();

                String formattedValue = String.format(Locale.US, "%.2f", ppgValue);
                textViewData.setText(formattedValue);
                latestPpgValue = ppgValue;
            }
        });
    }

    @SuppressLint("ResourceType")
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "APG Data");
        set.setLineWidth(2f);
        set.setDrawValues(false);
        set.setValueTextColor(Color.BLACK);
        set.setColor(Color.RED);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawCircles(false);
        set.setHighLightColor(Color.rgb(190, 190, 190));

        return set;
    }

    private void feedMultiple() {
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
            thread = null;
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if (isDataReceiving) {
                        plotData = true;
                    }
                    try {
                        long samplingDelay = 90;
                        Thread.sleep(samplingDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivity.handler = handler;
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("이전 화면으로 되돌아가시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        home.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void saveAverageListToSharedPreferences() {
        Gson gson = new Gson();
        String jsonAverageList = gson.toJson(averageList);

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_KEY_AVERAGE_LIST, jsonAverageList);
        editor.apply();
    }

    private void loadAverageListFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonAverageList = sharedPreferences.getString(PREF_KEY_AVERAGE_LIST, null);

        if (jsonAverageList != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Float>>() {}.getType();
            averageList = gson.fromJson(jsonAverageList, type);
        }
    }

    private void saveDataToDatabase(float ppgValue) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String timestamp = getCurrentTimestamp();
        values.put(DatabaseContract.DataEntry.COLUMN_NAME_DATA, ppgValue);
        values.put(DatabaseContract.DataEntry.COLUMN_NAME_DATE, timestamp);
        db.insert(DatabaseContract.DataEntry.TABLE_NAME, null, values);
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAverageListFromDatabase();
    }

    private void loadAverageListFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {DatabaseContract.DataEntry.COLUMN_NAME_DATA};
        Cursor cursor = null;

        try {
            cursor = db.query(
                    DatabaseContract.DataEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            averageList.clear();

            if (cursor != null) {
                int dataIndex = cursor.getColumnIndexOrThrow(DatabaseContract.DataEntry.COLUMN_NAME_DATA);

                while (cursor.moveToNext()) {
                    if (dataIndex != -1) {
                        float data = cursor.getFloat(dataIndex);
                        averageList.add(data);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }
}