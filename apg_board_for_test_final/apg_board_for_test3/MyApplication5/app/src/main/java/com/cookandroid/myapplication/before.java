package com.cookandroid.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class before extends AppCompatActivity {

    private Button startButton;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.before);

        startButton = findViewById(R.id.startButton);
        int pinkColor = ContextCompat.getColor(this, R.color.pink);
        startButton.setBackgroundColor(pinkColor);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(before.this, home.class);
                startActivity(intent);
            }
        });
    }

        public void onBackPressed() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("이전 화면으로 되돌아가시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            before.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", null);
            AlertDialog alert = builder.create();
            alert.show();

    }
}
