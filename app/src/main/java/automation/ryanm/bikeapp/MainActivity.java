package automation.ryanm.bikeapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import automation.ryanm.bikeapp.Controllers.*;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private BluetoothDevice mainDevice = null;
    private int REQUEST_ENABLE_BT = 1;
    private String uuidString = "f2801eef-31c3-4eb7-a95f-e635ada1fab4";
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean bluetoothEnabled = false;

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
                Log.d("BT","Bluetooth granted");
                bluetoothEnabled = true;
            }
            else {
                Log.d("BT","Bluetooth denied");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
        bluetoothEnabled = bluetoothAdapter.isEnabled();
        if(!bluetoothEnabled) {

            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
        }


        if(bluetoothEnabled) {
            try {
                BluetoothController controller = new BluetoothController(bluetoothAdapter);
                Set<BluetoothDevice> pairedDevices = controller.getDevices();

                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        Log.d("bt", device.getName());
                        if (device.getName().equals("raspberrypi")) {
                            mainDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
                            break;
                        }
                    }
                }
                BluetoothSocket socket = null;
                try {
                    if(mainDevice != null) {
                        socket = mainDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuidString));
                    }
                    if(socket != null) {
                        socket.connect();
                    }
                }
                catch(IOException e) {
                    Log.e("BT", "Error creating socket");
                }

            } catch (Exception e) {
                Log.e("BT", "Cannot pass null adapter");
            }
        }
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}