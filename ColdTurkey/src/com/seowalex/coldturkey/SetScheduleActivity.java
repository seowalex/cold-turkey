package com.seowalex.coldturkey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

public class SetScheduleActivity extends Activity {
	private ArrayList<ApplicationInfo> selected_applications;
	private PackageManager package_manager;
	private SimpleDateFormat sdf_date;
	private SimpleDateFormat sdf_time;
	private TextView selected_applications_textview;
	
	private Calendar start_datetime_calendar;
	private TextView start_date_picker_textview;
	private DatePickerDialog start_date_picker_dialog;
	private TextView start_time_picker_textview;
	private TimePickerDialog start_time_picker_dialog;

	private Calendar end_datetime_calendar;
	private TextView end_date_picker_textview;
	private DatePickerDialog end_date_picker_dialog;
	private TextView end_time_picker_textview;
	private TimePickerDialog end_time_picker_dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_schedule);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		selected_applications = getIntent().getParcelableArrayListExtra("selected_applications");
		String selected = "";
		package_manager = getPackageManager();
		
		for (ApplicationInfo packageInfo : selected_applications) {
			try {
				selected += package_manager.getPackageInfo(packageInfo.packageName, 0).applicationInfo.loadLabel(package_manager).toString() + ", ";;
			}
			
			catch (NameNotFoundException e) {
				return;
			}
		}
		
		selected = selected.substring(0, selected.length() - 2);
		
		selected_applications_textview = (TextView) findViewById(R.id.selected_applications);
		selected_applications_textview.setText(selected);
		
		sdf_date = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
		sdf_time = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
		
		start_datetime_calendar = Calendar.getInstance();
		
		start_date_picker_textview = (TextView) findViewById(R.id.start_date_picker);
		start_date_picker_textview.setText(sdf_date.format(start_datetime_calendar.getTime()));
		start_time_picker_textview = (TextView) findViewById(R.id.start_time_picker);
		start_time_picker_textview.setText(sdf_time.format(start_datetime_calendar.getTime()));
		
		final DatePickerDialog.OnDateSetListener start_date_picker_listener = new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int month, int day) {
				start_datetime_calendar.set(year, month, day);
				start_date_picker_textview.setText(sdf_date.format(start_datetime_calendar.getTime()));
				
				if (start_datetime_calendar.after(end_datetime_calendar)) {
					end_datetime_calendar.set(start_datetime_calendar.get(Calendar.YEAR), start_datetime_calendar.get(Calendar.MONTH), start_datetime_calendar.get(Calendar.DAY_OF_MONTH), start_datetime_calendar.get(Calendar.HOUR_OF_DAY), start_datetime_calendar.get(Calendar.MINUTE));
					end_datetime_calendar.add(Calendar.MINUTE, 1);
					
					end_date_picker_textview.setText(sdf_date.format(end_datetime_calendar.getTime()));
					end_time_picker_textview.setText(sdf_time.format(end_datetime_calendar.getTime()));
					
					end_date_picker_dialog.updateDate(end_datetime_calendar.get(Calendar.YEAR), end_datetime_calendar.get(Calendar.MONTH), end_datetime_calendar.get(Calendar.DAY_OF_MONTH));
					end_time_picker_dialog.updateTime(end_datetime_calendar.get(Calendar.HOUR_OF_DAY), end_datetime_calendar.get(Calendar.MINUTE));
				}
			}
		};
		
		final TimePickerDialog.OnTimeSetListener start_time_picker_listener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hour, int minute) {
				start_datetime_calendar.set(start_datetime_calendar.get(Calendar.YEAR), start_datetime_calendar.get(Calendar.MONTH), start_datetime_calendar.get(Calendar.DAY_OF_MONTH), hour, minute);
				start_time_picker_textview.setText(sdf_time.format(start_datetime_calendar.getTime()));
				
				if (start_datetime_calendar.after(end_datetime_calendar)) {
					end_datetime_calendar.set(start_datetime_calendar.get(Calendar.YEAR), start_datetime_calendar.get(Calendar.MONTH), start_datetime_calendar.get(Calendar.DAY_OF_MONTH), start_datetime_calendar.get(Calendar.HOUR_OF_DAY), start_datetime_calendar.get(Calendar.MINUTE));
					end_datetime_calendar.add(Calendar.MINUTE, 1);
					
					end_date_picker_textview.setText(sdf_date.format(end_datetime_calendar.getTime()));
					end_time_picker_textview.setText(sdf_time.format(end_datetime_calendar.getTime()));
					
					end_date_picker_dialog.updateDate(end_datetime_calendar.get(Calendar.YEAR), end_datetime_calendar.get(Calendar.MONTH), end_datetime_calendar.get(Calendar.DAY_OF_MONTH));
					end_time_picker_dialog.updateTime(end_datetime_calendar.get(Calendar.HOUR_OF_DAY), end_datetime_calendar.get(Calendar.MINUTE));
				}
			}
		};
		
		start_date_picker_dialog = new DatePickerDialog(this, start_date_picker_listener, start_datetime_calendar.get(Calendar.YEAR), start_datetime_calendar.get(Calendar.MONTH), start_datetime_calendar.get(Calendar.DAY_OF_MONTH));
		start_date_picker_dialog.setCancelable(false);
		start_date_picker_dialog.setCanceledOnTouchOutside(false);
		start_date_picker_dialog.setTitle("Select start date");
		
		start_time_picker_dialog = new TimePickerDialog(this, start_time_picker_listener, start_datetime_calendar.get(Calendar.HOUR_OF_DAY), start_datetime_calendar.get(Calendar.MINUTE), false);
		start_time_picker_dialog.setCancelable(false);
		start_time_picker_dialog.setCanceledOnTouchOutside(false);
		start_time_picker_dialog.setTitle("Select start time");
		
		start_date_picker_textview.setOnClickListener(new OnClickListener() {
			@Override
	        public void onClick(View view) {
				start_date_picker_dialog.show();
			}
		});
		
		start_time_picker_textview.setOnClickListener(new OnClickListener() {
			@Override
	        public void onClick(View view) {
				start_time_picker_dialog.show();
			}
		});
		
		end_datetime_calendar = Calendar.getInstance();
		end_datetime_calendar.add(Calendar.HOUR_OF_DAY, 1);
		
		end_date_picker_textview = (TextView) findViewById(R.id.end_date_picker);
		end_date_picker_textview.setText(sdf_date.format(end_datetime_calendar.getTime()));
		end_time_picker_textview = (TextView) findViewById(R.id.end_time_picker);
		end_time_picker_textview.setText(sdf_time.format(end_datetime_calendar.getTime()));
		
		final DatePickerDialog.OnDateSetListener end_date_picker_listener = new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int month, int day) {				
				end_datetime_calendar.set(year, month, day);
				end_date_picker_textview.setText(sdf_date.format(end_datetime_calendar.getTime()));
				
				if (end_datetime_calendar.before(start_datetime_calendar)) {
					start_datetime_calendar.set(end_datetime_calendar.get(Calendar.YEAR), end_datetime_calendar.get(Calendar.MONTH), end_datetime_calendar.get(Calendar.DAY_OF_MONTH), end_datetime_calendar.get(Calendar.HOUR_OF_DAY), end_datetime_calendar.get(Calendar.MINUTE));
					start_datetime_calendar.add(Calendar.MINUTE, -1);
					
					start_date_picker_textview.setText(sdf_date.format(start_datetime_calendar.getTime()));
					start_time_picker_textview.setText(sdf_time.format(start_datetime_calendar.getTime()));
					
					start_date_picker_dialog.updateDate(start_datetime_calendar.get(Calendar.YEAR), start_datetime_calendar.get(Calendar.MONTH), start_datetime_calendar.get(Calendar.DAY_OF_MONTH));
					start_time_picker_dialog.updateTime(start_datetime_calendar.get(Calendar.HOUR_OF_DAY), start_datetime_calendar.get(Calendar.MINUTE));
				}
			}
		};
		
		final TimePickerDialog.OnTimeSetListener end_time_picker_listener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hour, int minute) {
				end_datetime_calendar.set(end_datetime_calendar.get(Calendar.YEAR), end_datetime_calendar.get(Calendar.MONTH), end_datetime_calendar.get(Calendar.DAY_OF_MONTH), hour, minute);
				end_time_picker_textview.setText(sdf_time.format(end_datetime_calendar.getTime()));
				
				if (end_datetime_calendar.before(start_datetime_calendar)) {
					start_datetime_calendar.set(end_datetime_calendar.get(Calendar.YEAR), end_datetime_calendar.get(Calendar.MONTH), end_datetime_calendar.get(Calendar.DAY_OF_MONTH), end_datetime_calendar.get(Calendar.HOUR_OF_DAY), end_datetime_calendar.get(Calendar.MINUTE));
					start_datetime_calendar.add(Calendar.MINUTE, -1);
					
					start_date_picker_textview.setText(sdf_date.format(start_datetime_calendar.getTime()));
					start_time_picker_textview.setText(sdf_time.format(start_datetime_calendar.getTime()));
					
					start_date_picker_dialog.updateDate(start_datetime_calendar.get(Calendar.YEAR), start_datetime_calendar.get(Calendar.MONTH), start_datetime_calendar.get(Calendar.DAY_OF_MONTH));
					start_time_picker_dialog.updateTime(start_datetime_calendar.get(Calendar.HOUR_OF_DAY), start_datetime_calendar.get(Calendar.MINUTE));
				}
			}
		};
		
		end_date_picker_dialog = new DatePickerDialog(this, end_date_picker_listener, end_datetime_calendar.get(Calendar.YEAR), end_datetime_calendar.get(Calendar.MONTH), end_datetime_calendar.get(Calendar.DAY_OF_MONTH));
		end_date_picker_dialog.setCancelable(false);
		end_date_picker_dialog.setCanceledOnTouchOutside(false);
		end_date_picker_dialog.setTitle("Select end date");
		
		end_time_picker_dialog = new TimePickerDialog(this, end_time_picker_listener, end_datetime_calendar.get(Calendar.HOUR_OF_DAY), end_datetime_calendar.get(Calendar.MINUTE), false);
		end_time_picker_dialog.setCancelable(false);
		end_time_picker_dialog.setCanceledOnTouchOutside(false);
		end_time_picker_dialog.setTitle("Select end time");
		
		end_date_picker_textview.setOnClickListener(new OnClickListener() {
			@Override
	        public void onClick(View view) {
				end_date_picker_dialog.show();
			}
		});
		
		end_time_picker_textview.setOnClickListener(new OnClickListener() {
			@Override
	        public void onClick(View view) {
				end_time_picker_dialog.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_set_schedule, menu);
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
			case R.id.action_accept:
				Intent output = new Intent();
				output.putExtra("selected_applications", selected_applications);
				output.putExtra("start_datetime_calendar", start_datetime_calendar);
				output.putExtra("end_datetime_calendar", end_datetime_calendar);
				
    		   	setResult(RESULT_OK, output);
				
				finish();
				overridePendingTransition(0 , 0);
				
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
}
