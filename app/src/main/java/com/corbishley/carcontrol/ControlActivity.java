package com.corbishley.carcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

public class ControlActivity extends AppCompatActivity {

    private static final String TAG = "control";
    private Context context;
    private TextView textViewData,textViewBT;
    private String remoteDeviceInfo;
    private BluetoothAdapter btAdapter;
    private BTChatService mChatService;
    private Button buttonLink;
    private String remoteMACAddress;
    private Spinner spinner;
    private final String Song_off = "0";
    private final String Song_1 = "1";
    private final String Song_2 = "2";
    private final String Song_3 = "3";
    private final String Song_4 = "4";
    private String songCMD,lampCMD,fanCMD;
    private Button buttonPlay;
    public static boolean lamp1Flag = false, lamp2Flag = false;
    public static boolean fan1Flag = false, fan2Flag = false;
    private Switch switchlamp1,switchlamp2,switchfan1,switchfan2;
    private final String Lamp1_on = "x";
    private final String Lamp1_off = "y";
    private final String Lamp2_on = "c";
    private final String Lamp2_off = "d";
    private final String Fan1_on = "h";
    private final String Fan1_off = "i";
    private final String Fan2_on = "j";
    private final String Fan2_off = "k";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        setTitle("Control mode");
        context = this;

        textViewData = (TextView) findViewById(R.id.textView_controldata);
        textViewData.setText("Received data:\n");            //*********************1113************

        Intent intent = getIntent();
        remoteDeviceInfo = intent.getStringExtra("btdata");
        textViewBT = (TextView) findViewById(R.id.textView_control);
        textViewBT.setText(remoteDeviceInfo);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mChatService = new BTChatService(context,mHandler);

        buttonLink = (Button) findViewById(R.id.button_control);
        buttonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remoteDeviceInfo != null){
                    remoteMACAddress = remoteDeviceInfo.substring(remoteDeviceInfo.length()-17);
                    BluetoothDevice device = btAdapter.getRemoteDevice(remoteMACAddress);
                    mChatService.connect(device);
                }else{
                    Toast.makeText(context,"No paired BT module.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinner = (Spinner) findViewById(R.id.spinner_id);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        songCMD = Song_off;
                        break;

                    case 1:
                        songCMD = Song_1;
                        break;

                    case 2:
                        songCMD = Song_2;
                        break;

                    case 3:
                        songCMD = Song_3;
                        break;

                    case 4:
                        songCMD = Song_4;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        buttonPlay = (Button) findViewById(R.id.button_play);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCMD(songCMD);
            }
        });

        switchlamp1 = (Switch) findViewById(R.id.switch_lamp1);
        switchlamp2 = (Switch) findViewById(R.id.switch_lamp2);
        switchfan1 = (Switch) findViewById(R.id.switch_fan1);
        switchfan2 = (Switch) findViewById(R.id.switch_fan2);

        switchlamp1.setChecked(lamp1Flag);
        switchlamp2.setChecked(lamp1Flag);
        switchfan1.setChecked(fan1Flag);
        switchfan2.setChecked(fan2Flag);

        switchlamp1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lamp1Flag = isChecked;
                if (isChecked){
                    lampCMD = Lamp1_on;
                }else{
                    lampCMD = Lamp1_off;
                }
                sendCMD(lampCMD);
            }
        });

        switchlamp2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lamp2Flag = isChecked;
                if (isChecked){
                    lampCMD = Lamp2_on;
                }else{
                    lampCMD = Lamp2_off;
                }
                sendCMD(lampCMD);
            }
        });

        switchfan1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fan1Flag = isChecked;
                if (isChecked){
                    fanCMD = Fan1_on;
                }else{
                    fanCMD = Fan1_off;
                }
                sendCMD(fanCMD);
            }
        });

        switchfan2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fan2Flag = isChecked;
                if (isChecked){
                    fanCMD = Fan2_on;
                }else{
                    fanCMD = Fan2_off;
                }
                sendCMD(fanCMD);
            }
        });

    } //end of onCreate()
/****************************copy BTCode*****down******************************/
    // Sends a Command to remote BT device.
    private void sendCMD(String message) {
        // Check that we're actually connected before trying anything
        int mState = mChatService.getState();
        Log.d(TAG, "btstate in sendMessage =" + mState);

        if (mState != BTChatService.STATE_CONNECTED) {
            Log.d(TAG, "btstate =" + mState);
            // Toast.makeText(context, "Bluetooth device is not connected. ", Toast.LENGTH_SHORT).show();
            return;

        } else {
            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                byte[] send = message.getBytes();
                mChatService.BTWrite(send);

            }
        }

    }


    // The Handler that gets information back from the BluetoothChatService
    //There is no message queue leak problem
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    textViewData.append(readMessage);   //display on TextView  **********1113**************
                    Log.d(TAG,"Receive data : "+readMessage);

                    break;

                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDevice = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(context, "Connected to "+ mConnectedDevice, Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_TOAST:
                    Toast.makeText(context, msg.getData().getString(Constants.TOAST),Toast.LENGTH_SHORT).show();
                    break;

                case Constants.MESSAGE_ServerMode:
                    // Toast.makeText(context,"Enter Server accept state.",Toast.LENGTH_SHORT).show();   //display on TextView
                    break;

                case Constants.MESSAGE_ClientMode:
                    //  Toast.makeText(context,"Enter Client connect state.",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
//**************************1113 down*******************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.controlmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.control_exit:
                finish();
                break;

            case R.id.control_clear:
                textViewData.setText("Received data:\n");
                break;

        }
        return super.onOptionsItemSelected(item);
    }
//**************************1113 up*******************

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            Log.d(TAG,"CarActivity onDestry()");
            mChatService.stop();
            mChatService=null;
        }
    }
/****************************copy BTCode*****up******************************/
}
