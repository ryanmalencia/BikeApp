package automation.ryanm.bikeapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import java.util.Set;

public class BluetoothController {

    private BluetoothAdapter bluetoothAdapter;

    public BluetoothController(BluetoothAdapter adapter) throws Exception{
        if(adapter == null) {
            throw new Exception();
        }
    }

    public Set<BluetoothDevice> getDevices() {
        return bluetoothAdapter.getBondedDevices();
    }
}
