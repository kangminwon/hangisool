package com.hangisool.aircleaner_2;

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

public class StartActivity extends AppCompatActivity {
    EditText etId, etpw;
    Button btnLogin, btnJoinPage;
    String sId, sPw;
    AlertDialog.Builder alertBuilder;
    SharedPreferences auto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        etId = (EditText) findViewById(R.id.et_login_Id);
        etpw = (EditText) findViewById(R.id.et_login_Password);
        btnLogin = (Button) findViewById(R.id.bt_login);
        btnJoinPage = (Button) findViewById(R.id.bt_joinpage);
        alertBuilder = new AlertDialog.Builder(this);

        auto = getSharedPreferences("auto_login", Activity.MODE_PRIVATE);
        //처음에는 SharedPreferences에 아무런 정보도 없으므로 값을 저장할 키들을 생성한다.
        // getString의 첫 번째 인자는 저장될 키, 두 번쨰 인자는 값입니다.
        // 첨엔 값이 없으므로 키값은 원하는 것으로 하시고 값을 null을 줍니다.
        sId = auto.getString("inputId",null);
        sPw = auto.getString("inputPwd",null);


        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(sId !=null && sPw != null) {
                    AutologinDB autoLogin = new AutologinDB();
                    autoLogin.execute();
                }
                else{
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        th.start();
    }

    public class AutologinDB extends AsyncTask<Void, Integer, Void> {
        String data = "";
        @Override
        protected Void doInBackground(Void... unused) {

            /* 인풋 파라메터값 생성 */
            String param = "u_id=" + sId + "&u+pw=" + sPw + "";
            Log.e("POST", param);
            try {
                /* 서버연결 */
                URL url = new URL(
                        Address.serverIP+Address.addrLogin);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                /* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();

                /* 서버에서 응답 */
                Log.e("RECV DATA", data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(data.equals("1"))
            {
                Log.e("RESULT","자동 로그인 성공");
                Toast.makeText(StartActivity.this, "Welcome " +sId, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("user_id",sId);//main activity로 유저 아이디 전달
                startActivity(intent);
                finish();
            }
            else if(data.equals("0"))
            {
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
                alertBuilder
                        .setTitle("Notice")
                        .setMessage("Password not correct.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //finish();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else if(data.equals("100")){//일치하는 아이디가 없을 경우
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
                alertBuilder
                        .setTitle("Notice")
                        .setMessage("ID Not found.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //finish();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Log.e("RESULT","에러 발생! ERRCODE = " + data);
                alertBuilder
                        .setTitle("Notice")
                        .setMessage("ERROR! errcode : "+ data)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //finish();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    void SaveUserInfo(){
        SharedPreferences auto = getSharedPreferences("auto_login", Activity.MODE_PRIVATE);
        //auto_login의 loginId와 loginPwd에 값을 저장해 줍니다.
        SharedPreferences.Editor autoLogin = auto.edit();
        autoLogin.putString("inputId", etId.getText().toString().trim());
        autoLogin.putString("inputPwd", etpw.getText().toString().trim());
        //꼭 commit()을 해줘야 값이 저장
        autoLogin.commit();
    }
}
