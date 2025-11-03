package com.expensetrackerapp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

public class SmsListenerModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    private static ReactApplicationContext sReactContext;

    public SmsListenerModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
        sReactContext = context;

        Log.d("SmsListenerModule", "Constructed, registering SMS receiver");
        registerSMSReceiver();
    }

    @Override
    public String getName() {
        return "SmsListenerModule";
    }

    private void sendEvent(String eventName, WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    public static void emitFromReceiver(WritableMap params){
        try{
            if(sReactContext != null){
                sReactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onSMSReceived", params);
                Log.d("SmsListenerModule", "Event onSMSReceived emitted to JS from manifest receiver");
            } else {
                Log.d("SmsListenerModule", "React context is null in emitFromReceiver");
            }
        }catch(Exception e){
            Log.d("SmsListenerModule", "emitFromReceiver error: " + e.getMessage());
        }
    }

    private void registerSMSReceiver() {
        BroadcastReceiver smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("SmsListenerModule", "onReceive called for action: " + intent.getAction());
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Object[] pdus = (Object[]) extras.get("pdus");
                    if (pdus == null) {
                        Log.d("SmsListenerModule", "No PDUs found in intent extras");
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

                    Log.d("SmsListenerModule", "SMS from: " + senderPhoneNumber + " body: " + fullMessage);
                    WritableMap params = Arguments.createMap();
                    params.putString("messageBody", fullMessage.toString());
                    params.putString("senderPhoneNumber", senderPhoneNumber);
                    params.putDouble("timestamp", (double) timestamp);

                    sendEvent("onSMSReceived", params);
                    Log.d("SmsListenerModule", "Event onSMSReceived emitted to JS");
                }
            }
        };

        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(1000);
        this.reactContext.registerReceiver(smsReceiver, filter);
        Log.d("SmsListenerModule", "SMS receiver registered");
    }

    @ReactMethod
    public void startListeningToSMS() {
        registerSMSReceiver();
    }
}