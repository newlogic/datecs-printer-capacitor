package newlogic.plugins.datecsprinter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.util.Set;

@CapacitorPlugin(
    name = "DatecsPrinter",
    permissions = {
        @Permission(
            alias = "bluetooth",
            strings = {
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN
            }
        )
    }
)
public class DatecsPrinterPlugin extends Plugin {

    public static final String BLUETOOTH_STATUS_CHANGE = "bluetoothChange";
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

    @Override
    public void load() {
        implementation = new DatecsPrinter(getActivity());
        implementation.setReceiver(
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    if (isBluetoothOn(state)) {
                        updateConnectionStatus(BLUETOOTH_ON);
                    } else {
                        updateConnectionStatus(BLUETOOTH_OFF);
                    }
                    return;
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
