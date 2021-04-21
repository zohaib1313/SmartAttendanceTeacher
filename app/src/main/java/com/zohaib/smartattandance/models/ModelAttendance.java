package com.zohaib.smartattandance.models;

import java.util.ArrayList;

public class ModelAttendance {
    public String courseId;
    public String date;
    public ArrayList<ModelRollNo> rollnumbers;

    public int isSynced;


    public ModelAttendance(String courseId, String date, ArrayList<ModelRollNo> rollnumbers, int isSynced) {
        this.courseId = courseId;
        this.date = date;
        this.rollnumbers = rollnumbers;
        this.isSynced = isSynced;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<ModelRollNo> getRollnumbers() {
        return rollnumbers;
    }

    public void setRollnumbers(ArrayList<ModelRollNo> rollnumbers) {
        this.rollnumbers = rollnumbers;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }
}
