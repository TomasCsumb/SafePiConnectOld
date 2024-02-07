package com.example.safepiconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.companion.AssociationRequest;
import android.companion.BluetoothLeDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.widget.Button;

import com.example.safepiconnect.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    Button connectButton;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 1;

    // Write a message to the database
//    FirebaseDatabase database = FirebaseDatabase.getInstance();
//    DatabaseReference myRef = database.getReference("message");
    boolean bluetoothAvailable;
    boolean bluetoothLEAvailable;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // handle individual scan result
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            // handle batch scan results
        }

        @Override
        public void onScanFailed(int errorCode) {
            // handle scan failure
        }
    };
    private Handler handler = new Handler(Looper.getMainLooper());

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    BluetoothLeDeviceFilter deviceFilter = new BluetoothLeDeviceFilter.Builder()
            // Match only Bluetooth devices whose name matches the pattern.
            //TODO change pattern name
            .setNamePattern(Pattern.compile("My device"))
            // Match only Bluetooth devices whose service UUID matches this pattern.
            //.addServiceUuid(new ParcelUuid(new UUID(0x123abcL, -1L)), null)
            .build();

    AssociationRequest pairingRequest = new AssociationRequest.Builder()
            // Find only devices that match this request filter.
            .addDeviceFilter(deviceFilter)
            // Stop scanning as soon as one device matching the filter is found.
            .setSingleDevice(true)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        FirebaseApp.initializeApp(/*context=*/ this);
//        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
//        firebaseAppCheck.installAppCheckProviderFactory(
//                PlayIntegrityAppCheckProviderFactory.getInstance());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        CompanionDeviceManager deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
        bluetoothAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        bluetoothLEAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        setupVariables();
//        myRef.setValue("Hello, World!");
        if (bluetoothAvailable) {
            if (bluetoothLEAvailable) {
                scanLeDevice();
            }
        }
    }

    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_SCAN)) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
                        return;
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }


    void setupVariables(){
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        connectButton = mainBinding.connectButton;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    scanLeDevice(); // you can restart scanning here
                } else {
                    // Permission was denied
                    // Handle the denial appropriately
                }
                break;
            // You can handle other permission requests here
        }
    }
}