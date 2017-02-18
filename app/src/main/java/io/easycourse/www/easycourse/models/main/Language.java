package io.easycourse.www.easycourse.models.main;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import io.easycourse.www.easycourse.utils.ListsUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

public class Language extends RealmObject {

    @PrimaryKey
    private String code;
    private String name;
    private String translation;
    private boolean isChecked;

    public Language() {
    }

    public Language(String code) {
        this.code = code;
    }

    public Language(String name, String code, String translation) {
        this.name = name;
        this.code = code;
        this.translation = translation;
        this.isChecked = false;
    }

    public static Language getLanguageByCode (String code, Realm realm) {
        RealmResults<Language> results = realm.where(Language.class).equalTo("code", code).findAll();
        return results.first();
    }

    public static void updateLanguageToRealm(Language language, Realm realm) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(language);
        realm.commitTransaction();
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

    public void setChecked(boolean checked, Realm realm) {
        realm.beginTransaction();
        isChecked = checked;
        realm.commitTransaction();
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getTranslation() {
        return translation;
    }

    public static ArrayList<String> getCheckedLanguageCodeArrayList(Realm realm) {
        RealmResults<Language> checkedLanguageList = realm.where(Language.class).equalTo("isChecked", true).findAll();
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < checkedLanguageList.size(); i++) {
            result.add(checkedLanguageList.get(i).getCode());
        }
        return result;
    }
}
