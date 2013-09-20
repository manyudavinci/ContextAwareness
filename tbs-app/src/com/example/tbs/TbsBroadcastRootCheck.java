package com.example.tbs;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TbsBroadcastRootCheck extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {

		if (isRooted()) {

			i = new Intent(c, DisableSense.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("alert_type", "7");
			c.startActivity(i);

		}
	}

	public static boolean isRooted() {

		// get from build info
		String buildTags = android.os.Build.TAGS;
		if (buildTags != null && buildTags.contains("test-keys")) {
			return true;
		}

		// check if /system/app/Superuser.apk is present
		try {
			File file = new File("/system/app/Superuser.apk");
			if (file.exists()) {
				return true;
			}
		} catch (Exception e1) {
			// ignore
		}

		// try executing commands
		return canExecuteCommand("/system/xbin/which su")
				|| canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
	}

	// executes a command on the system
	private static boolean canExecuteCommand(String command) {
		boolean executedSuccesfully;
		try {
			Runtime.getRuntime().exec(command);
			executedSuccesfully = true;
		} catch (Exception e) {
			executedSuccesfully = false;
		}

		return executedSuccesfully;
	}

}
