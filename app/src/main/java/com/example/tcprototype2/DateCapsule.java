package com.example.tcprototype2;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class DateCapsule extends AppCompatActivity {
    DatePicker datePicker;
    TimePicker timePicker;
    Button btnNext;
    TextView txtDate, txtTime, txtTitle;
    Intent intent;
    Bundle extras;
    int d, m, y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_capsule);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get recipient from previous activity
        intent = getIntent();
        extras = intent.getExtras();

        datePicker = findViewById(R.id.dp_capsule_date);
        btnNext = findViewById(R.id.btn_to_fill_capsule);
        txtDate = findViewById(R.id.txt_date);
        txtTitle = findViewById(R.id.txt_set_title);
        txtTime = findViewById(R.id.txt_time);
        timePicker = findViewById(R.id.tp_open);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int year, int month, int dayOfMonth) {
                d = dayOfMonth;
                m = month + 1;
                y = year;
                String text = "The time capsule will be ready to open on " + String.valueOf(d) + "/" +String.valueOf(m) + "/" + String.valueOf(y);
                txtDate.setText(text);
            }
        });
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                String text = "Time of opening: " + String.valueOf(hourOfDay) + ":" + String.valueOf(minute);
                txtTime.setText(text);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = txtTitle.getText().toString();
                if (!title.isEmpty()) {
                    // Create Date object from selected date and time
                    Calendar cal = Calendar.getInstance();
                    cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute(), 0);
                    Date date = cal.getTime();

                    Intent next = new Intent(DateCapsule.this, CustomizeCapsule.class);
                    extras.putString("title", title);
                    extras.putSerializable("open_date", date);
                    next.putExtras(extras);
                    startActivity(next);
                }
                else {
                    Toast.makeText(DateCapsule.this, "Please enter a title for the time capsule.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}