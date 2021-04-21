package com.zohaib.smartattandance.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.zohaib.smartattandance.R;
import com.zohaib.smartattandance.activities.ActViewCourseDetails;
import com.zohaib.smartattandance.models.ModelCourses;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.paperdb.Paper;

public class AdapterRvCourses extends RecyclerView.Adapter<AdapterRvCourses.MyViewHolder> {

    Context context;
    ArrayList<ModelCourses> modelCoursesArrayList;
   public IOnItemClickListener iOnItemClickListener;
    private ModelCourses mRecentlyDeletedItem;
    private int mRecentlyDeletedItemPosition;
    private String keyToDb;
    private FragmentActivity activity;



    public void setiOnItemClickListener(IOnItemClickListener iOnItemClickListener) {
        this.iOnItemClickListener = iOnItemClickListener;
    }

    public interface IOnItemClickListener {
        void onItemClick(ModelCourses modelCourse);
    }

    public AdapterRvCourses(Context context, ArrayList<ModelCourses> modelCoursesArrayList, String keyToDb, FragmentActivity activity) {
        this.context = context;
        this.modelCoursesArrayList = modelCoursesArrayList;
        this.keyToDb = keyToDb;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_courses, parent, false);

        return new MyViewHolder(view);
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
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        List<Integer> allColors = null;
        try {
            allColors = getAllMaterialColors();
            int randomIndex = new Random().nextInt(allColors.size());
            int randomColor = allColors.get(randomIndex);
            holder.cardView.setCardBackgroundColor(randomColor);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        ModelCourses modelCourses = modelCoursesArrayList.get(position);

        holder.tvCourseName.setText(modelCourses.getName());
        holder.tvCourseCode.setText(modelCourses.getId());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ActViewCourseDetails.class);

                intent.putExtra("courseName", modelCourses.getName());
                intent.putExtra("courseCode", modelCourses.getId());
                intent.putExtra("spreadSheetId",modelCourses.getSpreadSheetId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return modelCoursesArrayList.size();
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
                    iOnItemClickListener.onItemClick(modelCoursesArrayList.get(getAdapterPosition()));
                }
            });
        }
    }




    public void deleteItem(int position) {
        mRecentlyDeletedItem = modelCoursesArrayList.get(position);
        mRecentlyDeletedItemPosition = position;
        modelCoursesArrayList.remove(position);
        Paper.book().write(keyToDb,modelCoursesArrayList);
        notifyItemRemoved(position);
        showUndoSnackbar();
    }

    public void showUndoSnackbar() {
        View view = activity.findViewById(R.id.fragCourse);
        Snackbar snackbar = Snackbar.make(view, "Course Deleted",
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
        modelCoursesArrayList.add(mRecentlyDeletedItemPosition, mRecentlyDeletedItem);
        notifyItemInserted(mRecentlyDeletedItemPosition);
        Paper.book().write(keyToDb, modelCoursesArrayList);
        notifyDataSetChanged();
    }
}
