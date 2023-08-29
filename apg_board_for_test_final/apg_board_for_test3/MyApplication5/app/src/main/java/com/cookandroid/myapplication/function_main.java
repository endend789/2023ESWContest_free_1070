package com.cookandroid.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class function_main extends AppCompatActivity {

    private ImageButton vascularButton;
    private ImageButton statisticsButton;
    private ImageButton explanationButton;
    private TextView bluetoothconnect;
    private boolean isBluetoothConnected = false;
    private ViewPager2 viewPager;
    private NoticePagerAdapter pagerAdapter;
    private List<Notice> notices = new ArrayList<>();
    private List<Float> averageList;

    private static final String PREF_NAME = "MyPrefs";
    private static final String PREF_KEY_AVERAGE_LIST = "AverageList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.function_main);

        averageList = loadAverageListFromSharedPreferences();

        vascularButton = findViewById(R.id.APGButton);
        statisticsButton = findViewById(R.id.statisticsButton);
        explanationButton = findViewById(R.id.explanationButton);
        bluetoothconnect = findViewById(R.id.bluetoothconnection);

        notices.add(new Notice(R.drawable.based, "Notice 1"));
        notices.add(new Notice(R.drawable.sssuubb, "Notice 2"));
        notices.add(new Notice(R.drawable.subb, "Notice 3"));

        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new NoticePagerAdapter(notices);
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateBluetoothConnectionStatus(isBluetoothConnected);
            }
        });

        viewPager.setCurrentItem(1);

        vascularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(function_main.this, before.class);
                startActivity(intent);
            }
        });

        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Float> averageArrayList = new ArrayList<>(averageList);
                Intent intent = new Intent(function_main.this, statistics_apg_day_sub.class);
                intent.putExtra("averageList", averageArrayList);
                startActivity(intent);
            }
        });

        explanationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(function_main.this, explanation.class);
                startActivity(intent);
            }
        });

        bluetoothconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(function_main.this, MainActivity.class);
                startActivity(intent);
                updateBluetoothConnectionStatus(isBluetoothConnected);
            }
        });
    }

    private void updateBluetoothConnectionStatus(boolean isConnected) {
        if (isConnected) {
            bluetoothconnect.setText("블루투스 연결");
        } else {
            bluetoothconnect.setText("블루투스 연결");
        }
    }

    public class Notice {
        private int imageResource;
        private String title;

        public Notice(int imageResource, String title) {
            this.imageResource = imageResource;
            this.title = title;
        }

        public int getImageResource() {
            return imageResource;
        }

        public String getTitle() {
            return title;
        }
    }

    private List<Float> loadAverageListFromSharedPreferences() {
        List<Float> loadedAverageList = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String jsonAverageList = sharedPreferences.getString(PREF_KEY_AVERAGE_LIST, null);

        if (jsonAverageList != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Float>>() {}.getType();
            loadedAverageList = gson.fromJson(jsonAverageList, type);
        }

        return loadedAverageList;
    }
}