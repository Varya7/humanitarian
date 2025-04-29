package com.example.hum1;

public class CenterApp {
    private String center, id_appl;
    public CenterApp(String center, String id_appl){
        this.center = center;
        this.id_appl = id_appl;
    }

    public String getCenter(){
        return this.center;
    }
    public void setCenter(String center){
        this.center = center;
    }
    public String getId_appl(){
        return this.id_appl;
    }
    public void setId_appl(String id_appl){
        this.id_appl = id_appl;
    }
}
