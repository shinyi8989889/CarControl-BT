package com.corbishley.carcontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class SearchActivity extends AppCompatActivity {

    private static final int RequestCode = 100;
    private static final String TAG = "requestcode";
    private final int enableItem = 10;
    private final int stopItem = 11;
    private final int exitItem = 12;
    private Context context;
    private BluetoothAdapter btAdapter;
    private ListView listViewSearch;
    private ArrayList<String> btDeviceList;
    private ArrayAdapter<String> searchAdapter;
    private Button buttonSearch,buttonPair;
    private boolean receiverFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setTitle("Search mode");
        context = this;

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        int check = ActivityCompat.checkSelfPermission(context,"Manifest.permission.ACCESS_COARSE_LOCATION");
        if (check != PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SearchActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    RequestCode);
        }

        listViewSearch = (ListView) findViewById(R.id.listView_search);
        btDeviceList = new ArrayList<String>();
        searchAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,btDeviceList);
        listViewSearch.setAdapter(searchAdapter);

        buttonSearch = (Button) findViewById(R.id.button_search);
        buttonPair = (Button) findViewById(R.id.button_pair);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btDeviceList.clear();
                searchAdapter.clear();
                listViewSearch.setAdapter(searchAdapter);

                btAdapter.startDiscovery();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                receiverFlag = true;
                registerReceiver(mReceiver, filter);
                Toast.makeText(context,"start to search BT",Toast.LENGTH_SHORT).show();
            }
        });
    }  // end of onCreate()

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                searchAdapter.add("found : "+device.getName()+"\n"+device.getAddress());
            }
        }
    };

    @Override  //***************bluetooth 6.0 up work only*******************
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RequestCode){
            Log.d(TAG,"Request Code");
            if (grantResults.length !=0){
                Log.d(TAG,"result = "+grantResults[0]);
                if (grantResults[0] == PERMISSION_DENIED){
                    Toast.makeText(context,"BT access deny",Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item1 = menu.add(1, enableItem, Menu.NONE, "Enable");
        item1.setIcon(R.drawable.enable_icon);
        item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(2, stopItem,Menu.NONE,"Stop");
        menu.add(3, exitItem,Menu.NONE,"Exit");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case enableItem:
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,100); //要給人發現多久的時間,100秒
                startActivity(intent);
                break;

            case stopItem:
                btAdapter.cancelDiscovery();

                break;

            case exitItem:
                btAdapter.cancelDiscovery();
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        btAdapter.cancelDiscovery();
        if (mReceiver != null){
            if (receiverFlag){
                unregisterReceiver(mReceiver);
                receiverFlag = false;
            }
        }
    }
}
