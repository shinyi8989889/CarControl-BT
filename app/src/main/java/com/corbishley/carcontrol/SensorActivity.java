package com.corbishley.carcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

public class SensorActivity extends AppCompatActivity {

    private static final String TAG = "sensor";
    private Context context;
    private String remoteDeviceInfo,remoteMacAddress;
    private TextView TextViewBT,TextViewACC;
    private BluetoothAdapter btAdapter;
    private BTChatService mChatService;
    private Button buttonLink;
    private boolean startFlag;
    private ImageView imageTop,imageLeft,imageRight,imageDown;
    private ImageButton imageStop;

    private String directionCMD;
    private final String GO_Forward = "F";
    private final String GO_Left = "L";
    private final String GO_Right = "R";
    private final String GO_Down = "B";
    private final String GO_Stop = "P";
    private SensorManager sensorManager;
    private MyListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle("Sensor mode");

        context = this;

        Intent intent = getIntent();
        remoteDeviceInfo = intent.getStringExtra("btdata");

        TextViewBT = (TextView) findViewById(R.id.textView_sensor);
        TextViewBT.setText(remoteDeviceInfo);

        TextViewACC = (TextView) findViewById(R.id.textView_accdata);
        TextViewACC.setText("");

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mChatService = new BTChatService(context,mHandler);  //mHandler處理畫面的message


        startFlag = false;
        buttonLink = (Button) findViewById(R.id.button_sensorlink);
        buttonLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remoteDeviceInfo != null){   //remoteDeviceInfo is bluetoothinfo
                    remoteMacAddress = remoteDeviceInfo.substring(remoteDeviceInfo.length()-17); //只取後面17個有用字
                    Log.d(TAG,"mac address = "+remoteMacAddress);

                    BluetoothDevice device = btAdapter.getRemoteDevice(remoteMacAddress);
                    Log.d(TAG,"device = "+device);

                    mChatService.connect(device);
                }else{
                    Toast.makeText(context,"There is no BT device.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageTop = (ImageView) findViewById(R.id.imageView_top);
        imageLeft = (ImageView) findViewById(R.id.imageView_left);
        imageRight = (ImageView) findViewById(R.id.imageView_right);
        imageDown = (ImageView) findViewById(R.id.imageView_down);

        imageTop.setVisibility(View.INVISIBLE);
        imageLeft.setVisibility(View.INVISIBLE);
        imageRight.setVisibility(View.INVISIBLE);
        imageDown.setVisibility(View.INVISIBLE);

        imageStop = (ImageButton) findViewById(R.id.imageButton_sensorstop);
        imageStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startFlag == false){
                    startFlag = true;
                    imageStop.setImageResource(R.drawable.stop_icon);
                }else{
                    startFlag = false;
                    imageStop.setImageResource(R.drawable.start);
                    directionCMD =GO_Stop;
                    sendCMD(directionCMD);

                }
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        listener = new MyListener();
        sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_UI);

    }

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

                    TextViewACC.append("remote : " + readMessage + "\n");   //display on TextView
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

    private class MyListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            StringBuilder sb = new StringBuilder();

        if (startFlag) {

            float X_value = event.values[0];
            float Y_value = event.values[1];
            float Z_value = event.values[2];  //float是資料型態 Float是物件

            sb.append("X = " + Float.toString(X_value) + "\n");
            sb.append("Y = " + Float.toString(Y_value) + "\n");
            sb.append("Z = " + Float.toString(Z_value) + "\n");

            TextViewACC.setText(sb.toString());

            if (X_value < -3.0) {  //turn right

                directionCMD = GO_Right;
                sendCMD(directionCMD);

                imageTop.setVisibility(View.INVISIBLE);
                imageLeft.setVisibility(View.INVISIBLE);
                imageRight.setVisibility(View.VISIBLE);
                imageDown.setVisibility(View.INVISIBLE);

            } else if (X_value > 3.0) {  //turn left

                directionCMD = GO_Left;
                sendCMD(directionCMD);

                imageTop.setVisibility(View.INVISIBLE);
                imageLeft.setVisibility(View.VISIBLE);
                imageRight.setVisibility(View.INVISIBLE);
                imageDown.setVisibility(View.INVISIBLE);

            } else {

                if (Z_value > 8) {  //代表往前走

                    directionCMD = GO_Forward;
                    sendCMD(directionCMD);

                    imageTop.setVisibility(View.VISIBLE);
                    imageLeft.setVisibility(View.INVISIBLE);
                    imageRight.setVisibility(View.INVISIBLE);
                    imageDown.setVisibility(View.INVISIBLE);

                } else if (Z_value < 1) {  //向回走

                    directionCMD = GO_Down;
                    sendCMD(directionCMD);

                    imageTop.setVisibility(View.INVISIBLE);
                    imageLeft.setVisibility(View.INVISIBLE);
                    imageRight.setVisibility(View.INVISIBLE);
                    imageDown.setVisibility(View.VISIBLE);

                } else {   //不動的,不改變,不會改變行進方向

                    directionCMD = GO_Stop;
                    sendCMD(directionCMD);

                    imageTop.setVisibility(View.INVISIBLE);
                    imageLeft.setVisibility(View.INVISIBLE);
                    imageRight.setVisibility(View.INVISIBLE);
                    imageDown.setVisibility(View.INVISIBLE);

                }
            }
        }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        sensorManager.unregisterListener(listener);
        startFlag = false;

        if (mChatService != null) {
            Log.d(TAG,"CarActivity onDestry()");
            mChatService.stop();
            mChatService=null;
        }
    }
}
