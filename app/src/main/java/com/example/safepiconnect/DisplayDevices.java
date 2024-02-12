package com.example.safepiconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



public class DisplayDevices extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 1;
    private List<String> deviceRegistry = new ArrayList<>();
    private List<BluetoothDevice> bleDevices = new ArrayList<>();
    private List<String> bleDeviceNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//    private MyBleManager bleManager = new MyBleManager(DisplayDevices.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_devices);
        //MAC address of TomasMacBook
        deviceRegistry.add("AC:C9:06:19:3E:61");
        populateDeviceNames();
        setupListView();
    }

    private void populateDeviceNames() {
        List<String> deviceAddresses = getIntent().getStringArrayListExtra("DEVICE_ADDRESSES");
        for (String address : deviceAddresses) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bleDevices.add(device);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
                return;
            }
            String deviceName = device.getName();
            if (deviceName == null || deviceName.isEmpty()) {
                deviceName = "Unknown Device";
            }
            bleDeviceNames.add(deviceName);
        }
    }

    private void setupListView() {
        ListView listView = findViewById(R.id.listViewDevices);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bleDeviceNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice selectedDevice = bleDevices.get(position);
                // Now you can use selectedDevice to initiate a connection
                if(!connectToDevice(selectedDevice)){
                    // error could not connect
                }
                else{
                    //check device status
                    //get services
                    //pass data
                }
            }
        });
    }

    private boolean connectToDevice(BluetoothDevice selectedDevice) {
        boolean inRegistry = true;
        if (!deviceRegistry.contains(selectedDevice.getAddress())){
            inRegistry = false;
            //error
            return inRegistry;
        }
        else{
            // connect to device
            MyBleManager bleManager = new MyBleManager(this);

            // When you want to connect to a device
            bleManager.connect(selectedDevice)
                    .useAutoConnect(true) // Set to true if you want to automatically connect to the device when it's in range
                    .enqueue();
        }
        return inRegistry;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN) {
            // Check if all required permissions are granted
            if (grantResults.length > 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                populateDeviceNames();
            } else {
                // Handle the case where permissions are denied
                Log.println(Log.ERROR, "Permission Error", "Unable to grant BLUETOOTH_SCAN permission for getName()");
            }
        }
    }

}