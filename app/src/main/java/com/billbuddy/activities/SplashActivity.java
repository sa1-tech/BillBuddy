package com.billbuddy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.billbuddy.R;

public class SplashActivity extends AppCompatActivity {

	private static final int SPLASH_DISPLAY_LENGTH = 1000; // 2 seconds

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		// Delayed launch of MainActivity
		new Handler().postDelayed(() -> {
			startActivity(new Intent(SplashActivity.this, MainActivity.class));
			finish();
		}, SPLASH_DISPLAY_LENGTH);
	}
}
