package com.expensetrackerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            if(intent == null || intent.getAction() == null) return;
            if(!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

            Bundle extras = intent.getExtras();
            if (extras == null) return;

            Object[] pdus = (Object[]) extras.get("pdus");
            if (pdus == null) {
                Log.d("SmsBroadcastReceiver", "No PDUs in extras");
                return;
            }
            String format = extras.getString("format");

            StringBuilder fullMessage = new StringBuilder();
            String senderPhoneNumber = null;
            long timestamp = 0L;

            for (Object pdu : pdus) {
                SmsMessage sms = format != null
                        ? SmsMessage.createFromPdu((byte[]) pdu, format)
                        : SmsMessage.createFromPdu((byte[]) pdu);
                if (senderPhoneNumber == null) {
                    senderPhoneNumber = sms.getOriginatingAddress();
                }
                if (timestamp == 0L) {
                    timestamp = sms.getTimestampMillis();
                }
                fullMessage.append(sms.getMessageBody());
            }

            WritableMap params = Arguments.createMap();
            params.putString("messageBody", fullMessage.toString());
            params.putString("senderPhoneNumber", senderPhoneNumber);
            params.putDouble("timestamp", (double) timestamp);

            SmsListenerModule.emitFromReceiver(params);
        }catch(Exception e){
            Log.d("SmsBroadcastReceiver", "Error in onReceive: " + e.getMessage());
        }
    }
}


