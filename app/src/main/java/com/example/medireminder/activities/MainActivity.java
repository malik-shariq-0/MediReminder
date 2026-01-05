package com.example.medireminder.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medireminder.R;
import com.example.medireminder.adapter.MedicineAdapter;
import com.example.medireminder.model.Medicine;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    List<Medicine> medicineList;
    MedicineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        medicineList = new ArrayList<>();
        adapter = new MedicineAdapter(this, medicineList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Add button → open AddMedicineActivity
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMedicineActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedicines(); // har dafa screen resume hone par list reload hogi
    }

    private void loadMedicines() {
        medicineList.clear();
        SharedPreferences prefs = getSharedPreferences("medireminder", MODE_PRIVATE);
        String json = prefs.getString("medicines", null);

        if (json != null) {
            try {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    Medicine med = Medicine.fromJSON(array.getJSONObject(i));
                    if (med != null) medicineList.add(med);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        adapter.notifyDataSetChanged(); // list refresh
    }
}