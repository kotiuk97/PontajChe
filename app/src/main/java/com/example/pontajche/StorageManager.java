package com.example.pontajche;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class StorageManager {

    private static final String SETTINGS_FOLDER_NAME = "settings_storage.txt";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String IS_REMEMBER = "rememberMe";
    private static final String IS_CONNECT = "connectAtStartup";


    private static StorageManager storageManager;
    private LoginProperties loginProperties;

    private StorageManager(){
        loginProperties = loadProperties();
    }

    public static StorageManager getInstance(){
        if (storageManager == null){
            storageManager = new StorageManager();
        }
        return storageManager;
    }

    private LoginProperties loadProperties(){
        FileReader fr = null;
        BufferedReader br = null;
        try{
            File file = new File(BaseActivity.INNER_TORAGE_PATH + "/" + SETTINGS_FOLDER_NAME);
            if (!file.exists()) return getEmptyLoginProperties();
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String jsonString = br.readLine();
            JSONObject jsonProperties = new JSONObject(jsonString);

            return convertJSONToLoginProperties(jsonProperties);

        }catch (Exception e){
            return getEmptyLoginProperties();
        }finally {
            if (br != null){
                try{
                    br.close();
                }catch (Exception e){}
            }
            if (fr != null){
                try{
                    fr.close();
                }catch (Exception e){}
            }
        }


    }

    public void saveProperties(LoginProperties loginProperties){
        this.loginProperties = loginProperties;
        JSONObject properties = getJSONObject(loginProperties);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try{
            File f = new File(BaseActivity.INNER_TORAGE_PATH + "/" + SETTINGS_FOLDER_NAME);
            fw = new FileWriter(f);
            bw = new BufferedWriter(fw);
            bw.write(properties.toString());
               }catch (Exception e){
            Log.e("",e.toString());
        }finally {
            if (bw != null){
                try{
                    bw.close();
                }catch (Exception e){}
            }
            if (fw != null){
                try{
                    fw.close();
                }catch (Exception e){}
            }
        }
    }

    private JSONObject getJSONObject(LoginProperties loginProperties){
        JSONObject properties = new JSONObject();
        try{
            properties.put(EMAIL, loginProperties.getEmail());
            properties.put(PASSWORD, loginProperties.getPassword());
            properties.put(IS_REMEMBER, loginProperties.isRememberMe());
            properties.put(IS_CONNECT, loginProperties.isConnectAtStartup());
        }catch (Exception e){
            Log.e("StorageManager", "Error saving data");
        }

        return properties;
    }

    private LoginProperties getEmptyLoginProperties(){
        return new LoginProperties("","", false, false);
    }

    private LoginProperties convertJSONToLoginProperties(JSONObject jsonObject){
        try{
            String email = jsonObject.getString(EMAIL);
            String pass = jsonObject.getString(PASSWORD);
            boolean isRemember = jsonObject.getBoolean(IS_REMEMBER);
            boolean isConnect = jsonObject.getBoolean(IS_CONNECT);

           return new LoginProperties(email, pass, isRemember, isConnect);
        }catch (Exception e){
            return getEmptyLoginProperties();
        }
    }

    public LoginProperties getLoginProperties() {
        return loginProperties;
    }

}
