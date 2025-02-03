package com.example.hum1;

public class Center {
    private String id, email, center_name, address, phone_number, fio, work_time, list;
    public Center(String id, String center_name, String address, String email, String fio, String work_time, String phone_number, String list){
        this.id = id;
        this.center_name = center_name;
        this.email = email;
        this.address = address;
        this.phone_number = phone_number;
        this.fio = fio;
        this.work_time = work_time;
        this.list = list;
    }
    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getCenter_name(){
        return this.center_name;
    }
    public void setCenter_name(String center_name){
        this.center_name = center_name;
    }
    public String getAddress(){
        return this.address;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public String getEmail(){
        return this.email;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public String getFIO(){
        return this.fio;
    }
    public void setFIO(String fio){
        this.fio = fio;
    }
    public String getWorkTime(){
        return this.work_time;
    }
    public void setWork_time(String work_time){
        this.work_time = work_time;
    }
    public String getPhone_number(){
        return this.phone_number;
    }
    public void setPhone_number(String phone_number){
        this.phone_number = phone_number;
    }
    public String getList(){
        return this.list;
    }
    public void setList(String list){
        this.list = list;
    }
}
