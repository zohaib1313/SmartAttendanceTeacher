package com.zohaib.smartattandance.models;

public class ModelStudents {


    private String name, rollNo, deviceId;
    private boolean isRegistered;
    private String connectionEndpoint;
    private String endPointName;

    public ModelStudents(String name, String rollNo, String deviceId, boolean isRegistered, String connectionEndpoint, String endPointName, String cellIdInSpreadSheet) {
        this.name = name;
        this.rollNo = rollNo;
        this.deviceId = deviceId;
        this.isRegistered = isRegistered;
        this.connectionEndpoint = connectionEndpoint;
        this.endPointName = endPointName;
        this.cellIdInSpreadSheet = cellIdInSpreadSheet;
    }

    private String cellIdInSpreadSheet;


    public String getCellIdInSpreadSheet() {
        return cellIdInSpreadSheet;
    }

    public void setCellIdInSpreadSheet(String cellIdInSpreadSheet) {
        this.cellIdInSpreadSheet = cellIdInSpreadSheet;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }

    public String getConnectionEndpoint() {
        return connectionEndpoint;
    }

    public void setConnectionEndpoint(String connectionEndpoint) {
        this.connectionEndpoint = connectionEndpoint;
    }

    public String getEndPointName() {
        return endPointName;
    }

    public void setEndPointName(String endPointName) {
        this.endPointName = endPointName;
    }
}
