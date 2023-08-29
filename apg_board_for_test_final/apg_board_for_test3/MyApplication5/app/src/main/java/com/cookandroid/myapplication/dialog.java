package com.cookandroid.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class dialog extends AppCompatActivity {

    private android.widget.Button Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);

        Button = findViewById(R.id.Button);
        int pinkColor = ContextCompat.getColor(this, R.color.pink);
        Button.setBackgroundColor(pinkColor);

        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(dialog.this, function_main.class);
                startActivity(intent);
            }
        });
    }
}
