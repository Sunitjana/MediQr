package com.example.project.model;

import android.graphics.Bitmap;

public class deta_class {
    String name,phone,gender,addres,problem,bd_group,add_info;
    Bitmap image;
    public deta_class(String name, String phone, String gender, String addres, String problem, String bd_group, String add_info, Bitmap image) {
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.addres = addres;
        this.problem = problem;
        this.bd_group = bd_group;
        this.add_info = add_info;
        this.image = image;
    }

    public deta_class(String name, String phone, String gender, String addres, String problem, String bd_group, String add_info) {
        this.name = name;
        this.phone = phone;
        this.gender = gender;
        this.addres = addres;
        this.problem = problem;
        this.bd_group = bd_group;
        this.add_info = add_info;
    }

    public deta_class() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }



    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddres() {
        return addres;
    }

    public void setAddres(String addres) {
        this.addres = addres;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getBd_group() {
        return bd_group;
    }

    public void setBd_group(String bd_group) {
        this.bd_group = bd_group;
    }

    public String getAdd_info() {
        return add_info;
    }

    public void setAdd_info(String add_info) {
        this.add_info = add_info;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
