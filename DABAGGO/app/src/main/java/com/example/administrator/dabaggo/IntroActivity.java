package com.example.administrator.dabaggo;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);


        setContentView(R.layout.activity_intro);


        getSupportActionBar().hide();
        ImageView iv_splash = (ImageView) findViewById(R.id.iv_splash);
        final AnimationDrawable animation = (AnimationDrawable) iv_splash.getBackground();

        animation.start();
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(new Intent(IntroActivity.this,MainActivity.class));
                finish();

            }
        },1680);
        /*Intent intent = new Intent(this, IntroActivity.class);




        animation.start();
        startActivity(intent);
        finish();*/


    }
}
