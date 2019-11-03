package com.matrixsoft.pison.pa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutMeActivity extends AppCompatActivity {

    TextView aboutMe;
    final private String aboutCreater = "\nCreate by MatriX \nhttps://vk.com/id201341485";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        Intent intent = getIntent();

        aboutMe = (TextView) findViewById(R.id.aboutMe);
        aboutMe.setText(intent.getStringExtra("text") + aboutCreater);
    }
}
