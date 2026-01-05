package com.example.medireminder.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.medireminder.R;
import com.example.medireminder.model.Medicine;

import org.json.JSONArray;

import java.util.Calendar;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AddMedicineActivity extends AppCompatActivity {

    EditText etMedicineName;
    TimePicker timePicker;
    CalendarView calendarView;
    Button btnSave, btnDelete;

    int selectedYear, selectedMonth, selectedDay;

    boolean isEdit = false;
    long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        etMedicineName = findViewById(R.id.etMedicineName);
        timePicker = findViewById(R.id.timePicker);
        calendarView = findViewById(R.id.calendarView);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, 101);
            }
        }

        // Default date = today
        Calendar today = Calendar.getInstance();
        selectedYear = today.get(Calendar.YEAR);
        selectedMonth = today.get(Calendar.MONTH);
        selectedDay = today.get(Calendar.DAY_OF_MONTH);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
        });

        // Edit mode check
        isEdit = getIntent().getBooleanExtra("edit", false);
        editId = getIntent().getLongExtra("id", -1);

        if (isEdit) {
            etMedicineName.setText(getIntent().getStringExtra("name"));
            String time = getIntent().getStringExtra("time");
            if (time != null) {
                String[] parts = time.split(":");
                timePicker.setHour(Integer.parseInt(parts[0]));
                timePicker.setMinute(Integer.parseInt(parts[1]));
            }
            btnDelete.setVisibility(View.VISIBLE);
        }

        btnSave.setOnClickListener(v -> saveMedicine());
        btnDelete.setOnClickListener(v -> deleteMedicine());
    }

    private void saveMedicine() {
        String medicineName = etMedicineName.getText().toString().trim();
        if (medicineName.isEmpty()) {
            Toast.makeText(this, "Enter medicine name", Toast.LENGTH_SHORT).show();
            return;
        }

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String time = String.format("%02d:%02d", hour, minute);

        long medicineId = isEdit ? editId : System.currentTimeMillis();
        Medicine medicine = new Medicine(medicineId, medicineName, time);

        SharedPreferences prefs = getSharedPreferences("medireminder", MODE_PRIVATE);
        String jsonString = prefs.getString("medicines", null);

        JSONArray array = new JSONArray();
        try {
            if (jsonString != null) array = new JSONArray(jsonString);

            if (isEdit) {
                JSONArray newArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    if (array.getJSONObject(i).getLong("id") != editId) {
                        newArray.put(array.getJSONObject(i));
                    }
                }
                array = newArray;
            }

            array.put(medicine.toJSON());
            prefs.edit().putString("medicines", array.toString()).commit();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving medicine", Toast.LENGTH_SHORT).show();
            return;
        }

        setAlarm(medicine);
        Toast.makeText(this, isEdit ? "Medicine updated" : "Medicine added", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteMedicine() {
        if (!isEdit) return;

        try {
            SharedPreferences prefs = getSharedPreferences("medireminder", MODE_PRIVATE);
            String json = prefs.getString("medicines", null);
            if (json != null) {
                JSONArray array = new JSONArray(json);
                JSONArray newArray = new JSONArray();
                for (int i = 0; i < array.length(); i++) {
                    if (array.getJSONObject(i).getLong("id") != editId) {
                        newArray.put(array.getJSONObject(i));
                    }
                }
                prefs.edit().putString("medicines", newArray.toString()).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) editId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) alarmManager.cancel(pendingIntent);

        Toast.makeText(this, "Medicine deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setAlarm(Medicine medicine) {

        Calendar calendar = Calendar.getInstance();
        String[] parts = medicine.time.split(":");

        calendar.set(Calendar.YEAR, selectedYear);
        calendar.set(Calendar.MONTH, selectedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Selected time is in past", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicine", medicine.name);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) medicine.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}
