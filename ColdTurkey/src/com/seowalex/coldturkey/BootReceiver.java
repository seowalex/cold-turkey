package com.seowalex.coldturkey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
	
public class BootReceiver extends BroadcastReceiver {
	private Intent service;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		service = new Intent(context, ColdTurkeyService.class);
		context.startService(service);
	}
}
