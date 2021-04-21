package com.zohaib.smartattandance.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zohaib.smartattandance.R;
import com.zohaib.smartattandance.models.ModelStudents;

import java.util.ArrayList;

public class AdapterToBeRegisterStudents extends RecyclerView.Adapter<AdapterToBeRegisterStudents.MyViewHolder> {
    ArrayList<ModelStudents> studentsArrayList;

    public AdapterToBeRegisterStudents(ArrayList<ModelStudents> studentsArrayList) {
        this.studentsArrayList = studentsArrayList;
    }

    public IonItemClickListener ionItemClickListener;

    public void setIonItemClickListener(IonItemClickListener ionItemClickListener) {
        this.ionItemClickListener = ionItemClickListener;
    }

    public interface IonItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_register_student, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModelStudents modelStudents = studentsArrayList.get(position);

        holder.tvName.setText(modelStudents.getName());
        holder.tvRoll.setText(modelStudents.getRollNo());

        if (modelStudents.isRegistered()) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.green));
            holder.btnRegister.setActivated(false);
            holder.btnRegister.setText("REGISTERED");
            holder.btnRegister.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return studentsArrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoll, tvName;
        Button btnRegister;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvNameRowRegStd);
            tvRoll = itemView.findViewById(R.id.tvRollRowRegStd);
            btnRegister = itemView.findViewById(R.id.btnRowRegStd);
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ionItemClickListener.onItemClick(getAdapterPosition());
                }
            });

        }
    }
}
