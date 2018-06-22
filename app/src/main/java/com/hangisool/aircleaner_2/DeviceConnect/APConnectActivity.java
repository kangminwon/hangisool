package com.hangisool.aircleaner_2.DeviceConnect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hangisool.aircleaner_2.MainActivity;
import com.hangisool.aircleaner_2.R;
import com.hangisool.aircleaner_2.Util.Address;
import com.hangisool.aircleaner_2.setting.ReservationActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static com.hangisool.aircleaner_2.MainActivity.userID;

/**
 * Created by L G on 2018-03-21.
 */
//와이파이로 연결된 AP와 TCP 통신을 통해 AP가 연결해야할 와이파이 이름과 패스워드를 넘겨주는 액티비티
public class APConnectActivity extends AppCompatActivity {

    private Socket socket;
    private DataOutputStream writeSocket;
    private DataInputStream readSocket;
    private Handler mHandler = new Handler();

    private ConnectivityManager cManager;
    private NetworkInfo wifi;
    private ServerSocket serverSocket;

    private WifiManager wifiManager;
    private ConnectivityManager connManager;
    private int scanCount = 0;
    private List<ScanResult> scanResult;
    Button btnConnect;
    Button btnNext;
    EditText et_device_name;

    private String WIFI_ID;
    private String WIFI_PW;
    WifiConfiguration wfc;
    private static final String TAG = "TEST";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_connect);
        Intent intent = getIntent();
        WIFI_ID = intent.getExtras().getString("WIFI_ID");
        WIFI_PW = intent.getExtras().getString("WIFI_PW");

        wfc = new WifiConfiguration();// (WifiConfiguration는 전역변수,지역변수 알아서 상황에 맞게 조절한다)

        btnConnect = (Button)findViewById(R.id.btn_try_connect);
        btnNext = (Button)findViewById(R.id.btn_next);
        et_device_name = (EditText)findViewById(R.id.et_device_name);

        ConnectivityManager manager;
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);// 활성화 한지 와이파이 체크 위함
        manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);//와이파이 연결 체크

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentConfirm = new Intent();
                intentConfirm.setAction("android.settings.WIFI_SETTINGS");
                startActivity(intentConfirm);
                //connectToAP("HAN_AP","");
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APCommunicationProcess apCommunicationProcess = new APCommunicationProcess();
                apCommunicationProcess.start();
            }
        });
    }

    void connect(){
        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        wfc = new WifiConfiguration();
        wfc.SSID = "\"".concat("HAN_AP").concat("\"");
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 40;
        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wfc.allowedAuthAlgorithms.clear();
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        int networkId = wm.addNetwork(wfc);

        if (networkId != -1) {
            Log.e("test","true");
            wm.enableNetwork(networkId, true);
        }
    }

    Handler uiHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 100:
                    //tv4.append(msg.obj + "\n");
                    break;
            }
        }
    };
    //AP와 주고받을 데이터 통신 하는 쓰레드
    class APCommunicationProcess extends Thread{
        @Override
        public void run() {
            super.run();
            Connect connect = new Connect();
            connect.start();
        }
    }
    class Connect extends Thread {
        public void run() {
            String ip = null;
            int port = 0;
            try {
                ip = Address.addrAPIP;
                port = Address.addrAPPort;
                socket = new Socket(ip, port);
                writeSocket = new DataOutputStream(socket.getOutputStream());
                readSocket = new DataInputStream(socket.getInputStream());

                (new recvSocket()).start();

            } catch (Exception e) {
                final String recvInput = "연결에 실패하였습니다.";
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setToast(recvInput);
                    }
                });

            }
        }
    }

    class recvSocket extends Thread{
        String data = "";
        Message msg = null;
        public void run() {
            try {
                readSocket = new DataInputStream(socket.getInputStream());
                while (true) {

                    byte[] b = new byte[100];
                    int ac = readSocket.read(b, 0, b.length);
                    String input = new String(b, 0, b.length);
                    String recvInput = input.trim();

                    if (ac == -1) break;
                    Log.e("receiveData",recvInput);
                    //공기청정기는 안드로이드에서 Connection이 들어오면 Welcome메시지를 응답한다.
                    if(recvInput.equals("Welcome!")){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                setToast("OK");
                            }
                        });
                        //Welcome메시지를 수신하면 안드로이드에서 와이파이이름, 패스워드, 사용자id, 기기id 데이터를 공기청정기로 응답한다. 구분자는 //
                        sendDataMessage sendDataMessage = new sendDataMessage();
                        sendDataMessage.start();
                        //Intent intent = new Intent(APConnectActivity.this,MainActivity.class);
                        /*progressDialog = ProgressDialog.show(APConnectActivity.this,
                                "Registering device...", null, true, false);*/

                        //while(MainActivity.myJSON.equals("null")) {}//이 아이디에 등록된 기기가 없을경우 계속 기다린다.
                        //공기청정기가 와이파이 연결을완료하고 서버에 기기 데이터를 응답했을때 다이얼로그를 없애고 앱을 다시 시작한다.
                        //progressDialog.dismiss();
                        finish();

                        //startActivity(intent);
                    }

                    msg = uiHandler.obtainMessage();
                    msg.what = 100;
                    msg.obj = recvInput;
                    uiHandler.sendMessage(msg);
                }
            } catch (Exception e) {
            }
        }
    }



    class sendDataMessage extends Thread{
        @Override
        public void run(){
            try{
                byte[] b = new byte[256];
                String myDeviceName = "";
                myDeviceName = et_device_name.getText().toString().trim();
                Log.e("WIFI_ID",WIFI_ID);
                Log.e("WIFI_PW",WIFI_PW);
                Log.e("userID",userID);
                Log.e("deviceID",myDeviceName);
                String data = WIFI_ID + "//" + WIFI_PW + "//" + userID +"//"+myDeviceName+"//"+Address.serverIPtoAP+Address.addrInsertData+"//"+Address.serverIPtoAP+Address.addrSetRemoteControl;
                b = data.getBytes();
                writeSocket.write(b);


            }catch (Exception e){
                final String recvInput="메시지 전송에 실패하였습니다";
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setToast(recvInput);
                    }
                });

            }
        }
    }
    void setToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    public String getScanResultSecurity(ScanResult scanResult) {
        //Log.i(TAG, "* getScanResultSecurity");

        final String cap = scanResult.capabilities;
        final String[] securityModes = { "WEP", "PSK", "EAP" };

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }

    public void connectToAP(String ssid, String passkey) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String networkSSID = ssid;
        String networkPass = passkey;

        List<ScanResult> scanResultList = wifiManager.getScanResults();

        for (ScanResult result : scanResultList) {
            if (result.SSID.equals(networkSSID)) {

                String securityMode = getScanResultSecurity(result);

                if (securityMode.equalsIgnoreCase("OPEN")) {
                    Log.e("securityMode","OPEN");
                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int res = wifiManager.addNetwork(wifiConfiguration);
                    boolean b = wifiManager.enableNetwork(res, true);
                    wifiManager.setWifiEnabled(true);
                } else if (securityMode.equalsIgnoreCase("WEP")) {
                    Log.e("securityMode","WEP");
                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
                    wifiConfiguration.wepTxKeyIndex = 0;
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    int res = wifiManager.addNetwork(wifiConfiguration);
                    boolean b = wifiManager.enableNetwork(res, true);
                    wifiManager.setWifiEnabled(true);
                }
                Log.e("securityMode","OTHER");
                wifiConfiguration.SSID = "\"" + networkSSID + "\"";

                wifiConfiguration.preSharedKey = "\"" + networkPass + "\"";
                wifiConfiguration.hiddenSSID = true;
                wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

                int res = wifiManager.addNetwork(wifiConfiguration);
                wifiManager.enableNetwork(res, true);

                boolean changeHappen = wifiManager.saveConfiguration();

                if(res != -1 && changeHappen){

                }else{
                    //Log.d(TAG, "*** Change NOT happen");
                }

                wifiManager.setWifiEnabled(true);
            }
        }
    }
}
