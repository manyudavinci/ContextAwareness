package com.example.tbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceived extends BroadcastReceiver {

	private static final String TAG = "NumberReceived";

	@Override
	public void onReceive(Context c, Intent intent) {
		
		if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) 
			return;

		Bundle bundle = intent.getExtras(); // ---get the SMS message passed in---

		if (bundle == null) 
			return;

		try {

			String myNumber = LoginActivity.myNumber;
			String myMsg = LoginActivity.myMsg;
			

			Object[] pdus = (Object[]) bundle.get("pdus");			
			SmsMessage[] msgs = new SmsMessage[pdus.length];
			
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				
				String msg_from = msgs[i].getOriginatingAddress().toString();
				String msgBody = msgs[i].getMessageBody().toString();
				
				if(msgBody.equals(myMsg))
					abortBroadcast(); // never show the random numbers
								
				Log.d(TAG, "SMS Message from:"+msg_from);
				
				// String compare between the received number with the registered number				
				if (msgBody.equals(myMsg) && myNumber.equals(msg_from))				
					LoginActivity.isVerified = true;
			}			
		} catch (Exception e) {
			Log.d(TAG,e.getMessage());
		}
	}	
}
