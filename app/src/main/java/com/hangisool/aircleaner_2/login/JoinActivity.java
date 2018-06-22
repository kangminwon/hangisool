package com.hangisool.aircleaner_2.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.hangisool.aircleaner_2.R;
import com.hangisool.aircleaner_2.Util.Address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by L G on 2018-03-29.
 */

public class JoinActivity extends AppCompatActivity {

    EditText et_id, et_pw, et_pw_chk;
    String sId, sPw, sPw_chk;
    AlertDialog.Builder alertBuilder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        et_id = (EditText) findViewById(R.id.et_Id);
        et_pw = (EditText) findViewById(R.id.et_Password);
        et_pw_chk = (EditText) findViewById(R.id.et_Password_chk);
        alertBuilder = new AlertDialog.Builder(this);
    }

    public void bt_Join(View view) {
            /* 버튼을 눌렀을 때 동작하는 소스 */
        sId = et_id.getText().toString();
        sPw = et_pw.getText().toString();
        sPw_chk = et_pw_chk.getText().toString();

        if(sPw.equals(sPw_chk))
        {
        /* 패스워드 확인이 정상적으로 됨 */
            registDB rdb = new registDB();
            rdb.execute();
        }
        else
        {
        /* 패스워드 확인이 불일치 함 */

        }
    }
    public class registDB extends AsyncTask<Void, Integer, Void> {
        String data = "";
        @Override
        protected Void doInBackground(Void... unused) {

/* 인풋 파라메터값 생성 */
            String param = "u_id=" + sId + "&u_pw=" + sPw + "";
            try {
/* 서버연결 */
                URL url = new URL(
                        Address.serverIP+Address.addrJoin);
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
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.e("RECV DATA",data);


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
            if(data.equals("0"))
            {
                Log.e("RESULT","성공적으로 처리되었습니다!");
                alertBuilder
                        .setTitle("Success")
                        .setMessage("Successfully registered.!")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
            else if(data.equals("1004"))
            {
                Log.e("RESULT","중복된 아이디 입니다.");
                alertBuilder
                        .setTitle("Fail")
                        .setMessage("This ID already exists.")
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
            else
            {
                Log.e("RESULT","에러 발생! ERRCODE = " + data);
                alertBuilder
                        .setTitle("Notice")
                        .setMessage("An error occurred during registration! errcode : "+ data)
                        .setCancelable(false)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
        }
    }
}
