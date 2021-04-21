package com.zohaib.smartattandance.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zohaib.smartattandance.R;
import com.zohaib.smartattandance.models.ModelAttendance;
import com.zohaib.smartattandance.models.ModelCourses;

import java.util.ArrayList;

public class AdapterNotSyncedAttendance extends RecyclerView.Adapter<AdapterNotSyncedAttendance.MyViewHolder> {

    ArrayList<ModelAttendance> attendanceArrayList;


    public AdapterNotSyncedAttendance(ArrayList<ModelAttendance> attendanceArrayList) {
        this.attendanceArrayList = attendanceArrayList;
    }

    public IOnItemClickListener iOnItemClickListener;

    public void setiOnItemClickListener(IOnItemClickListener iOnItemClickListener) {
        this.iOnItemClickListener = iOnItemClickListener;
    }

    public interface IOnItemClickListener {
        void onItemClick(ModelAttendance modelAttendance, int position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_not_synced, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ModelAttendance modelAttendance = attendanceArrayList.get(position);
        holder.tvDate.setText(modelAttendance.getDate());

    }

    @Override
    public int getItemCount() {
        return attendanceArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate;
        Button btnSync;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.date);
            btnSync = itemView.findViewById(R.id.updatebtn);

            btnSync.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iOnItemClickListener.onItemClick(attendanceArrayList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

        }
    }


}
