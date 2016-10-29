package com.example.markwen.easycourse.models;

/**
 * Created by noahrinehart on 10/29/16.
 */

public class Language {

    private String name;
    private int code;

    public Language(String name) {
        this.name = name;
    }

    public Language(String name, int code) {
        this.name = name;
        this.code = code;
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

}
