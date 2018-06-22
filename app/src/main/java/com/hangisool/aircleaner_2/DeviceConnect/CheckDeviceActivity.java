package com.hangisool.aircleaner_2.DeviceConnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hangisool.aircleaner_2.MainActivity;
import com.hangisool.aircleaner_2.R;
import com.hangisool.aircleaner_2.Util.Address;
import com.hangisool.aircleaner_2.login.LoginActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by L G on 2018-03-14.
 */

public class CheckDeviceActivity extends AppCompatActivity {
    Button btnOK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_device);
        btnOK = (Button)findViewById(R.id.btn_device_check);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckDeviceActivity.this, WifiSelectActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
