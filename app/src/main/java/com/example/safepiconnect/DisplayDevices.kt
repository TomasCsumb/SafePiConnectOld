package com.example.safepiconnect

import android.Manifest
import android.bluetooth.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.nio.charset.StandardCharsets
import java.util.*

class DisplayDevices : AppCompatActivity() {
    private val deviceRegistry: MutableList<String> = ArrayList()
    private val bleDevices: MutableList<BluetoothDevice?> = ArrayList()
    private val bleDeviceNames: MutableList<String?> = ArrayList()
    private var adapter: ArrayAdapter<String?>? = null
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    //    private MyBleManager bleManager = new MyBleManager(DisplayDevices.this);
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_devices)
        //MAC address of TomasMacBook
        deviceRegistry.add("AC:C9:06:19:3E:61")
        populateDeviceNames()
        setupListView()
    }

    private fun populateDeviceNames() {
        val deviceAddresses: List<String>? = intent.getStringArrayListExtra("DEVICE_ADDRESSES")
        for (address in deviceAddresses!!) {
            val device = bluetoothAdapter.getRemoteDevice(address)
            bleDevices.add(device)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ),
                    MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN
                )
                return
            }
            var deviceName = device!!.name
            if (deviceName == null || deviceName.isEmpty()) {
                deviceName = device.alias
                if (device == null || deviceName!!.isEmpty()) {
                    deviceName = "Unknown Device"
                }
            }
            bleDeviceNames.add(deviceName)
        }
    }

    private fun setupListView() {
        val listView = findViewById<ListView>(R.id.listViewDevices)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bleDeviceNames)
        listView.adapter = adapter
        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val selectedDevice = bleDevices[position]
            // Now you can use selectedDevice to initiate a connection
            connectToDevice(selectedDevice)

//                if(!connectToDevice(selectedDevice)){
//                    // error could not connect
//                }
//                else{
//                    //check device status
//                    //get services
//                    //pass data
//                }
        }
    }

    private fun connectToDevice(selectedDevice: BluetoothDevice?) {
        val gattCallback = createBluetoothGattCallback()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                this@DisplayDevices,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT),
                MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN
            )
            return
        }
        val bluetoothGatt = selectedDevice!!.connectGatt(this, false, gattCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN) {
            // Check if all required permissions are granted
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                populateDeviceNames()
            } else {
                // Handle the case where permissions are denied
                Log.println(
                    Log.ERROR,
                    "Permission Error",
                    "Unable to grant BLUETOOTH_SCAN permission for getName()"
                )
            }
        }
    }

    private fun createBluetoothGattCallback(): BluetoothGattCallback {
        return object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Device connected
                    if (ActivityCompat.checkSelfPermission(
                            this@DisplayDevices,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@DisplayDevices,
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ),
                            MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN
                        )
                        return
                    }
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // Device disconnected
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                // Services discovered
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(MY_SERVICE_UUID)
                    if (service != null) {
                        // Find read characteristic
                        val readCharacteristic = service.getCharacteristic(
                            MY_READ_CHARACTERISTIC_UUID
                        )
                        if (readCharacteristic != null) {
                            // You can read the characteristic here or set it up to be read later
                            if (ActivityCompat.checkSelfPermission(
                                    this@DisplayDevices,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                ActivityCompat.requestPermissions(
                                    this@DisplayDevices, arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    ),
                                    MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN
                                )
                                return
                            }
                            gatt.readCharacteristic(readCharacteristic)
                        }

                        // Find write characteristic
                        val writeCharacteristic = service.getCharacteristic(
                            MY_WRITE_CHARACTERISTIC_UUID
                        )
                        if (writeCharacteristic != null) {
                            // You can write to the characteristic here or set it up to be written to later
                        }
                    }
                } else {
                    Log.w("BluetoothGatt", "onServicesDiscovered received: $status")
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Process the data
                    val data = characteristic.value
                    val piRead = String(data, StandardCharsets.UTF_8)
                    // Update UI on the main thread
                    runOnUiThread { // Update your UI here with the data
                        Toast.makeText(this@DisplayDevices, piRead, Toast.LENGTH_LONG).show()
                    }
                }
            } // Implement other callback methods like onCharacteristicRead, onCharacteristicWrite, etc.
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 1
        val MY_SERVICE_UUID = UUID.fromString("A07498CA-AD5B-474E-940D-16F1FBE7E8CD")
        val MY_READ_CHARACTERISTIC_UUID = UUID.fromString("51FF12BB-3ED8-46E5-B4F9-D64E2FEC021B")
        val MY_WRITE_CHARACTERISTIC_UUID = UUID.fromString("52FF12BB-3ED8-46E5-B4F9-D64E2FEC021B")
    }
}