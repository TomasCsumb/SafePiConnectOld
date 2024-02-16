package com.example.safepiconnect

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.safepiconnect.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    var mainBinding: ActivityMainBinding? = null
    var connectButton: Button? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val foundDevices: MutableList<BluetoothDevice> = ArrayList()
    private var scanning = false
    private var adapter: ArrayAdapter<BluetoothDevice>? = null
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        //TODO change Toasts to logs
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (!foundDevices.contains(device)) {
                foundDevices.add(device)
                //                Toast.makeText(MainActivity.this, "Device found", Toast.LENGTH_SHORT).show();
                // Update your list view or notify the adapter
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                val device = result.device
                if (!foundDevices.contains(device)) {
                    foundDevices.add(device)
                    //                    Toast.makeText(MainActivity.this, "Device found", Toast.LENGTH_SHORT).show();
                    // Update your list view or notify the adapter
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // Handle scan failure
            Toast.makeText(this@MainActivity, "Scan Failed", Toast.LENGTH_SHORT).show()
        }
    }
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //        FirebaseApp.initializeApp(/*context=*/ this);
//        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
//        firebaseAppCheck.installAppCheckProviderFactory(
//                PlayIntegrityAppCheckProviderFactory.getInstance());
//        CompanionDeviceManager deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // Write a message to the database
        //    FirebaseDatabase database = FirebaseDatabase.getInstance();
        //    DatabaseReference myRef = database.getReference("message");
        val bluetoothAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        val bluetoothLEAvailable =
            packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        setupVariables()
        if (bluetoothAvailable) {
            if (bluetoothLEAvailable) {
                connectButton!!.setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Toast.makeText(
                            this@MainActivity, "Scanning for BLE Devices",
                            Toast.LENGTH_LONG
                        ).show()
                        scanLeDevice()
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private fun scanLeDevice() {
        if (!scanning) {
            // Check if the BLUETOOTH_SCAN permission has been granted
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                    REQUEST_BLUETOOTH_SCAN_PERMISSION
                )
                return
            }

            // Stops scanning after a predefined scan period.
            handler.postDelayed({
                scanning = false
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothLeScanner!!.stopScan(leScanCallback)
                    startDisplayDevicesActivity()
                }
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner!!.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner!!.stopScan(leScanCallback)
        }
    }

    fun setupVariables() {
        mainBinding = ActivityMainBinding.inflate(
            layoutInflater
        )
        setContentView(mainBinding!!.root)
        connectButton = mainBinding!!.connectButton
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    scanLeDevice() // you can restart scanning here
                }
            } else {
                // Permission was denied
            }
        }
    }

    private fun startDisplayDevicesActivity() {
        val intent = Intent(this@MainActivity, DisplayDevices::class.java)
        val deviceAddresses = ArrayList<String>()
        for (device in foundDevices) {
            deviceAddresses.add(device.address)
        }
        if (foundDevices.isEmpty()) {
            Toast.makeText(this@MainActivity, "NO DEVICES FOUND", Toast.LENGTH_SHORT).show()
        }
        intent.putStringArrayListExtra("DEVICE_ADDRESSES", deviceAddresses)
        startActivity(intent)
    }

    private fun updateListView() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, foundDevices)
        runOnUiThread { adapter!!.notifyDataSetChanged() }
    } //    private boolean checkPermissions (){

    //        boolean
    //
    //        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH)
    //                != PackageManager.PERMISSION_GRANTED)
    //        {
    //            // Permission is not granted
    //            // Request Permission
    //            Log.println(Log.DEBUG, "Permission Error", "BLUETOOTH permission denied");
    //        }
    //
    //        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_ADMIN)
    //                != PackageManager.PERMISSION_GRANTED)
    //        {
    //            // Permission is not granted
    //            // Request Permission
    //            Log.println(Log.DEBUG, "Permission Error", "BLUETOOTH_ADMIN permission denied");
    //        }
    //
    //        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN)
    //                != PackageManager.PERMISSION_GRANTED)
    //        {
    //            // Permission is not granted
    //            // Request Permission
    //            Log.println(Log.DEBUG, "Permission Error", "BLUETOOTH_SCAN permission denied");
    //        }
    //    }
    companion object {
        private const val MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 1
        private const val REQUEST_BLUETOOTH_SCAN_PERMISSION = 1

        // Stops scanning after 10 seconds.
        private const val SCAN_PERIOD: Long = 10000
    }
}