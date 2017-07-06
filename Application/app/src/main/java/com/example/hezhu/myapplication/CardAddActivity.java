package com.example.hezhu.myapplication;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class CardAddActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    @OnClick({R.id.btnIDCard, R.id.btnDriverLicense, R.id.btnStudentCard, R.id.btnDisabledCard})
    void onSelected(View view) {
        Intent intent = new Intent(this, CardAddDetailActivity.class);
        intent.putExtra("type", Integer.parseInt((String)view.getTag()));
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_add);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("添加证件");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
