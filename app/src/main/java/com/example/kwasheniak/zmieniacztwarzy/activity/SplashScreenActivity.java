package com.example.kwasheniak.zmieniacztwarzy.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.kwasheniak.zmieniacztwarzy.R;

public class SplashScreenActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 1400;
    Animation animation;
    ImageView image, image2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        animation = AnimationUtils.loadAnimation(this, R.anim.splash_top_animation);
        image = findViewById(R.id.splash_screen_image_view2);
        image2 = findViewById(R.id.splash_screen_image_view);

        image2.setVisibility(View.GONE);
        image.setAnimation(animation);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                image2.setVisibility(View.VISIBLE);
            }
        }, 800);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                image2.setVisibility(View.GONE);
            }
        }, 1000);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
