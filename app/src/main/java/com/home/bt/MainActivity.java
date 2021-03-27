package com.home.bt;

import androidx.appcompat.app.AppCompatActivity;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    Button i1;
    TextView t1;

    String address=null,name=null;

    BluetoothAdapter myBluetooth=null;
    BluetoothSocket btSocket=null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{setw();}catch(Exception e){}
    }

    private void findViews(){

        TextView result=findViewById(R.id.result);
    }

    private void setw()throws IOException{
        t1=(TextView)findViewById(R.id.textView);
        bluetooth_connect_device();
       // i1=(Button)findViewById(R.id.button);

        i1.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v, MotionEvent event){
                if(event.getAction()==MotionEvent.ACTION_DOWN){led_on_off("f");}
                if(event.getAction()==MotionEvent.ACTION_UP){led_on_off("b");}
                return true;}
            });
        }


    private void bluetooth_connect_device()throws IOException{
        try{
            myBluetooth=BluetoothAdapter.getDefaultAdapter();
            address=myBluetooth.getAddress();
            pairedDevices=myBluetooth.getBondedDevices();
            if(pairedDevices.size()>0){
                for(BluetoothDevice bt:pairedDevices){
                    address=bt.getAddress().toString();name=bt.getName().toString();
                    Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch(Exception we){}
        myBluetooth=BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice dispositivo=myBluetooth.getRemoteDevice(address);
        btSocket=dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
        btSocket.connect();
        try {t1.setText("BT Name: "+name+"\nBT Address: "+address);}
        catch (Exception e){}

    }
    private void led_on_off(String i){
        try {
            if(btSocket!=null){
                btSocket.getOutputStream().write(i.toString().getBytes());
            }
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }








}










