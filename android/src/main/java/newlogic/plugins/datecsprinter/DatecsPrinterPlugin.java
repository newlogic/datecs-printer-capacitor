package newlogic.plugins.datecsprinter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(name = "DatecsPrinter", permissions = {@Permission(alias = "bluetooth", strings = {Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION})})
public class DatecsPrinterPlugin extends Plugin {

    public static final String BLUETOOTH_STATUS_CHANGE = "bluetoothChange";
    public static final String BLUETOOTH_SEARCH_CHANGE = "bluetoothSearchChange";
    private static final String BLUETOOTH_ON = "BLUETOOTH_ON";
    private static final String BLUETOOTH_OFF = "BLUETOOTH_OFF";
    private DatecsPrinter implementation;

    private boolean isBluetoothOn(int state) {
        return state == BluetoothAdapter.STATE_ON;
    }

    private boolean getBluetoothConnectionStatus() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return isBluetoothOn(bluetoothAdapter.getState());
    }

    private void updateConnectionStatus(String connectionStatus) {
        JSObject ret = new JSObject();
        ret.put("status", connectionStatus);
        notifyListeners(BLUETOOTH_STATUS_CHANGE, ret);
    }

    @SuppressLint("MissingPermission")
    private void updateSearchStatus(BluetoothDevice device) {
        JSObject ret = new JSObject();
        ret.put("name", device.getName());
        ret.put("address", device.getAddress());

        notifyListeners(BLUETOOTH_SEARCH_CHANGE, ret);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void load() {
        implementation = new DatecsPrinter(getActivity());
        implementation.setBLStateReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                String action = intent.getAction();

                if (isBluetoothOn(state)) {
                    updateConnectionStatus(BLUETOOTH_ON);
                } else {
                    updateConnectionStatus(BLUETOOTH_OFF);
                }
                return;
            }
        });
        implementation.setSearchBluetoothReceiverReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    updateSearchStatus(device);
                }
            }
        });


    }

    @Override
    protected void handleOnResume() {
        implementation.startMonitoring(getActivity());
    }

    @Override
    protected void handleOnPause() {
        implementation.stopMonitoring(getActivity());
    }

    @PluginMethod()
    public void scanBluetoothDevice(PluginCall call) {
        if (getPermissionState("bluetooth") != PermissionState.GRANTED) {
            requestPermissionForAlias("bluetooth", call, "scanBluetoothDeviceCallback");
        } else {
            implementation.startScanBluetoothDevice();
            call.resolve();
        }
    }

    @PermissionCallback
    private void scanBluetoothDeviceCallback(PluginCall call) {
        if (getPermissionState("bluetooth") == PermissionState.GRANTED) {
            implementation.startScanBluetoothDevice();
        } else {
            call.reject("Permission is required to scan bluetooth");
        }
    }

    @PluginMethod
    public void getConnectionStatus(PluginCall call) {
        String status = BLUETOOTH_OFF;

        if (getBluetoothConnectionStatus()) {
            status = BLUETOOTH_ON;
        }

        JSObject ret = new JSObject();
        ret.put("status", status);
        call.resolve(ret);
    }

    @PluginMethod
    public void getBluetoothPairedDevices(PluginCall call) {
        implementation.getBluetoothPairedDevices(call);
    }

    @PluginMethod
    public void connect(PluginCall call) {
        String address = call.getString("address");
        implementation.connect(address, call);
    }

    @PluginMethod
    public void print(PluginCall call) {
        String content = call.getString("content");
        implementation.printTaggedText(content, call);
    }
}
