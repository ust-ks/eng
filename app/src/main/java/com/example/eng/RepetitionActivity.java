package com.example.eng;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;

public class RepetitionActivity extends AppCompatActivity {

    public String dir_name;
    public String[] words;
    public String[] translations;
    public int dir_id;

    private TextView tv_dir_name;
    private ConstraintLayout cl_card;
    private TextView tv_text;

    private TextView tv_hint;

    private LinearLayout ll_grade;
    private ImageButton b_dislike;
    private ImageButton b_like;

    private TextView tv_result;
    private TextView b_again;
    private TextView b_complete;

    private int result = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repetition);

        dir_name = getIntent().getStringExtra("name");
        words = getIntent().getStringArrayExtra("list_words");
        translations = getIntent().getStringArrayExtra("list_translations");
        dir_id = getIntent().getIntExtra("dir_id", 0);

        init();

        tv_dir_name.setText(dir_name);

        front(0);
    }

    private void init(){

        tv_dir_name = findViewById(R.id.tv_dir_name);
        cl_card = findViewById(R.id.cl_card);
        tv_text = findViewById(R.id.tv_text);

        tv_hint = findViewById(R.id.tv_hint);
        tv_hint.setVisibility(View.INVISIBLE);

        ll_grade = findViewById(R.id.ll_grade);
        ll_grade.setVisibility(View.INVISIBLE);
        b_dislike = findViewById(R.id.b_dislike);
        b_like = findViewById(R.id.b_like);

        tv_result = findViewById(R.id.tv_result);
        tv_result.setVisibility(View.INVISIBLE);
        b_again = findViewById(R.id.b_again);
        b_again.setVisibility(View.INVISIBLE);
        b_complete = findViewById(R.id.b_complete);
        b_complete.setVisibility(View.INVISIBLE);
    }

    private void front(int i){

        tv_hint.setVisibility(View.VISIBLE);
        ll_grade.setVisibility(View.INVISIBLE);
        tv_result.setVisibility(View.INVISIBLE);
        b_again.setVisibility(View.INVISIBLE);
        b_complete.setVisibility(View.INVISIBLE);

        if(words[i].length() < 50){tv_text.setTextSize(30);}
        if((words[i].length() >= 50) && (words[i].length() < 100)){ tv_text.setTextSize(25);}
        if(words[i].length() >= 100){tv_text.setTextSize(20);}

        tv_text.setText(words[i]);

        cl_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back(i);
            }
        });
    }

    private void back(int i){

        tv_hint.setVisibility(View.INVISIBLE);
        ll_grade.setVisibility(View.VISIBLE);
        tv_result.setVisibility(View.INVISIBLE);
        b_again.setVisibility(View.INVISIBLE);
        b_complete.setVisibility(View.INVISIBLE);

        if(translations[i].length() < 50){tv_text.setTextSize(30);}
        if((translations[i].length() >= 50) && (translations[i].length() < 100)){ tv_text.setTextSize(25);}
        if(translations[i].length() >= 100){tv_text.setTextSize(20);}

        tv_text.setText(translations[i]);

        b_dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i+1 == words.length){
                    end();
                }
                else{ front(i+1);}
            }
        });

        b_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result+=1;
                if (i+1 == words.length){
                    end();
                }
                else{ front(i+1);}

            }
        });
    }

    private void end(){
        String res = "вы хорошо знаете " + result + " из " + words.length + " слов";
        tv_text.setTextSize(30);
        tv_text.setText(res);

        tv_hint.setVisibility(View.INVISIBLE);
        ll_grade.setVisibility(View.INVISIBLE);
        tv_result.setVisibility(View.VISIBLE);
        b_again.setVisibility(View.VISIBLE);
        b_complete.setVisibility(View.VISIBLE);

        cl_card.setEnabled(false);

        b_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result = 0;
                cl_card.setEnabled(true);
                front(0);
            }
        });

        b_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_next_repetition();
                finish();
            }
        });
    }

    private void update_next_repetition(){
        int new_num = get_num_repetition()+1;

        ContentValues contentValues = new ContentValues();

        // dt будет хранить количество МИНУТ прошедших с 01.01.2022 00:00
        Date date = new Date();
        long sec = date.getTime()/(1000*60); // тут минуты прошедшие с 01.01.1970 00:00
        long dt = (sec - 52*365*24*60); // тут минуты прошедшие с 01.01.2022 00:00
        contentValues.put(DBHelper.KEY_LAST_REPETITION_dirs, dt);

        // interval хранит количество дней спустя которые надо будет снова повторить словарь
        int interval = new_num*2 + 1;
        dt += interval*24*60;
        contentValues.put(DBHelper.KEY_NEXT_REPETITION_dirs, dt);

        contentValues.put(DBHelper.KEY_NUM_dirs, new_num);

        MainActivity.database.update(
                DBHelper.TABLE_DIRS,
                contentValues,
                DBHelper.KEY_ID_dirs + "=" + dir_id,
                null);
    }

    private int get_num_repetition(){
        int num = 0;
        SQLiteDatabase database = MainActivity.dbHelper.getWritableDatabase();
        String selection = DBHelper.KEY_ID_dirs + " = " + dir_id;
        Cursor cursor = database.query(
                DBHelper.TABLE_DIRS,
                null,
                selection,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()){
            @SuppressLint("Range") int n = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_NUM_dirs));
            num = n;
        }

        cursor.close();
        return num;
    }

    public void onBackPressed() {
        finish();
    }
}