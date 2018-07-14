package com.skynet.aptosense.services;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WearListenerService extends WearableListenerService {
    public static String hRate = "00";
    public static String gyroXYZ = "XXX, YYY, ZZZ";
    public static String accXYZ = "XXX, YYY, ZZZ";

    public static String gethRate() {
        return hRate;
    }

    public static String getGyroXYZ() {
        return gyroXYZ;
    }

    public static String getAccXYZ() {
        return accXYZ;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String event = messageEvent.getPath();
        String[] wearMessage = event.split("--");
        if (wearMessage[0].toString().equals("HR")) {
            hRate = wearMessage[1].toString();
        }
        if (wearMessage[0].toString().equals("GYRO")) {

            gyroXYZ = wearMessage[1].toString();
        }
        if (wearMessage[0].toString().equals("ACC")) {
            accXYZ = wearMessage[1].toString();
        }
        if (wearMessage[0].equals("None")) {
            hRate = "00";
            gyroXYZ = "XXX, YYY, ZZZ";
        }
    }
}


