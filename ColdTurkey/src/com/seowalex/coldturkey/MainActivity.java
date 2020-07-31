package com.seowalex.coldturkey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {
	public static Activity activity;
	
	public static ArrayList<HashMap<String, Object>> schedules;
	private HashMap<String, Object> schedule;
	private String schedules_string;
	public static ArrayAdapter<HashMap<String, Object>> arrayAdapter;
	
	private ArrayList<ApplicationInfo> selected_applications;
	private Calendar start_datetime_calendar;
	private Calendar end_datetime_calendar;
	private SimpleDateFormat sdf_datetime;
	private PackageManager package_manager;
	private Calendar current_calendar;
	private ProgressDialog progress_dialog;
	
	private RelativeLayout relative_layout;
	private TextView no_schedules;
	
	private HttpResponse response;
	private HttpClient client;
	private HttpGet request;
	private Thread thread;
	private HttpPost post_request;
	private AsyncTask<Void, Void, Void> task;
	private JSONObject json_object;
	
	private static final int RC_SIGN_IN = 0;
	private GoogleApiClient google_api_client;
	private boolean intent_in_progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		activity = this;
		relative_layout = (RelativeLayout) findViewById(R.id.relative_layout);
		no_schedules = (TextView) findViewById(R.id.no_schedules);
		
		if (schedules == null) {
			schedules = new ArrayList<HashMap<String, Object>>();
		}
		
		else if (!schedules.isEmpty()) {			
			relative_layout.removeView(no_schedules);
		}
		
		Intent intent = new Intent(getBaseContext(), ColdTurkeyService.class);
		intent.putExtra("application_start", true);
		startService(intent);
		
		google_api_client = new GoogleApiClient.Builder(this)
								.addConnectionCallbacks(this)
								.addOnConnectionFailedListener(this)
								.addApi(Plus.API)
								.addScope(Plus.SCOPE_PLUS_LOGIN)
								.build();
		
		google_api_client.connect();
		
		ListView schedules_list = (ListView) findViewById(R.id.schedules_list);
		
		sdf_datetime = new SimpleDateFormat("dd MMMM yyyy, hh:mm aa", Locale.getDefault());
		
		arrayAdapter = new ArrayAdapter<HashMap<String, Object>>(getBaseContext(), android.R.layout.simple_list_item_2, android.R.id.text1, schedules) {
        	@SuppressWarnings("unchecked")
			@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		View view = super.getView(position, convertView, parent);
        	    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        	    TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        	    
        	    selected_applications = (ArrayList<ApplicationInfo>) schedules.get(position).get("selected_applications");
        		String selected = "";
        		package_manager = getPackageManager();
        		
        		for (ApplicationInfo packageInfo : selected_applications) {
        			try {
        				selected += package_manager.getPackageInfo(packageInfo.packageName, 0).applicationInfo.loadLabel(package_manager).toString() + ", ";;
        			}
        			
        			catch (NameNotFoundException e) {
        				return null;
        			}
        		}
        		
        		selected = selected.substring(0, selected.length() - 2);
        		
        		start_datetime_calendar = (Calendar) schedules.get(position).get("start_datetime_calendar");
        		end_datetime_calendar = (Calendar) schedules.get(position).get("end_datetime_calendar");
        	    
        	    text1.setText(sdf_datetime.format(start_datetime_calendar.getTime()) + " — " + sdf_datetime.format(end_datetime_calendar.getTime()));
        	    text2.setText(selected);
        	    
        	    return view;
        	}
        };
        
        schedules_list.setAdapter(arrayAdapter);
        
        progress_dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		progress_dialog.setTitle("Please wait...");
		progress_dialog.setMessage("Loading schedules...");
		progress_dialog.setIndeterminate(true);
		progress_dialog.setCancelable(false);
		progress_dialog.setCanceledOnTouchOutside(false);
		
		progress_dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_new) {
			Intent intent = new Intent(this, AddApplicationActivity.class);
			startActivityForResult(intent, 1);
			overridePendingTransition(0 , 0);
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				
				relative_layout.removeView(no_schedules);

				selected_applications = extras.getParcelableArrayList("selected_applications");
				start_datetime_calendar = (Calendar) extras.getSerializable("start_datetime_calendar");
				end_datetime_calendar = (Calendar) extras.getSerializable("end_datetime_calendar");
				
				schedule = new HashMap<String, Object>();
				schedule.put("selected_applications", selected_applications);
				schedule.put("start_datetime_calendar", start_datetime_calendar);
				schedule.put("end_datetime_calendar", end_datetime_calendar);
				
				schedules.add(schedule);
				
				arrayAdapter.notifyDataSetChanged();
				
				if (Plus.PeopleApi.getCurrentPerson(google_api_client) != null) {
				    final Person currentPerson = Plus.PeopleApi.getCurrentPerson(google_api_client);
				    schedules_string = ""; 
				    response = null;
				    
				    for (ApplicationInfo packageInfo : selected_applications) {
				    	schedules_string += packageInfo.packageName + ",";
				    }
				    
				    schedules_string = schedules_string.substring(0, schedules_string.length() - 1);
				    
				    thread = new Thread(new Runnable() {
				    	public void run() {
				    		try {
						    	client = new DefaultHttpClient();
						    	request = new HttpGet();
						    	request.setURI(new URI("https://cold-turkey.appspot.com/?schedule_id=" + currentPerson.getId() + "&schedule_packages=" + schedules_string + "&schedule_start_datetime=" + String.valueOf(start_datetime_calendar.getTimeInMillis()) + "&schedule_end_datetime=" + String.valueOf(end_datetime_calendar.getTimeInMillis())));
						    	response = client.execute(request);
						    }
						    
						    catch (URISyntaxException e) {
						    	e.printStackTrace();
						    }
						    
						    catch (ClientProtocolException e) {
						    	e.printStackTrace();
						    }
						    
						    catch (IOException e) {
						    	e.printStackTrace();
						    }
						    
						    return;
				    	}
				    });
				    
				    thread.start();
				}
			}
		}
		
		if (requestCode == RC_SIGN_IN) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			overridePendingTransition(0 , 0);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (!intent_in_progress && result.hasResolution()) {			
			try {				
				intent_in_progress = true;
				result.startResolutionForResult(this, RC_SIGN_IN);
			}
			
			catch (SendIntentException e) {
				intent_in_progress = false;
				google_api_client.connect();
			}
		}		
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		response = null;
		
		task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				try {
					client = new DefaultHttpClient();
				    post_request = new HttpPost("https://cold-turkey.appspot.com/_ah/api/getSchedules/v1/schedules");
			    	response = client.execute(post_request);
					
					try {
						json_object = new JSONObject(inputStreamToString(response.getEntity().getContent()).toString());
						
						package_manager = getPackageManager();
						schedules.clear();
						
						try {
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
									
									if (current_calendar.before(end_datetime_calendar)) {
										schedules.add(schedule);
									}
								}
							}
							
							if (schedules.isEmpty()) {
								MainActivity.this.runOnUiThread(new Runnable(){
									@Override
									public void run(){
										if ((TextView) findViewById(R.id.no_schedules) == null) {
											MainActivity.this.relative_layout.addView(MainActivity.this.no_schedules);
										}
										MainActivity.this.arrayAdapter.notifyDataSetChanged();
									}
								});
							}
							
							else {
								MainActivity.this.runOnUiThread(new Runnable(){
									@Override
									public void run(){
										if ((TextView) findViewById(R.id.no_schedules) != null) {
											MainActivity.this.relative_layout.removeView(MainActivity.this.no_schedules);
										}
										MainActivity.this.arrayAdapter.notifyDataSetChanged();
									}
								});
							}
							
							progress_dialog.dismiss();
						}
						
						catch (JSONException e) {
							progress_dialog.dismiss();
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

	@Override
	public void onConnectionSuspended(int cause) {
		google_api_client.connect();
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
