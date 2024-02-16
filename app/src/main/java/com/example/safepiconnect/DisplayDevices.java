package com.example.safepiconnect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.ble.exception.BluetoothDisabledException;
import no.nordicsemi.android.ble.exception.DeviceDisconnectedException;
import no.nordicsemi.android.ble.exception.InvalidRequestException;
import no.nordicsemi.android.ble.exception.RequestFailedException;


public class DisplayDevices extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 1;
    private List<String> deviceRegistry = new ArrayList<>();
    private List<BluetoothDevice> bleDevices = new ArrayList<>();
    private List<String> bleDeviceNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static final UUID MY_SERVICE_UUID = UUID.fromString("A07498CA-AD5B-474E-940D-16F1FBE7E8CD");
    public static final UUID MY_READ_CHARACTERISTIC_UUID = UUID.fromString("51FF12BB-3ED8-46E5-B4F9-D64E2FEC021B");
    public static final UUID MY_WRITE_CHARACTERISTIC_UUID = UUID.fromString("52FF12BB-3ED8-46E5-B4F9-D64E2FEC021B");

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
                deviceName = device.getAlias();
                if(device == null || deviceName.isEmpty()){
                    deviceName = "Unknown Device";
                }
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
                connectToDevice(selectedDevice);

//                if(!connectToDevice(selectedDevice)){
//                    // error could not connect
//                }
//                else{
//                    //check device status
//                    //get services
//                    //pass data
//                }
            }
        });
    }

    private void connectToDevice(final BluetoothDevice selectedDevice) {
        BluetoothGattCallback gattCallback = createBluetoothGattCallback();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(DisplayDevices.this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
            return;
        }
        BluetoothGatt bluetoothGatt = selectedDevice.connectGatt(this, false, gattCallback);
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

    private BluetoothGattCallback createBluetoothGattCallback() {
        return new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Device connected
                    if (ActivityCompat.checkSelfPermission(DisplayDevices.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DisplayDevices.this,
                                new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                                MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
                        return;
                    }
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // Device disconnected
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                // Services discovered
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(MY_SERVICE_UUID);
                    if (service != null) {
                        // Find read characteristic
                        BluetoothGattCharacteristic readCharacteristic = service.getCharacteristic(MY_READ_CHARACTERISTIC_UUID);
                        if (readCharacteristic != null) {
                            // You can read the characteristic here or set it up to be read later
                            if (ActivityCompat.checkSelfPermission(DisplayDevices.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(DisplayDevices.this,
                                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                                        MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
                                return;
                            }
                            gatt.readCharacteristic(readCharacteristic);
                        }

                        // Find write characteristic
                        BluetoothGattCharacteristic writeCharacteristic = service.getCharacteristic(MY_WRITE_CHARACTERISTIC_UUID);
                        if (writeCharacteristic != null) {
                            // You can write to the characteristic here or set it up to be written to later
                        }
                    }
                } else {
                    Log.w("BluetoothGatt", "onServicesDiscovered received: " + status);
                }
            }
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic,
                                             int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Process the data
                    final byte[] data = characteristic.getValue();
                    String piRead = new String(data, StandardCharsets.UTF_8);
                    // Update UI on the main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Update your UI here with the data
                            Toast.makeText(DisplayDevices.this, piRead, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }


            // Implement other callback methods like onCharacteristicRead, onCharacteristicWrite, etc.
        };
    }

}