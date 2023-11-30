package com.ivansm.sockets.models;
public class User {
       
    int id;
    String name;
    String email;
    String phone;

    public User(int i, String n, String e, String p) {
        this.id = i;
        this.name = n;
        this.email = e;
        this.phone = p;
    }

    @Override
    public String toString(){
        return "Usuario: \nID: " + id + ",\nName: "+ name +",\nEmail: "+ email+",\nTelefono: "+ phone;
    }
}   
