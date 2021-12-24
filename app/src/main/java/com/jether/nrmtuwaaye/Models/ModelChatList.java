package com.jether.nrmtuwaaye.Models;

public class ModelChatList {

    String id;//we need this id to get chatlist,sender/receiver uid.

    public ModelChatList() {
    }

    public ModelChatList(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
