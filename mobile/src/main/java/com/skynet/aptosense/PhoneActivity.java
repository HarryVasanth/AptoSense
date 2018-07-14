package com.skynet.aptosense;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skynet.aptosense.services.WearListenerService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PhoneActivity extends Activity implements View.OnClickListener {
    // Initialize views and components
    TextView txtHRate, txtGyro, txtAcc, txtWearStatus, txtDIP;
    ImageView imgWear;
    WearListenerService wearListenerService = new WearListenerService();
    Timer udpTimer;
    ParseData parseData;
    int frequency = 1;
    String filename = "aptoSense_" + new SimpleDateFormat("yyyyMMdd_HHmm'.csv'").format(new Date());

    int UDP_SERVER_PORT = 11111;
    private String ipAddress = "127.0.0.1";

    public PhoneActivity() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        txtWearStatus = (TextView) findViewById(R.id.txtWearStatus);
        imgWear = (ImageView) findViewById(R.id.imgWear);

        txtHRate = (TextView) findViewById(R.id.txtHRate);
        txtGyro = (TextView) findViewById(R.id.txtGyro);
        txtAcc = (TextView) findViewById(R.id.txtAcc);
        txtDIP = (TextView) findViewById(R.id.txtDIP);

        udpTimer = new Timer();
        parseData = new ParseData();
        udpTimer.scheduleAtFixedRate(parseData, 0, 1000 / frequency);

    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void btnConfig(View view) {
       // Query user for UDP port
        AlertDialog.Builder builderUDP = new AlertDialog.Builder(this);
        builderUDP.setTitle("Enter the target UDP address");

        // Set up the input
        final EditText inputUDP = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputUDP.setInputType(InputType.TYPE_CLASS_NUMBER);
        builderUDP.setView(inputUDP);

        // Set up the buttons
        builderUDP.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UDP_SERVER_PORT = Integer.parseInt(inputUDP.getText().toString());
                txtDIP.setText(String.valueOf(ipAddress) + ":" + String.valueOf(UDP_SERVER_PORT));
            }
        });
        builderUDP.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderUDP.show();

        // Query user for IP Address
        AlertDialog.Builder builderIP = new AlertDialog.Builder(this);
        builderIP.setTitle("Enter the target IP address");

        // Set up the input
        final EditText inputIP = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputIP.setInputType(InputType.TYPE_CLASS_TEXT);
        builderIP.setView(inputIP);

        // Set up the buttons
        builderIP.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ipAddress = inputIP.getText().toString();
                txtDIP.setText(String.valueOf(ipAddress) + ":" + String.valueOf(UDP_SERVER_PORT));
            }
        });
        builderIP.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderIP.show();
        //Toast.makeText(getApplicationContext(), "Configuring IP", Toast.LENGTH_SHORT).show();
    }

    public void btnExit(View view) {
        this.finish();
        System.exit(0);
    }

    public void btnInfo(View view) {
        Toast.makeText(getApplicationContext(), getString(R.string.android_system), Toast.LENGTH_LONG).show();
    }

    ///////////////////////////////
    //////// File Writer /////////
    //////////////////////////////

    public void writeData(String wearData) {

        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AptoSense_data");

        dir.mkdir();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.SSS").format(new Date());

        File sessionFile = new File(dir, filename);
        if (!sessionFile.exists()) {
            try {
                sessionFile.createNewFile();
                BufferedWriter buf = new BufferedWriter(new FileWriter(sessionFile, true));
                buf.append("date,time,hr,gyro_x,gyro_y,gyro_z,acc_x,acc_y,acc_z");
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }
        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(sessionFile, true));
            buf.append(timeStamp + "," + wearData);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }

    /////////////////////////////
    //////// UDP Sender /////////
    /////////////////////////////


    public void sendData(String wearData) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss.SSS").format(new Date());


        String udpMsg = wearData;
        Log.d("UDP Sent: ", "WEAR: " + wearData);

        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(ipAddress);

            DatagramPacket dp;
            dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, UDP_SERVER_PORT);
            ds.send(dp);

            Log.d("UDP Sent: ", udpMsg + " via " + UDP_SERVER_PORT + " to: " + serverAddr);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
    }

    ///////////////////////////////
    //////// Data Parser /////////
    //////////////////////////////

    class ParseData extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtHRate.setText(wearListenerService.gethRate());
                    txtGyro.setText(wearListenerService.getGyroXYZ());
                    txtAcc.setText(wearListenerService.getAccXYZ());

                    if (wearListenerService.gethRate().equals("00")) {
                        imgWear.setImageResource(R.drawable.hr_off);
                        txtWearStatus.setText("Disconnected");
                        txtWearStatus.setTextColor(Color.parseColor("#ffffff"));
                    } else {
                        imgWear.setImageResource(R.drawable.hr_on);
                        txtWearStatus.setText("Connected");
                        txtWearStatus.setTextColor(Color.parseColor("#000000"));

                    }
                }
            });
            if (txtWearStatus.getText().toString().equals("Connected")) {

                writeData(wearListenerService.gethRate() + "," + wearListenerService.getGyroXYZ() + "," + wearListenerService.getAccXYZ());
                sendData(wearListenerService.gethRate() + "," + wearListenerService.getGyroXYZ() + "," + wearListenerService.getAccXYZ());
            }
        }
    }
}



