package com.example.eng;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity {

    private ListView lv_timetable;

    private ListDir list;
    private String[] timetable;
    private ArrayAdapter<String> adapter;

    private Calendar dateAndTime;
    private int edit_dir_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        lv_timetable = findViewById(R.id.lv_timetable);

        setting_timetable();

        lv_timetable.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialog_edit_time_next_repetition(i);
                return true;
            }
        });

    }

    private void setting_timetable(){
        list = MainActivity.get_dirs();
        timetable = new String[list.id_dirs.length];
        String str = null;
        for(int i=0; i<list.id_dirs.length; i++){
            long sec = list.NextRepetitions_dirs[i] * (60);
            long ms = sec + 60*60*24*365*52;
            ms = ms*1000;
            Date dt = new Date(ms);

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "dd.MM.yyyy HH:mm"
            );

            str = list.names_dirs[i] + " - " + dateFormat.format(dt);
            timetable[i] = str;
        }
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, timetable);
        lv_timetable.setAdapter(adapter);
    }


    private void dialog_edit_time_next_repetition(int i){
        dateAndTime = Calendar.getInstance();
        edit_dir_id = list.id_dirs[i];
        setTime();
    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            dateAndTime.set(Calendar.YEAR, year);
            dateAndTime.set(Calendar.MONTH, monthOfYear);
            dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
    };

    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            dateAndTime.set(Calendar.SECOND, 0);
            update_time_next_repetition();
        }
    };

    private void setDate(){
        new DatePickerDialog(SettingsActivity.this, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setTime(){
        new TimePickerDialog(SettingsActivity.this, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
        setDate();
    }

    private void update_time_next_repetition(){
        // dt будет хранить количество МИНУТ прошедших с 01.01.2022 00:00
        Date new_date = dateAndTime.getTime();
        long sec = new_date.getTime()/(1000*60); // тут минуты прошедшие с 01.01.1970 00:00
        long dt = (sec - 52*365*24*60); // тут минуты прошедшие с 01.01.2022 00:00

        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_NEXT_REPETITION_dirs, dt);
        String where = DBHelper.KEY_ID_dirs + " = " + edit_dir_id;
        MainActivity.database.update(DBHelper.TABLE_DIRS, contentValues, where, null);
        edit_dir_id = -1;
        recreate();
    }

    public void onBackPressed() {
        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}