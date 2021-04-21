package com.zohaib.smartattandance.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.zohaib.smartattandance.R;
import com.zohaib.smartattandance.models.ModelStudents;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.paperdb.Paper;

public class AdapterRegisterdStudents extends RecyclerView.Adapter<AdapterRegisterdStudents.MyViewHolder> {
    Context context;
    ArrayList<ModelStudents> modelStudents;
    public AdapterRegisterdStudents.IOnItemClickListener iOnItemClickListener;
    Activity activity;
    String keyToDb;
    private int mRecentlyDeletedItemPosition;
    private ModelStudents mRecentlyDeletedItem;

    public void setiOnItemClickListener(AdapterRegisterdStudents.IOnItemClickListener iOnItemClickListener) {
        this.iOnItemClickListener = iOnItemClickListener;
    }

    public interface IOnItemClickListener {
        void onItemClick(ModelStudents modelStudents);
    }

    public AdapterRegisterdStudents(Context context, ArrayList<ModelStudents> modelStudents, Activity activity, String keyToDb) {
        this.context = context;
        this.modelStudents = modelStudents;
        this.activity = activity;
        this.keyToDb = keyToDb;
    }

    @NonNull
    @Override
    public AdapterRegisterdStudents.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_courses, parent, false);

        return new AdapterRegisterdStudents.MyViewHolder(view);
    }

    private List<Integer> getAllMaterialColors() throws IOException, XmlPullParserException {
        XmlResourceParser xrp = context.getResources().getXml(R.xml.material_design_colors);
        List<Integer> allColors = new ArrayList<>();
        int nextEvent;
        while ((nextEvent = xrp.next()) != XmlResourceParser.END_DOCUMENT) {
            String s = xrp.getName();
            if ("color".equals(s)) {
                String color = xrp.nextText();
                allColors.add(Color.parseColor(color));
            }
        }
        return allColors;
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRegisterdStudents.MyViewHolder holder, int position) {
        List<Integer> allColors = null;
        try {
            allColors = getAllMaterialColors();
            int randomIndex = new Random().nextInt(allColors.size());
            int randomColor = allColors.get(randomIndex);
            holder.cardView.setCardBackgroundColor(randomColor);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        ModelStudents modelStudent = modelStudents.get(position);

        holder.tvCourseName.setText(modelStudent.getName());
        holder.tvCourseCode.setText(modelStudent.getRollNo());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(context, ActViewCourseDetails.class);
//                intent.putExtra("courseCode", modelCourses.getRollNo());
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return modelStudents.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvCourseCode, tvCourseName;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView.getRootView();
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvCourseName = itemView.findViewById(R.id.tvCourseName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iOnItemClickListener.onItemClick(modelStudents.get(getAdapterPosition()));
                }
            });
        }
    }
    public void deleteItem(int position) {
        mRecentlyDeletedItem = modelStudents.get(position);
        mRecentlyDeletedItemPosition = position;
        modelStudents.remove(position);
        Paper.book().write(keyToDb,modelStudents);
        notifyItemRemoved(position);
        showUndoSnackbar();
    }

    public void showUndoSnackbar() {
        View view = activity.findViewById(R.id.actStudents);
        Snackbar snackbar = Snackbar.make(view, "Student Deleted",
                Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoDelete();
            }
        });
        snackbar.setActionTextColor(context.getResources().getColor(R.color.design_default_color_on_primary));
        snackbar.show();
    }

    private void undoDelete() {
        modelStudents.add(mRecentlyDeletedItemPosition, mRecentlyDeletedItem);
        notifyItemInserted(mRecentlyDeletedItemPosition);
        Paper.book().write(keyToDb, modelStudents);
        notifyDataSetChanged();
    }
}
