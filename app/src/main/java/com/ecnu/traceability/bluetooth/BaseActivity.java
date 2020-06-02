package com.ecnu.traceability.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    protected <T extends Activity> void startActivity(Class<T> clazz) {
        startActivity(new Intent(this, clazz));
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}