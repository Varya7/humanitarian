package com.example.hum1;

public class ApplicationU {
    private String id_appl, date, time, email, fio, phone_number, birth, family_members, list, status, center;
    public ApplicationU(String id_appl, String date, String time, String email, String fio, String phone_number, String birth, String family_members, String list, String status, String center){
        this.id_appl = id_appl;
        this.date = date;
        this.time = time;
        this.email = email;
        this.fio = fio;
        this.phone_number = phone_number;
        this.birth = birth;
        this.family_members = family_members;
        this.list = list;
        this.status = status;
        this.center = center;
    }

    public String getId_appl(){
        return this.id_appl;
    }
    public void setId_appl(String id_appl){
        this.id_appl = id_appl;
    }

    public String getDate(){
        return this.date;
    }
    public void setDate(String date){
        this.date = date;
    }
    public String getTime(){
        return this.time;
    }
    public void setTime(String time){
        this.time = time;
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

    public String getPhone_number(){
        return this.phone_number;
    }
    public void setPhone_number(String phone_number){
        this.phone_number = phone_number;
    }

    public String getBirth(){
        return this.birth;
    }
    public void setBirth(String birth){
        this.birth = birth;
    }
    public String getList(){
        return this.list;
    }
    public void setList(String list){
        this.list = list;
    }
    public String getFamily_members(){
        return this.family_members;
    }
    public void setFamily_members(String family_members){
        this.family_members = family_members;
    }
    public String getStatus(){
        return this.status;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getCenter(){
        return this.center;
    }
    public void setCenter(String center){
        this.center = center;
    }
}
