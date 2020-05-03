package com.saska.mypetapp.singletons;

import com.saska.mypetapp.db.User;

public class AppContext {

    private static AppContext context = null;

    private User activeUser;
    private String username;

    private AppContext(){}

    public static AppContext getContext(){
        if (context == null){
            context = new AppContext();
        }
        return context;
    }

    public void clearContext(){
        context = null;
    }

    public User getActiveUser() {
        return activeUser;
    }

    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
