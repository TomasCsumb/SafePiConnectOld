package com.example.safepiconnect;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import android.bluetooth.BluetoothManager;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

public class MyBleManager extends BleManager{

    private static final String TAG = "MyBleManager";
//    private ByteArrayInputStream myUUID = new ByteArrayInputStream(51FF12BB-3ED8-46E5-B4F9-D64E2FEC021B)
    private static final UUID FLUX_SERVICE_UUID = UUID.fromString("A07498CA-AD5B-474E-940D-16F1FBE7E8CD");
    private static final UUID READ_CHAR_UUID = UUID.fromString("51FF12BB-3ED8-46E5-B4F9-D64E2FEC021B");
    private static final UUID WRITE_CHAR_UUID = UUID.fromString("52FF12BB-3ED8-46E5-B4F9-D64E2FEC021B");


    public MyBleManager(@NonNull Context context) {
        super(context);
    }

    public MyBleManager(@NonNull Context context, @NonNull Handler handler) {
        super(context, handler);
    }

    // ==== Logging =====

    @Override
    public int getMinLogPriority() {
        // Use to return minimal desired logging priority.
        return Log.VERBOSE;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        // Log from here.
        Log.println(priority, TAG, message);
    }

    // ==== Required implementation ====

    // This is a reference to a characteristic that the manager will use internally.
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;

    @Override
    protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
        // Here obtain instances of your characteristics.
        // Return false if a required service has not been discovered.
        BluetoothGattService fluxCapacitorService = gatt.getService(FLUX_SERVICE_UUID);
        if (fluxCapacitorService != null) {
            readCharacteristic = fluxCapacitorService.getCharacteristic(READ_CHAR_UUID);
            writeCharacteristic = fluxCapacitorService.getCharacteristic(WRITE_CHAR_UUID);
        }
        return (readCharacteristic != null && writeCharacteristic != null);
    }

    @Override
    protected void initialize() {
        // Initialize your device.
        // This means e.g. enabling notifications, setting notification callbacks, or writing
        // something to a Control Point characteristic.
        // Kotlin projects should not use suspend methods here, as this method does not suspend.
        requestMtu(517)
                .enqueue();
    }

    @Override
    protected void onServicesInvalidated() {
        // This method is called when the services get invalidated, i.e. when the device
        // disconnects.
        // References to characteristics should be nullified here.
        readCharacteristic = null;
        writeCharacteristic = null;
    }


    // ==== Public API ====

    // Here you may add some high level methods for your device:
    public void enableFluxCapacitor() {
        // Do the magic.
//        writeCharacteristic(writeCharacteristic, Flux.enable(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
//                .enqueue();
    }
}
