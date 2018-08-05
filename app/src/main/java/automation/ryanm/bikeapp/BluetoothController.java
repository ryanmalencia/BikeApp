package automation.ryanm.bikeapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

public class BluetoothController {

    private BluetoothAdapter bluetoothAdapter;

    public BluetoothController(BluetoothAdapter adapter) throws Exception{
        if(adapter == null) {
            Log.e("BTController","Null adapter");
            throw new Exception();
        }
        bluetoothAdapter = adapter;
    }

    public Set<BluetoothDevice> getDevices() {
        return bluetoothAdapter.getBondedDevices();
    }
}
