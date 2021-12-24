package com.jether.nrmtuwaaye;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import android.view.WindowManager;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);



        //start login activity after 2secs

        Runnable postDelayed;
        new Handler().postDelayed(new Runnable(){

            @Override
            public void run() {




                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();




            }
        },6000);
    }


}
