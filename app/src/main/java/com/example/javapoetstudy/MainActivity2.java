package com.example.javapoetstudy;

import android.os.Bundle;

import com.example.arouter_annotations.ARouter;

import androidx.appcompat.app.AppCompatActivity;

@ARouter(path = "/app/MainActivity2")
public class MainActivity2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}