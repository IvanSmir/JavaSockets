package com.ivansm.sockets.models;


public class Posts {

    int id;
    int userId;
    String title;
    String body;

    public Posts(int i, int ic, String t, String b) {
        this.id = i;
        this.userId = ic;
        this.title = t;
        this.body = b;
    }
    @Override
    public String toString(){
        return "Post\nID: "+this.id+"\nTitulo: "+this.title+"\nCuerpo: "+this.body;
    }
}
