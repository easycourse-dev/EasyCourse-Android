package com.example.markwen.easycourse.models.signup;

/**
 * Created by markw on 10/29/2016.
 */

public class University {
    private String id = "";
    private String name = "";
    private boolean isSelected = false;

    public University(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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
