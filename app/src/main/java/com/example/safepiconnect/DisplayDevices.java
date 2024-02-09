package com.example.safepiconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DisplayDevices extends AppCompatActivity {


    private List<BluetoothDevice> devices = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_devices);

        List<String> deviceAddresses = getIntent().getStringArrayListExtra("DEVICE_ADDRESSES");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        for (String address : deviceAddresses) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            devices.add(device);
        }

        if(devices.isEmpty()){
            Toast.makeText(DisplayDevices.this, "NO DEVICES FOUND", Toast.LENGTH_SHORT).show();
        }

        setupListView();
    }

    private void setupListView() {
        ListView listView = findViewById(R.id.listViewDevices);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices);
        listView.setAdapter(adapter);
    }

}