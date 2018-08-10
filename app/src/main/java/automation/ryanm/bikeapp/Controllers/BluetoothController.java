package automation.ryanm.bikeapp.Controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import automation.ryanm.bikeapp.IAsyncListener;

public class BluetoothController {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket = null;
    private BluetoothDevice activeDevice = null;
    private IAsyncListener asyncListener = null;

    public BluetoothController(BluetoothAdapter adapter) throws Exception {
        if(adapter == null) {
            Log.e("BTController","Null adapter");
            throw new Exception();
        }
        bluetoothAdapter = adapter;
    }

    public void sendData(String data) {
        try {
            OutputStream os = bluetoothSocket.getOutputStream();
            os.write(data.getBytes());
        }
        catch(IOException e) {
            Log.e("BTController", "Failed to write data");
        }
    }

    public InputStream getInputStream() {
        try {
            return bluetoothSocket.getInputStream();
        }
        catch(IOException e) {
            return new InputStream() {
                @Override
                public int read() {
                    return 0;
                }
            };
        }
    }


    public IAsyncListener setConnectListener(IAsyncListener listener) {
        asyncListener = listener;
        return asyncListener;
    }

    public BluetoothSocket setSocket(BluetoothSocket socket) {
        bluetoothSocket = socket;
        return bluetoothSocket;
    }
    public BluetoothSocket getSocket() {
        return bluetoothSocket;
    }

    public BluetoothDevice setActiveDevice(BluetoothDevice device) {
        activeDevice = device;
        return activeDevice;
    }

    public BluetoothDevice getActiveDevice() {
        return activeDevice;
    }

    public void Connect() {
        new Connect().execute();
    }

    public void Close() {
        if(bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            }
            catch(IOException e) {
                Log.e("BTController", "Unable to close socket");
            }
        }
    }

    public Set<BluetoothDevice> getDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    private class Connect extends AsyncTask<Void, Void, Integer> {
        protected void onPreExecute() {

        }

        protected Integer doInBackground(Void... params) {
            try {
                bluetoothSocket.connect();
            }
            catch(IOException e) {
                Log.e("BTController","Unable to connect");
                return 0;
            }
            return -1;
        }

        protected void onPostExecute(Integer result) {
            if(asyncListener != null) {
                asyncListener.ActionComplete(result, "");
            }
        }
    }
}
