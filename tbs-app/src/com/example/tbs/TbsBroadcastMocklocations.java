package com.example.tbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class TbsBroadcastMocklocations extends BroadcastReceiver {

	private static final String TAG = "TbsBroadcastMocklocations";

	public void onReceive(Context c, Intent i) {
		Log.d(TAG, "Received broadcast");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

			if (!(Settings.Secure.getString(c.getContentResolver(),
					Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))) {

				Vibrator vi = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
				vi.vibrate(1000);
				Toast.makeText(c, c.getString(R.string.stat_notify_disable_mock_locations)
, Toast.LENGTH_LONG).show();

				i = new Intent(c, DisableSense.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("alert_type", "4");
				c.startActivity(i);
			}

		}

		else {
			Toast.makeText(c, c.getString(R.string.stat_notify_disable_mock_locations)
, Toast.LENGTH_LONG).show();
		}
	}
}
