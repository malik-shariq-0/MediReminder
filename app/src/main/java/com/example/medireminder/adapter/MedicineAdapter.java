package com.example.medireminder.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medireminder.R;
import com.example.medireminder.activities.AddMedicineActivity;
import com.example.medireminder.model.Medicine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private Context context;
    private List<Medicine> medicines;

    public MedicineAdapter(Context context, List<Medicine> medicines){
        this.context = context;
        this.medicines = medicines;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Medicine med = medicines.get(position);
        holder.tvName.setText(med.name);
        holder.tvTime.setText(med.time);

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(context, AddMedicineActivity.class);
            i.putExtra("edit", true);
            i.putExtra("id", med.id);
            i.putExtra("name", med.name);
            i.putExtra("time", med.time);
            context.startActivity(i);
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("medireminder", Context.MODE_PRIVATE);
                String json = prefs.getString("medicines", null);
                if(json != null){
                    JSONArray array = new JSONArray(json);
                    JSONArray newArray = new JSONArray();
                    for(int i=0;i<array.length();i++){
                        JSONObject obj = array.getJSONObject(i);
                        if(obj.getInt("id") != med.id) {
                            newArray.put(obj);
                        }
                    }
                    prefs.edit().putString("medicines", newArray.toString()).apply();
                }
            } catch(Exception e){ e.printStackTrace(); }

            medicines.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, medicines.size());
        });
    }

    @Override
    public int getItemCount(){
        return medicines.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvName, tvTime;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvTime = itemView.findViewById(R.id.tvMedicineTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
