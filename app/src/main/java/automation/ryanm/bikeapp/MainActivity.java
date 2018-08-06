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
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import automation.ryanm.bikeapp.Controllers.BluetoothController;

public class MainActivity extends AppCompatActivity implements IAsyncListener {

    private TextView mTextMessage;
    TextView errorMessage;
    private ProgressBar connectStatus;
    private int REQUEST_ENABLE_BT = 1;
    private int currentStatus = RESULT_OK;
    String uuidString = "f2801eef-31c3-4eb7-a95f-e635ada1fab4";
    BluetoothAdapter bluetoothAdapter = null;
    private boolean bluetoothEnabled = false;
    private BluetoothController controller;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
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
        bluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();mTextMessage = findViewById(R.id.message);
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

    public void ActionComplete(int status) {

        if(status == RESULT_OK) {
            connectStatus.setVisibility(View.GONE);
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
}