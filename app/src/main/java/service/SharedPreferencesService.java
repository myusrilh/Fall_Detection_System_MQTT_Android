package service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

//import org.jetbrains.annotations.NotNull;

public class SharedPreferencesService {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private final String CATEGORY = "CATEGORY";
    private final String ROLE = "ROLE";
    public final String DEFAULT_VALUE_STRING = "";

    public SharedPreferencesService(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void clearData(){
        sharedPreferences.edit().remove(CATEGORY).apply();
        sharedPreferences.edit().remove(ROLE).apply();
    }

    public void saveRole(String value){
        editor = sharedPreferences.edit();
        editor.putString(ROLE,value);
        editor.apply();
    }

    @NonNull
    public String loadRole(){
        return sharedPreferences.getString(ROLE, DEFAULT_VALUE_STRING);
    }
}
