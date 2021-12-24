package com.jether.nrmtuwaaye;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.cuberto.liquid_swipe.LiquidPager;

public class OnBoardActivity  extends AppCompatActivity {

    LiquidPager pager;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);

        pager=findViewById(R.id.pager);
        viewPager= new ViewPager(getSupportFragmentManager(),1);
        pager.setAdapter(viewPager);


    }
}
