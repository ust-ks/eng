package com.example.eng;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ServiceNotice extends Service {
    public ServiceNotice() {
    }

    NotificationManager nm;
    NotificationChannel mChannel;

    public void onCreate(){
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public int onStartCommand(Intent intent, int flags, int startid){
        init();

        checkNotice();
        return super.onStartCommand(intent, flags, startid);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void init(){
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String id = "my_channel_01";
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_LOW;

        mChannel = new NotificationChannel(id, name,importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        nm.createNotificationChannel(mChannel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void checkNotice(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        TimeUnit.SECONDS.sleep(30);
                        if(newNotice()){
                            sendNotice();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public boolean newNotice(){

        Date date = new Date();
        long sec = date.getTime()/(1000*60); // тут минуты прошедшие с 01.01.1970 00:00
        long now = (sec - 52*365*24*60); // тут минуты прошедшие с 01.01.2022 00:00

        String selection = DBHelper.KEY_STATUS_dirs + " = 1";

        SQLiteDatabase database = MainActivity.dbHelper.getWritableDatabase();
        Cursor cursor = database.query(
                DBHelper.TABLE_DIRS,
                null,
                selection,
                null,
                null,
                null,
                null);

        while (cursor.moveToNext()){
            @SuppressLint("Range") int nr = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_NEXT_REPETITION_dirs));
            if (nr <= now){
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public void sendNotice(){

        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, i, 0);
        String CHANNEL_ID = "my_channel_01";

        Notification notice = new NotificationCompat.Builder(this)
                .setContentTitle("Пора повторить словарь!")
                .setContentText(" ")
                .setSmallIcon(R.drawable.ic_dictionary)
                .setChannelId(CHANNEL_ID)
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true).build();

        notice.flags |= Notification.FLAG_AUTO_CANCEL;
        nm.notify(1, notice);
        Log.d("tag", "NOTICE IS SEND");
    }
}