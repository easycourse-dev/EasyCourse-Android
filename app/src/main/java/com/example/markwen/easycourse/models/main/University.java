package com.example.markwen.easycourse.models.main;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahrinehart on 11/5/16.
 */

public class University extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;

    public University(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
