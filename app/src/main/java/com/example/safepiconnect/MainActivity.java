package com.example.safepiconnect;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.view.View;
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
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    Button connectButton;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 1;
    private static final int REQUEST_BLUETOOTH_SCAN_PERMISSION = 1;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;

    private final ScanCallback leScanCallback = new ScanCallback() {
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
    private final Handler handler = new Handler(Looper.getMainLooper());

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
//        CompanionDeviceManager deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Write a message to the database
        //    FirebaseDatabase database = FirebaseDatabase.getInstance();
        //    DatabaseReference myRef = database.getReference("message");
        boolean bluetoothAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
        boolean bluetoothLEAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        setupVariables();
//        myRef.setValue("Hello, World!");
        if (bluetoothAvailable) {
            if (bluetoothLEAvailable) {
                connectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            scanLeDevice();
                        }
                    }
                });
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void scanLeDevice() {
        if (!scanning) {
            // Check if the BLUETOOTH_SCAN permission has been granted
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_SCAN_PERMISSION);
                return;
            }

            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                        bluetoothLeScanner.stopScan(leScanCallback);
                    }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        scanLeDevice(); // you can restart scanning here
                    }
                } else {
                    // Permission was denied

                }
                break;
            // You can handle other permission requests here
        }
    }
}