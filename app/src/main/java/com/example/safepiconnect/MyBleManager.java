package com.example.safepiconnect;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import no.nordicsemi.android.ble.BleManager;

public class MyBleManager extends BleManager {

    private static final String TAG = "MyBleManager";

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
    private BluetoothGattCharacteristic fluxCapacitorControlPoint;

    @Override
    protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
        // Here obtain instances of your characteristics.
        // Return false if a required service has not been discovered.
        BluetoothGattService fluxCapacitorService = gatt.getService(FLUX_SERVICE_UUID);
        if (fluxCapacitorService != null) {
            fluxCapacitorControlPoint = fluxCapacitorService.getCharacteristic(FLUX_CHAR_UUID);
        }
        return fluxCapacitorControlPoint != null;
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
        fluxCapacitorControlPoint = null;
    }

    // ==== Public API ====

    // Here you may add some high level methods for your device:
    public void enableFluxCapacitor() {
        // Do the magic.
        writeCharacteristic(fluxCapacitorControlPoint, Flux.enable(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                .enqueue();
    }
}
