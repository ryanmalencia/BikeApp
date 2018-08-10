package automation.ryanm.bikeapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;

import automation.ryanm.bikeapp.Controllers.BluetoothController;

public class MainActivity extends AppCompatActivity implements IAsyncListener {

    private TextView message;
    TextView errorMessage;
    private ProgressBar connectStatus;
    private Button startStop;
    private int REQUEST_ENABLE_BT = 1;
    private int currentStatus = RESULT_OK;
    String uuidString = "f2801eef-31c3-4eb7-a95f-e635ada1fab4";
    BluetoothAdapter bluetoothAdapter = null;
    private boolean bluetoothEnabled = false;
    private BluetoothController controller;
    private boolean running = false;
    private final int MESSAGE_RECEIVED = 1055;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            return false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK) {
                Log.d("MainActivity","Bluetooth granted");
                bluetoothEnabled = true;
            }
            else {
                Log.e("MainActivity","Bluetooth denied");
            }
        }
    }

    private void Close() {
        if(controller != null) {
            controller.Close();
        }
    }

    private void retryBluetooth() {
        if(controller == null) {
            return;
        }
        bluetoothEnabled = bluetoothAdapter.isEnabled();
        if(!bluetoothEnabled) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = controller.getDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d("bt", device.getName());
                if (device.getName().equals("raspberrypi")) {
                    controller.setActiveDevice(bluetoothAdapter.getRemoteDevice(device.getAddress()));
                    break;
                }
            }
        }
        if(controller.getActiveDevice() != null) {
            try {
                controller.setSocket(controller.getActiveDevice().createRfcommSocketToServiceRecord(UUID.fromString(uuidString)));
            }
            catch(IOException e) {
                Log.e("MainActivity", "Could not create bluetooth socket");
            }
        }
        if(controller.getSocket() != null) {
            controller.Connect();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        errorMessage = findViewById(R.id.bluetooth_error);
        connectStatus = findViewById(R.id.connect_status);
        startStop = findViewById(R.id.startStop);
        message = findViewById(R.id.message);
        bluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        try {
            controller = new BluetoothController(bluetoothAdapter);
            controller.setConnectListener(this);
        }
        catch(Exception e) {
            Log.e("MainActivity", "Cannot pass null to controller");
        }
        bluetoothEnabled = bluetoothAdapter.isEnabled();
        if(!bluetoothEnabled) {

            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }
        if(bluetoothEnabled) {
            Set<BluetoothDevice> pairedDevices = controller.getDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    Log.d("bt", device.getName());
                    if (device.getName().equals("raspberrypi")) {
                        controller.setActiveDevice(bluetoothAdapter.getRemoteDevice(device.getAddress()));
                        break;
                    }
                }
            }
            if(controller.getActiveDevice() != null) {
                try {
                    controller.setSocket(controller.getActiveDevice().createRfcommSocketToServiceRecord(UUID.fromString(uuidString)));
                }
                catch(IOException e) {
                    Log.e("MainActivity", "Could not create bluetooth socket");
                }
            }
            if(controller.getSocket() != null) {
                controller.Connect();

            }
        }
    }

    public void ActionComplete(int status, String message) {
        if(status == MESSAGE_RECEIVED) {
            this.message.setText(message);
            return;
        }
        if(status == RESULT_OK) {
            connectStatus.setVisibility(View.GONE);
            startStop.setEnabled(true);
            ConnectedThread thread = new ConnectedThread(controller, this);
            thread.start();
        }

        if(status != RESULT_OK && currentStatus != RESULT_CANCELED) {
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.startAnimation(AnimationUtils.loadAnimation(this.getApplicationContext(), R.anim.fadein));
            retryBluetooth();

        }
        else if(status != RESULT_OK) {
            retryBluetooth();
        }
        else if(currentStatus == RESULT_CANCELED){
            errorMessage.startAnimation(AnimationUtils.loadAnimation(this.getApplicationContext(), R.anim.fadeout));
            errorMessage.setVisibility(View.INVISIBLE);
        }
        else {
            return;
        }
        currentStatus = status;
    }

    public void toggleRunning(View view) {
        if(!running) {
            controller.sendData("1");
            startStop.setText("STOP");
        }
        else {
            controller.sendData("0");
            startStop.setText("START");
        }
        running = !running;
    }

    private class ConnectedThread extends Thread {
        private final BluetoothController controller;
        private byte[] buffer;
        private IAsyncListener asyncListener = null;

        public ConnectedThread(BluetoothController controller, IAsyncListener listener) {
            this.controller = controller;
            this.asyncListener = listener;
        }

        public void run() {
            buffer = new byte[1024];
            int numBytes = 0;
            InputStream is = null;
            is = controller.getInputStream();
            while(true) {
                try {
                    numBytes = is.read(buffer);
                    String message = new String(buffer);
                    double value = Double.parseDouble(message);
                    value = 81.68141/value * 0.05682;
                    DecimalFormat df = new DecimalFormat("##.##");
                    Log.e("TEST",message);
                    asyncListener.ActionComplete(MESSAGE_RECEIVED, df.format(value) + " MPH");
                }
                catch(IOException e) {

                }
            }
        }
    }
}