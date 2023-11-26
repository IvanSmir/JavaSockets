package com.ivansm.sockets.models;


public class Posts {

    int id;
    int userID;
    String title;
    String body;

    public Posts(int i, int ic, String t, String b) {
        this.id = i;
        this.userID = ic;
        this.title = t;
        this.body = b;
    }

    public String toString(){
        return "Post\nID: "+this.id+"\nTitulo: "+this.title+"\nCuerpo: "+this.body;
    }
}
