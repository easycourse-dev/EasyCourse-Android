package com.example.markwen.easycourse.models.main;

import org.json.JSONArray;
import org.json.JSONException;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

import static com.example.markwen.easycourse.utils.ListsUtils.isLanguageInList;

/**
 * Created by noahrinehart on 10/29/16.
 */

public class Language extends RealmObject {

    private String name;
    private String code;
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

    public static Language getUserLanguages (String code, Realm realm) {
        RealmResults<Language> results = realm.where(Language.class).equalTo("code", code).findAll();
        return results.first();
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

    public static boolean isLanguageInRealm(Language language, Realm realm) {
        RealmResults<Language> results = realm.where(Language.class)
                .equalTo("code", language.getCode())
                .findAll();
        return results.size() != 0;
    }

    public static void deleteLanguageFromRealm(final Language language, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Language> results = realm.where(Language.class)
                        .equalTo("code", language.getCode())
                        .findAll();
                results.deleteAllFromRealm();
            }
        });
    }

    public static RealmList<Language> syncLanguage(RealmList<Language> list, JSONArray listJSON, Realm realm) {
        for (int i = 0; i < listJSON.length(); i++) {
            try {
                if (!isLanguageInList(list, listJSON.getString(i))) {
                    list.add(new Language(listJSON.getString(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < list.size(); i++) {
            if (!isLanguageInList(listJSON, list.get(i).getCode())) {
                list.remove(i);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            updateLanguageToRealm(list.get(i), realm);
        }

        return list;
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
