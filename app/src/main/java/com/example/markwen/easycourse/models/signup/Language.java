package com.example.markwen.easycourse.models.signup;

/**
 * Created by noahrinehart on 10/29/16.
 */

public class Language {

    private String name;
    private int code;
    private boolean isChecked;

    public Language(String name) {
        this.name = name;
    }

    public Language(String name, int code) {
        this.name = name;
        this.code = code;
        this.isChecked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
