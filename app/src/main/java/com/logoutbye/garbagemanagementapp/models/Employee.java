package com.logoutbye.garbagemanagementapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Employee implements Parcelable {


    private String name;
    private String email;
    private long employeeContact;
    private String employeeID;
    private String employeeOfficeLoc;
    private String employeeWorkLoc;
    private String employeeAttendance;
    private long reg_status;
    private long employeePoints;
    private double employeeEcoCash;
    private long donation_status;
    private String token;

    public Employee() {

    }

    public Employee(String name, String email, long employeeContact, String employeeID, String employeeOfficeLoc, String employeeWorkLoc, String employeeAttendance, long reg_status, long employeePoints, double employeeEcoCash, long donation_status, String token) {
        this.name = name;
        this.email = email;
        this.employeeContact = employeeContact;
        this.employeeID = employeeID;
        this.employeeOfficeLoc = employeeOfficeLoc;
        this.employeeWorkLoc = employeeWorkLoc;
        this.employeeAttendance = employeeAttendance;
        this.reg_status = reg_status;
        this.employeePoints = employeePoints;
        this.employeeEcoCash = employeeEcoCash;
        this.donation_status = donation_status;
        this.token = token;
    }

    protected Employee(Parcel in) {
        name = in.readString();
        email = in.readString();
        employeeContact = in.readLong();
        employeeID = in.readString();
        employeeOfficeLoc = in.readString();
        employeeWorkLoc = in.readString();
        employeeAttendance = in.readString();
        reg_status = in.readLong();
        employeePoints = in.readLong();
        employeeEcoCash = in.readDouble();
        donation_status = in.readLong();
        token = in.readString();
    }

    public static final Creator<Employee> CREATOR = new Creator<Employee>() {
        @Override
        public Employee createFromParcel(Parcel in) {
            return new Employee(in);
        }

        @Override
        public Employee[] newArray(int size) {
            return new Employee[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getEmployeeContact() {
        return employeeContact;
    }

    public void setEmployeeContact(long employeeContact) {
        this.employeeContact = employeeContact;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getEmployeeOfficeLoc() {
        return employeeOfficeLoc;
    }

    public void setEmployeeOfficeLoc(String employeeOfficeLoc) {
        this.employeeOfficeLoc = employeeOfficeLoc;
    }

    public String getEmployeeWorkLoc() {
        return employeeWorkLoc;
    }

    public void setEmployeeWorkLoc(String employeeWorkLoc) {
        this.employeeWorkLoc = employeeWorkLoc;
    }

    public String getEmployeeAttendance() {
        return employeeAttendance;
    }

    public void setEmployeeAttendance(String employeeAttendance) {
        this.employeeAttendance = employeeAttendance;
    }

    public long getReg_status() {
        return reg_status;
    }

    public void setReg_status(long reg_status) {
        this.reg_status = reg_status;
    }

    public long getEmployeePoints() {
        return employeePoints;
    }

    public void setEmployeePoints(long employeePoints) {
        this.employeePoints = employeePoints;
    }

    public double getEmployeeEcoCash() {
        return employeeEcoCash;
    }

    public void setEmployeeEcoCash(double employeeEcoCash) {
        this.employeeEcoCash = employeeEcoCash;
    }

    public long getDonation_status() {
        return donation_status;
    }

    public void setDonation_status(long donation_status) {
        this.donation_status = donation_status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeLong(employeeContact);
        dest.writeString(employeeID);
        dest.writeString(employeeOfficeLoc);
        dest.writeString(employeeWorkLoc);
        dest.writeString(employeeAttendance);
        dest.writeLong(reg_status);
        dest.writeLong(employeePoints);
        dest.writeDouble(employeeEcoCash);
        dest.writeLong(donation_status);
        dest.writeString(token);
    }
}

