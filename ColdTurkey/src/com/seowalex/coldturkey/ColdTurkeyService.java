package com.seowalex.coldturkey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
	
public class ColdTurkeyService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks, com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener {
	private final Handler handler = new Handler();
    private Runnable runnable;
    
    private Intent notificationIntent;
    private PendingIntent contentIntent;
    private Notification.Builder notification_builder;
    private Notification notification;
    private ActivityManager activity_manager;
    private PackageManager package_manager;
    private NotificationManager notification_manager;
    private String packageName;
    private ArrayList<ApplicationInfo> packages;
    private String sorted_packages;
    
    private Calendar current_calendar;
    private Calendar start_datetime_calendar;
    private Calendar end_datetime_calendar;
    private ArrayList<ApplicationInfo> selected_applications;
    
    private HttpResponse response;
	private HttpClient client;
	private HttpPost request;
	private AsyncTask<Void, Void, Void> task;
	private JSONObject json_object;
	private HashMap<String, Object> schedule;
	private ArrayList<HashMap<String, Object>> schedules;
    
    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient google_api_client;
    private boolean intent_in_progress;
    
    @Override
    public void onCreate() {
    	google_api_client = new GoogleApiClient.Builder(this)
    							.addConnectionCallbacks(this)
    							.addOnConnectionFailedListener(this)
    							.addApi(Plus.API)
    							.addScope(Plus.SCOPE_PLUS_LOGIN)
    							.build();
    	
    	schedules = new ArrayList<HashMap<String, Object>>();
    }
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	if (!intent.getBooleanExtra("application_start", false)) {
    		google_api_client.connect();
    	}
    	
    	notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
		contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		
		notification_manager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
		notification_builder = new Notification.Builder(getApplicationContext());
		
		notification_builder.setContentIntent(contentIntent)
			.setContentTitle("Cold Turkey running")
			.setContentText("No applications blocked")
			.setSmallIcon(R.drawable.ic_stat_notify)
			.setLargeIcon((((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap()))
			.setOngoing(true)
			.setPriority(Notification.PRIORITY_LOW)
			.setShowWhen(false)
			.setStyle(new Notification.BigTextStyle().bigText("No applications blocked"))
			.setTicker("Cold Turkey started");
		
		notification = notification_builder.build();
		
		startForeground(1337, notification);
		
    	activity_manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
		package_manager = getApplicationContext().getPackageManager();
		
		runnable = new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				packageName = activity_manager.getRunningTasks(1).get(0).topActivity.getPackageName();
				packages = new ArrayList<ApplicationInfo>();
				sorted_packages = "Applications blocked: ";
				
				current_calendar = Calendar.getInstance(); 
				
				if (MainActivity.schedules != null) {
					for (HashMap<String, Object> schedule : MainActivity.schedules) {
						start_datetime_calendar = (Calendar) schedule.get("start_datetime_calendar");
						end_datetime_calendar = (Calendar) schedule.get("end_datetime_calendar");
						selected_applications = (ArrayList<ApplicationInfo>) schedule.get("selected_applications");
						
						if (current_calendar.after(start_datetime_calendar) && current_calendar.before(end_datetime_calendar)) {
							for (ApplicationInfo packageInfo : selected_applications) {
								if (packageName.equals(packageInfo.packageName)) {
									Intent intent = new Intent(getBaseContext(), StopActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
									intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
									startActivity(intent);
								}
								
								packages.add(packageInfo);
							}
						}
					}
					
					MainActivity.arrayAdapter.notifyDataSetChanged();
				}
				
				else if (schedules != null) {
					for (HashMap<String, Object> schedule : schedules) {
						start_datetime_calendar = (Calendar) schedule.get("start_datetime_calendar");
						end_datetime_calendar = (Calendar) schedule.get("end_datetime_calendar");
						selected_applications = (ArrayList<ApplicationInfo>) schedule.get("selected_applications");
						
						if (current_calendar.after(start_datetime_calendar) && current_calendar.before(end_datetime_calendar)) {
							for (ApplicationInfo packageInfo : selected_applications) {
								if (packageName.equals(packageInfo.packageName)) {
									Intent intent = new Intent(getBaseContext(), StopActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									startActivity(intent);
								}
								
								packages.add(packageInfo);
							}
						}
					}
				}
				
				Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(package_manager)); 
				
				for (ApplicationInfo packageInfo : packages) {
					try {
						sorted_packages += package_manager.getPackageInfo(packageInfo.packageName, 0).applicationInfo.loadLabel(package_manager).toString() + ", ";
					}
					
					catch (NameNotFoundException e) {
						return;
					}
				}
				
				if (!packages.isEmpty()) {
					sorted_packages = sorted_packages.substring(0, sorted_packages.length() - 2);
					notification_builder.setContentText(sorted_packages)
						.setStyle(new Notification.BigTextStyle().bigText(sorted_packages));
				}
				
				else {
					notification_builder.setContentText("No applications blocked")
						.setStyle(new Notification.BigTextStyle().bigText("No applications blocked"));
				}
			
				notification_manager.notify(1337, notification_builder.build());
				
				handler.postDelayed(runnable, 1000);
			}
		};
		
		handler.postDelayed(runnable, 1);
		
		return START_STICKY;
	}
    
    @Override
    public void onDestroy() {
    	if (google_api_client.isConnected()) {
    		google_api_client.disconnect();
    	}
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onConnectionFailed(ConnectionResult result) {
		if (!intent_in_progress && result.hasResolution()) {
			try {
				intent_in_progress = true;
				result.startResolutionForResult(MainActivity.activity, RC_SIGN_IN);
			}
			
			catch (SendIntentException e) {
				intent_in_progress = false;
				google_api_client.connect();
			}
		}
	}
	
	public void onConnected(Bundle connectionHint) {		
		response = null;
		
		task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					client = new DefaultHttpClient();
				    request = new HttpPost("https://cold-turkey.appspot.com/_ah/api/getSchedules/v1/schedules");
			    	response = client.execute(request);   
					
					try {
						json_object = new JSONObject(inputStreamToString(response.getEntity().getContent()).toString());
						
						JSONArray items = json_object.getJSONArray("items");
						
						for (int i = 0; i < items.length(); i++) {
							Person currentPerson = Plus.PeopleApi.getCurrentPerson(google_api_client);
							
							if (items.getJSONObject(i).getString("schedule_id").equals(currentPerson.getId())) {
								String[] package_names = items.getJSONObject(i).getString("schedule_packages").split(",");
								selected_applications = new ArrayList<ApplicationInfo>();
								
								for (int j = 0; j < package_names.length; j++) {
									try {
										selected_applications.add(package_manager.getApplicationInfo(package_names[j], 0));
									}
									
									catch (NameNotFoundException e) {
										e.printStackTrace();
									}
								}
								
								long start_datetime = Long.parseLong(items.getJSONObject(i).getString("schedule_start_datetime"));
								long end_datetime = Long.parseLong(items.getJSONObject(i).getString("schedule_end_datetime"));
								
								Calendar start_datetime_calendar = Calendar.getInstance();
								Calendar end_datetime_calendar = Calendar.getInstance();
								
								Date start_datetime_date = new Date(start_datetime);
								Date end_datetime_date = new Date(end_datetime);
								
								start_datetime_calendar.setTime(start_datetime_date);
								end_datetime_calendar.setTime(end_datetime_date);
								
								schedule = new HashMap<String, Object>();
								schedule.put("selected_applications", selected_applications);
								schedule.put("start_datetime_calendar", start_datetime_calendar);
								schedule.put("end_datetime_calendar", end_datetime_calendar);
								
								current_calendar = Calendar.getInstance(); 
								
								if (current_calendar.after(start_datetime_calendar) && current_calendar.before(end_datetime_calendar)) {
									schedules.add(schedule);
								}
							}
						}
					}
					
					catch (JSONException e) {
						e.printStackTrace();
					}
			    }
			    
			    catch (ClientProtocolException e) {
			    	e.printStackTrace();
			    }
			    
			    catch (IOException e) {
			    	e.printStackTrace();
			    }
			    
			    catch (IllegalStateException e) {
					e.printStackTrace();
				}
				
				return null;
			}
		};
		
		task.execute();
	}
	
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if (requestCode == RC_SIGN_IN) {
			intent_in_progress = false;
			
			if (!google_api_client.isConnecting()) {
				google_api_client.connect();
			}
		}
	}
	
	public void onConnectionSuspended(int cause) {
		google_api_client.connect();
	}
	
	@Override
	public void onDisconnected() {
		// Also nothing here atm
	}
	
	private StringBuilder inputStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

	    try {
			while ((line = rd.readLine()) != null) { 
			    total.append(line); 
			}
		}
	    
	    catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return total;
	}
}
