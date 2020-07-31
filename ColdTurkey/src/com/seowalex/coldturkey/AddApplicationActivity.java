package com.seowalex.coldturkey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class AddApplicationActivity extends Activity {
	private PackageManager package_manager;
	private List<ApplicationInfo> packages;
	private AsyncTask<Void, Void, Void> task;
	private ProgressDialog progress_dialog;
	private ListView listview;
	private ArrayAdapter<ApplicationInfo> array_adapter;
	private ArrayList<ApplicationInfo> applications;
	private ArrayList<ApplicationInfo> selected_applications;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_application);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		progress_dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
		progress_dialog.setTitle("Please wait...");
		progress_dialog.setMessage("Loading apps...");
		progress_dialog.setIndeterminate(true);
		progress_dialog.setCanceledOnTouchOutside(false);
		progress_dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Intent intent = new Intent();
			   	setResult(RESULT_CANCELED, intent);
				finish();				
				overridePendingTransition(0 , 0);
			}
		});
		
		progress_dialog.show();
		
		task = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				applications = new ArrayList<ApplicationInfo>();
				
				package_manager = getPackageManager();
				packages = package_manager.getInstalledApplications(PackageManager.GET_META_DATA);
				
				Collections.sort(packages, new ApplicationInfo.DisplayNameComparator(package_manager)); 
				
				for (ApplicationInfo packageInfo : packages) {
					if (package_manager.getLaunchIntentForPackage(packageInfo.packageName) != null) {						
						applications.add(packageInfo);
					}
				}

				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if (progress_dialog.isShowing()) {
					listview = (ListView) findViewById(R.id.listview);
					listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
					
					array_adapter = new ArrayAdapter<ApplicationInfo>(getBaseContext(), R.layout.simple_list_item_multiple_choice, applications) {
			        	@Override
			        	public View getView(int position, View convertView, ViewGroup parent) {
			        		View view = super.getView(position, convertView, parent);
			        		CheckedTextView text1 = (CheckedTextView) view.findViewById(android.R.id.text1);
			        	    
			        	    String appName;
			        	    
							try {
								appName = package_manager.getPackageInfo(applications.get(position).packageName, 0).applicationInfo.loadLabel(package_manager).toString();
							}
							
							catch (NameNotFoundException e) {
								return null;
							}
							
							text1.setText(appName);
			        	    text1.setCompoundDrawablesWithIntrinsicBounds(applications.get(position).loadIcon(package_manager), null, null, null);
			        	    
			        	    return view;
			        	}
					};
					
					listview.setAdapter(array_adapter);
					
					progress_dialog.dismiss();
		        }
			}
		};
		
		task.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_add_application, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent intent = new Intent();
			   	setResult(RESULT_CANCELED, intent);
				finish();				
				overridePendingTransition(0 , 0);
				
				return true;
			case R.id.action_next_item:				
				listview = (ListView) findViewById(R.id.listview);
				
				int count = listview.getCount();
				SparseBooleanArray sparseBooleanArray = listview.getCheckedItemPositions();
				selected_applications = new ArrayList<ApplicationInfo>();
				
				for (int i = 0; i < count; i++) {
					if (sparseBooleanArray.get(i)) {
						selected_applications.add(applications.get(i));
					}
				}
				
				if (!selected_applications.isEmpty()) {
					Intent i = new Intent(this, SetScheduleActivity.class);
					i.putExtra("selected_applications", selected_applications);
					startActivityForResult(i, 1);
					overridePendingTransition(0 , 0);
				}
				
				else {
					Toast.makeText(getBaseContext(), "No applications selected!", Toast.LENGTH_SHORT).show();
				}
				
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
	   	setResult(RESULT_CANCELED, intent);
		finish();				
		overridePendingTransition(0 , 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK, data);
				finish();
				overridePendingTransition(0 , 0);
			}
		}
	}
}
