package com.mayank.rucky.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.mayank.rucky.R;
import com.mayank.rucky.utils.Config;
import com.mayank.rucky.utils.Constants;

public class WelcomeActivity extends AppCompatActivity {

    private Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        config = new Config(this);
        if (config.getDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        setTheme(Constants.themeList[config.getAccentTheme()]);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = getWindow().getDecorView();
        view.setSystemUiVisibility(flags);

        SplashScreen.installSplashScreen(this);

        if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.R)
            splash();
        else
            launchNext();
    }

    void launchNext() {
        Intent intent = new Intent(WelcomeActivity.this, config.getInitState() ? EditorActivity.class : InitActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }

    private void splash() {
        setContentView(R.layout.activity_welcome);
        RelativeLayout parentLayout = findViewById(R.id.splashParentLayout);
        parentLayout.setBackground(ContextCompat.getDrawable(this, Constants.themeSplashBorder[config.getAccentTheme()]));
        ImageView i1 = findViewById(R.id.imageViewBG);
        FrameLayout l2= findViewById(R.id.splashTextView);
        i1.setAnimation(AnimationUtils.loadAnimation(this,R.anim.rotate));
        l2.setAnimation(AnimationUtils.loadAnimation(this,R.anim.downtoup));
        new Handler().postDelayed(this::launchNext, 2000);
    }
}