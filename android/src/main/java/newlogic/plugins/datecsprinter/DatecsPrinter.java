package newlogic.plugins.datecsprinter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.datecs.printer.ProtocolAdapter;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PluginCall;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatecsPrinter {

    @Nullable
    private AppCompatActivity activity;

    private BroadcastReceiver bluetoothStateReceiver;
    private BroadcastReceiver searchBluetoothReceiver;
    private Printer mPrinter;
    private ProtocolAdapter mProtocolAdapter;
    private BluetoothSocket mBluetoothSocket;

    private final ProtocolAdapter.PrinterListener mChannelListener = new ProtocolAdapter.PrinterListener() {
        @Override
        public void onPaperStateChanged(boolean hasNoPaper) {
            if (hasNoPaper) {
                Toast.makeText(activity, "NO_PAPER", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "PAPER_OK", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onThermalHeadStateChanged(boolean overheated) {
            if (overheated) {
                closeActiveConnections();
                Toast.makeText(activity, "OVERHEATING", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onBatteryStateChanged(boolean lowBattery) {
            Toast.makeText(activity, "LOW_BATTERY", Toast.LENGTH_SHORT).show();
        }
    };

    public DatecsPrinter(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void setBLStateReceiver(BroadcastReceiver receiver) {
        this.bluetoothStateReceiver = receiver;
    }

    public void setSearchBluetoothReceiverReceiver(BroadcastReceiver receiver) {
        this.searchBluetoothReceiver = receiver;
    }

    @SuppressLint("MissingPermission")
    public void startMonitoring(@NonNull AppCompatActivity activity) {
        IntentFilter searchBluetoothFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);

        activity.registerReceiver(bluetoothStateReceiver, filter);
        activity.registerReceiver(searchBluetoothReceiver, searchBluetoothFilter);
    }

    public void stopMonitoring(@NonNull AppCompatActivity activity) {
        activity.unregisterReceiver(bluetoothStateReceiver);
        activity.unregisterReceiver(searchBluetoothReceiver);
    }

    @SuppressLint("MissingPermission")
    public void startScanBluetoothDevice(PluginCall call) {
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.startDiscovery();
            call.resolve();
        } catch (Exception e) {
            call.reject(String.valueOf(e), e);
            Logger.error(String.valueOf(e));
        }
    }

    @SuppressLint("MissingPermission")
    public void getBluetoothPairedDevices(PluginCall call) {
        BluetoothAdapter mBluetoothAdapter = null;
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                JSONArray json = new JSONArray();
                JSObject ret = new JSObject();
                ret.put("data", json);
                call.resolve(ret);
                return;
            }

            @SuppressLint("MissingPermission")
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                JSONArray json = new JSONArray();
                for (BluetoothDevice device : pairedDevices) {
                    Hashtable map = new Hashtable();
                    int deviceType = 0;
                    try {
                        Method method = device.getClass().getMethod("getType");
                        if (method != null) {
                            deviceType = (Integer) method.invoke(device);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    map.put("type", deviceType);
                    map.put("address", device.getAddress());
                    map.put("name", device.getName());
                    String deviceAlias = device.getName();
                    try {
                        Method method = device.getClass().getMethod("getAliasName");
                        if (method != null) {
                            deviceAlias = (String) method.invoke(device);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    map.put("aliasName", deviceAlias);
                    JSONObject jObj = new JSONObject(map);
                    json.put(jObj);
                }

                JSObject ret = new JSObject();
                ret.put("data", json);
                call.resolve(ret);
            } else {
                JSONArray json = new JSONArray();
                JSObject ret = new JSObject();
                ret.put("data", json);
                call.resolve(ret);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONArray json = new JSONArray();
            JSObject ret = new JSObject();
            ret.put("data", json);
            call.resolve(ret);
        }
    }

    private void closeActiveConnections() {
        /**
         * Close printer connection
         */
        if (mPrinter != null) {
            mPrinter.close();
        }

        if (mProtocolAdapter != null) {
            mProtocolAdapter.close();
        }

        /**
         * Close bluetooth connection
         */
        BluetoothSocket socket = mBluetoothSocket;
        mBluetoothSocket = null;
        if (socket != null) {
            try {
                Thread.sleep(50);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device, UUID uuid, final PluginCall call) throws IOException {
        try {
            Method method = device.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[] { UUID.class });
            return (BluetoothSocket) method.invoke(device, uuid);
        } catch (Exception e) {
            e.printStackTrace();
            call.reject("ERR_BT_SOCKET");
        }
        return device.createRfcommSocketToServiceRecord(uuid);
    }

    private void initializePrinter(InputStream inputStream, OutputStream outputStream, PluginCall call) throws IOException {
        mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);
        if (mProtocolAdapter.isProtocolEnabled()) {
            mProtocolAdapter.setPrinterListener(mChannelListener);

            final ProtocolAdapter.Channel channel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);

            mPrinter = new Printer(channel.getInputStream(), channel.getOutputStream());
        } else {
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(), mProtocolAdapter.getRawOutputStream());
        }

        mPrinter.setConnectionListener(
            new Printer.ConnectionListener() {
                @Override
                public void onDisconnect() {}
            }
        );
    }

    @SuppressLint("MissingPermission")
    private void establishBluetoothConnection(final String address, final PluginCall call) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(address);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        InputStream in = null;
        OutputStream out = null;
        adapter.cancelDiscovery();

        try {
            mBluetoothSocket = createBluetoothSocket(device, uuid, call);
            Thread.sleep(50);
            mBluetoothSocket.connect();
            in = mBluetoothSocket.getInputStream();
            out = mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            //fallback
            try {
                mBluetoothSocket =
                    (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] { int.class }).invoke(device, 1);
                Thread.sleep(50);
                mBluetoothSocket.connect();
                in = mBluetoothSocket.getInputStream();
                out = mBluetoothSocket.getOutputStream();
            } catch (Exception ex) {
                ex.printStackTrace();

                JSObject ret = new JSObject();
                ret.put("status", "FAILED_TO_CONNECT_1");
                ret.put("error", String.valueOf(ex));
                call.resolve(ret);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();

            JSObject ret = new JSObject();
            ret.put("status", "FAILED_TO_CONNECT_2");
            ret.put("error", String.valueOf(e));
            call.resolve(ret);
            return;
        }

        try {
            initializePrinter(in, out, call);

            JSObject ret = new JSObject();
            ret.put("status", "PRINTER_CONNECTED");
            call.resolve(ret);
        } catch (IOException e) {
            e.printStackTrace();

            JSObject ret = new JSObject();
            ret.put("status", "FAILED_TO_INITIALIZE");
            ret.put("error", String.valueOf(e));
            call.resolve(ret);
            return;
        }
    }

    public void connect(String address, PluginCall call) {
        closeActiveConnections();
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            establishBluetoothConnection(address, call);
        }
    }

    public void printTaggedText(String text, PluginCall call) {
        try {
            mPrinter.printTaggedText(text, "ISO-8859-1");
            mPrinter.flush();

            JSObject ret = new JSObject();
            ret.put("status", "PRINT_SUCCESS");
            ret.put("content", text);
            call.resolve(ret);
        } catch (Exception e) {
            e.printStackTrace();

            JSObject ret = new JSObject();
            ret.put("status", "PRINT_FAILED");
            ret.put("error", String.valueOf(e));
            call.resolve(ret);
        }
    }
}
