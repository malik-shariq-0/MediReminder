package com.example.medireminder.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import com.example.medireminder.utils.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String medicine = intent.getStringExtra("medicine");
        if(medicine == null) medicine = "Time to take medicine";

        // Vibration
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator != null) vibrator.vibrate(1000);

        // Show notification
        NotificationHelper.showNotification(context, medicine);
    }
}
