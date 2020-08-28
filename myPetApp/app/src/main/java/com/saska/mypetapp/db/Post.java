package com.saska.mypetapp.db;

import com.amazonaws.amplify.generated.graphql.ListPostsQuery;

public class Post {

    private String id;
    private String text, picture, heading, createdAt;
    private User user;
    private int approved;

    public Post(){}

    public Post(String heading, String text, String picture, User user, String createdAt, int approved){
        this.heading = heading;
        this.text = text;
        this.picture = picture;
        this.user = user;
        this.createdAt = createdAt;
        this.approved = approved;
    }

    public Post(ListPostsQuery.Item post){
        this.id = post.id();
        this.text = post.text();
        this.picture = post.picture();
        this.heading = post.heading();
        this.user = new User(post.user());
        this.createdAt = post.createdAt();
        this.approved = post.approved();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }
}
