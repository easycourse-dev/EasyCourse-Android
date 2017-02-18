package io.easycourse.www.easycourse.models.signup;


public class Language {

    private String name;
    private String code;
    private String translation;
    private boolean isChecked;

    public Language(String name) {
        this.name = name;
    }

    public Language(String name, String code, String translation) {
        this.name = name;
        this.code = code;
        this.translation = translation;
        this.isChecked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }
}
