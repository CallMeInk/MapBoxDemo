package com.example.yukai.mapboxdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by yukai on 2017/12/28.
 */

public class EntranceActivity extends Activity implements View.OnClickListener{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrance_layout);
        (findViewById(R.id.basic_func_btn)).setOnClickListener(this);
        (findViewById(R.id.poi_func_btn)).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.basic_func_btn){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.poi_func_btn){
            Intent intent = new Intent(this, PoiActivity.class);
            startActivity(intent);
        }
    }
}
