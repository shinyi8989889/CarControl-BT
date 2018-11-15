package com.corbishley.carcontrol;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class CarActivity extends AppCompatActivity {

    private static final String TAG = "button";
    private String remoteDeviceInfo;
    private TextView textViewBT,textViewSong;
    private Context context;
    private BluetoothAdapter btAdapter;
    private BTChatService mChatService;
    private Button buttonLink;
    private String remoteMacAddress;
    private ImageButton buttonTop,buttonDown,buttonRight,buttonLeft,buttonStop;
    private String directionCMD;
    private final String GO_Forward = "F";
    private final String GO_Left = "L";
    private final String GO_Right = "R";
    private final String GO_Down = "B";
    private final String GO_Stop = "S";
    private final String Song_off = "0";
    private final String Song_1 = "1";
    private final String Song_2 = "2";
    private final String Song_3 = "3";
    private final String Song_4 = "4";
    private String songCMD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //讓畫面不會自己亂移動

        setTitle("Car Mode");

        Intent intent = getIntent();
        remoteDeviceInfo = intent.getStringExtra("btdata");  //藍芽傳過來的東西
        textViewBT = (TextView) findViewById(R.id.textView_button);  //放在textView上面
        textViewBT.setText(remoteDeviceInfo);

        textViewSong = (TextView) findViewById(R.id.textView_song);
        textViewSong.setText("");

        context = this;

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        mChatService = new BTChatService(context,mHandler);

        buttonLink = (Button) findViewById(R.id.button_car);
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

        buttonTop = (ImageButton) findViewById(R.id.imageButton_top);
        buttonDown = (ImageButton) findViewById(R.id.imageButton_down);
        buttonRight = (ImageButton) findViewById(R.id.imageButton_right);
        buttonLeft = (ImageButton) findViewById(R.id.imageButton_left);
        buttonStop = (ImageButton) findViewById(R.id.imageButton_sensorstop);
        buttonTop.setOnClickListener(new myClick());
        buttonDown.setOnClickListener(new myClick());
        buttonRight.setOnClickListener(new myClick());
        buttonLeft.setOnClickListener(new myClick());
        buttonStop.setOnClickListener(new myClick());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.car_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch((item.getItemId())){
            case R.id.car_exit:
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Exit");
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setMessage("Are you sure to exit car mode ? ");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                break;

            case R.id.songoff:
                songCMD = Song_off;
                textViewSong.setText("Song off");
                sendCMD(songCMD);
                break;

            case R.id.song1:
                songCMD = Song_1;
                textViewSong.setText("Song 1");
                sendCMD(songCMD);
                break;

            case R.id.song2:
                songCMD = Song_2;
                textViewSong.setText("Song 2");
                sendCMD(songCMD);
                break;

            case R.id.song3:
                songCMD = Song_3;
                textViewSong.setText("Song 4");
                sendCMD(songCMD);
                break;

            case R.id.song4:
                songCMD = Song_4;
                textViewSong.setText("Song 4");
                sendCMD(songCMD);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /***********copy BTCode.java*****down****************/

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

                    textViewSong.append("remote : " + readMessage + "\n");   //display on TextView
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            Log.d(TAG,"CarActivity onDestry()");
            mChatService.stop();
            mChatService=null;
        }
    }
    /***********copy BTCode.java*****up****************/

    private class myClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.imageButton_top:
                    directionCMD = GO_Forward;
                    sendCMD(directionCMD);
                    break;

                case R.id.imageButton_left:
                    directionCMD = GO_Left;
                    sendCMD(directionCMD);
                    break;

                case R.id.imageButton_right:
                    directionCMD = GO_Right;
                    sendCMD(directionCMD);
                    break;

                case R.id.imageButton_down:
                    directionCMD = GO_Down;
                    sendCMD(directionCMD);
                    break;

                case R.id.imageButton_sensorstop:
                    directionCMD = GO_Stop;
                    sendCMD(directionCMD);
                    break;
            }
        }
    } // end of button

}
