package com.example.workflow.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPref {
    public static final String OPERATOR_NAME="operator_name";
    public static final String OPERATION="operation";

    static SharedPreferences getPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public static void setOperatorName(Context context, String operatorName){
        SharedPreferences.Editor editor=getPreferences(context).edit();
        editor.putString(OPERATOR_NAME,operatorName);
        editor.apply();
    }
    public static void setOperation(Context context,String operation){
        SharedPreferences.Editor editor=getPreferences(context).edit();
        editor.putString(OPERATION,operation);
        editor.apply();
    }

    public static String getOperatorName(Context context){
        return  getPreferences(context).getString(OPERATOR_NAME,"DEFAULT");
    }
    public static String getOperation(Context context){
        return  getPreferences(context).getString(OPERATION,"DEFAULT");
    }

}
