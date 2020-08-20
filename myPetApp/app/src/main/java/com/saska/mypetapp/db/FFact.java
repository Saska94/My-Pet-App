package com.saska.mypetapp.db;

import com.amazonaws.amplify.generated.graphql.ListFFactsQuery;

public class FFact {

    private String id;
    private String text;

    public FFact(){};

    public FFact(String text){
        this.text = text;
    }

    public FFact(ListFFactsQuery.Item ffact){
        this.id = ffact.id();
        this.text = ffact.text();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }
}
