package newlogic.plugins.datecsprinter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(
    name = "DatecsPrinter",
    permissions = {
        @Permission(
            alias = "old_bluetooth",
            strings = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }
        ),
        @Permission(alias = "new_bluetooth", strings = { Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN })
    }
)
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
        implementation.setBLStateReceiver(
            new BroadcastReceiver() {
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
            }
        );
        implementation.setSearchBluetoothReceiverReceiver(
            new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Discovery has found a device. Get the BluetoothDevice
                        // object and its info from the Intent.
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        updateSearchStatus(device);
                    }
                }
            }
        );
    }

    @Override
    protected void handleOnResume() {
        implementation.startMonitoring(getActivity());
    }

    @Override
    protected void handleOnPause() {
        implementation.stopMonitoring(getActivity());
    }

    @PluginMethod
    public void scanBluetoothDevice(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (getPermissionState("new_bluetooth") != PermissionState.GRANTED) {
                requestPermissionForAlias("new_bluetooth", call, "scanBluetoothDeviceCallback");
            } else {
                implementation.startScanBluetoothDevice(call);
            }
        } else {
            if (getPermissionState("old_bluetooth") != PermissionState.GRANTED) {
                requestPermissionForAlias("old_bluetooth", call, "scanBluetoothDeviceCallback");
            } else {
                implementation.startScanBluetoothDevice(call);
            }
        }
    }

    @PermissionCallback
    private void scanBluetoothDeviceCallback(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (getPermissionState("new_bluetooth") == PermissionState.GRANTED) {
                implementation.startScanBluetoothDevice(call);
                call.resolve();
            } else {
                call.reject("Permission is required to scan bluetooth");
            }
        } else {
            if (getPermissionState("old_bluetooth") == PermissionState.GRANTED) {
                implementation.startScanBluetoothDevice(call);
                call.resolve();
            } else {
                call.reject("Permission is required to scan bluetooth");
            }
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
