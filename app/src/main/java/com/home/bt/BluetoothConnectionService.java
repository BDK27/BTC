package com.home.bt;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private  static final String TAG="BluetoothConnectionService";

    private static final String appName="MYAPP";

    private static final UUID MY_UUID_INSECURE=
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private   AcceptThread mInsecureAcceptThread;

    private  ConnectThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private  ConnectedThread mConnectedThread;
    /*****************************************************************************************************/
    public BluetoothConnectionService(Context context,BluetoothAdapter bluetoothAdapter){
        mContext=context;
        mBluetoothAdapter=bluetoothAdapter;
    }
    /*****************************************************************************************************/
    private class AcceptThread extends Thread{
        private final BluetoothServerSocket mmServerSocket;

        /*****************************************************************************************************/

        @SuppressLint("LongLogTag")
        public AcceptThread(){
            BluetoothServerSocket tmp=null;
            try{
                tmp=mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,MY_UUID_INSECURE);
                Log.d(TAG,"AcceptThread:Setting up Server using: "+MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG,"AcceptThread: IOException: "+e.getMessage());
            }
            mmServerSocket=tmp;
        }

        /*****************************************************************************************************/

        @SuppressLint("LongLogTag")
        public void run(){
            Log.d(TAG,"run: AcceptThread Running.");

            BluetoothSocket socket=null;

            try{
                Log.d(TAG,"run: RFCOM server socket start...");
                socket=mmServerSocket.accept();;
                Log.d(TAG,"run: RFCOM server socket accepted connection.");

            }catch(IOException e){
                Log.e(TAG,"AcceptThread: IOException: "+e.getMessage());
            }

            if(socket!=null){
                connected(socket,mmDevice);
            }
            Log.i(TAG,"END mAcceptThread");
        }

        /*****************************************************************************************************/

        @SuppressLint("LongLogTag")
        public void cancel(){
            Log.i(TAG,"cancel: Canceling AcceptThread.");
            try{
                mmServerSocket.close();
            }catch (IOException e){
                Log.e(TAG,"cancel:Close of AcceptThread ServerSocket failed."+e.getMessage());
            }
        }
    }
    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        /*****************************************************************************************************/

        @SuppressLint("LongLogTag")
        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG,"ConnectThread: started.");
            mmDevice=device;
            deviceUUID=uuid;
        }

        /*****************************************************************************************************/
        @SuppressLint("LongLogTag")
        public void run(){
            BluetoothSocket tmp=null;
            Log.i(TAG,"RUN mConnectThread");
            try{
                Log.d(TAG,"ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE);
                tmp=mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }catch(IOException e){
                Log.e(TAG,"ConnectThread: Could not create InsecureRfcommSocket "+ e.getMessage());
            }

            mmSocket=tmp;

            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                Log.d(TAG,"run: ConnectThread connected.");
            } catch (IOException e) {
                try{
                    mmSocket.close();
                    Log.d(TAG,"run: Closed Socket.");
                }catch(IOException e1){
                    Log.e(TAG,"mConnectThread: run: Unable to close connection in socket"+ e1.getMessage());
                }
                Log.d(TAG,"run: ConnectThread: Could not connect to UUID: "+ MY_UUID_INSECURE);
            }
            connected(mmSocket,mmDevice);
        }

        /*****************************************************************************************************/
        @SuppressLint("LongLogTag")
        public void cancel(){
            try{
                Log.d(TAG,"cancel: Closing Client Socket.");
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG,"cancel: close() of mmSocket in Connectthewad failed. "+e.getMessage());
            }
        }

        /*****************************************************************************************************/
        @SuppressLint("LongLogTag")
        public synchronized void start(){
            Log.d(TAG,"start");
            if(mConnectThread!=null){
                mConnectThread.cancel();
                mConnectThread=null;
            }
            if(mInsecureAcceptThread==null){
                mInsecureAcceptThread=new AcceptThread();
                mInsecureAcceptThread.start();
            }
        }
    }
    /*****************************************************************************************************/

    @SuppressLint("LongLogTag")
    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG,"startClient: Started.");
        mProgressDialog=ProgressDialog.show(mContext,"Connecting Bluetooth"
                ,"Please Wait...",true);
        mConnectThread=new ConnectThread(device,uuid);
        mConnectThread.start();
    }

    /*****************************************************************************************************/
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStrem;
        private final OutputStream mmOutStream;

        /*****************************************************************************************************/
        @SuppressLint("LongLogTag")
        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG,"ConnectedThread: Starting.");

            mmSocket=socket;
            InputStream tmpIn=null;
            OutputStream tmpOut=null;

            mProgressDialog.dismiss();

            try{
                tmpIn=mmSocket.getInputStream();
                tmpOut=mmSocket.getOutputStream();
            }catch(IOException e){
                e.printStackTrace();
            }

            mmInStrem=tmpIn;
            mmOutStream=tmpOut;
        }

        /*****************************************************************************************************/

        @SuppressLint("LongLogTag")
        public void run(){
            byte[] buffer=new byte[1024];
            int bytes;

            while(true){
                try{
                    bytes=mmInStrem.read(buffer);
                    String incomingMessage=new String(buffer,0,bytes);
                    Log.d(TAG,"InputStream: "+ incomingMessage);
                }catch(IOException e){
                    Log.d(TAG,"write: Error reading inputstream: "+ e.getMessage());
                    break;
                }
            }
        }

        /*****************************************************************************************************/

        @SuppressLint("LongLogTag")
        public void write(byte[]bytes){
            String text=new String(bytes, Charset.defaultCharset());
            Log.d(TAG,"write: Writing to outputstream: "+ text);
            try{
                mmOutStream.write(bytes);
            }catch(IOException e){
                Log.d(TAG,"write: Error Writing to outputstream: "+ e.getMessage());
            }
        }

        /*****************************************************************************************************/

        public void cancel(){
            try{
                mmSocket.close();
            }catch(IOException e){}
        }
    }
    @SuppressLint("LongLogTag")
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG,"connected: Starting.");

        mConnectedThread=new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    @SuppressLint("LongLogTag")
    public void write(byte[] out){
        ConnectThread r;

        Log.d(TAG,"write: Write Called.");
        mConnectedThread.write(out);
    }
}
