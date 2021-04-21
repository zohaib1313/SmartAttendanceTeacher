package com.zohaib.smartattandance.models;

import java.util.ArrayList;

public class ModelCourses {
   String id, name, spreadSheetId;
    ArrayList<ModelRollNo> modelRollNos;

    public ModelCourses(String id, String name, ArrayList<ModelRollNo> modelRollNos) {
        this.id = id;
        this.name = name;
        this.spreadSheetId = spreadSheetId;
        this.modelRollNos = modelRollNos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpreadSheetId() {
        return spreadSheetId;
    }

    public void setSpreadSheetId(String spreadSheetId) {
        this.spreadSheetId = spreadSheetId;
    }

    public ArrayList<ModelRollNo> getModelRollNos() {
        return modelRollNos;
    }

    public void setModelRollNos(ArrayList<ModelRollNo> modelRollNos) {
        this.modelRollNos = modelRollNos;
    }
}
