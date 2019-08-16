package com.example.pontajche;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class LoginPage extends BaseActivity {

    private EditText emailField;
    private EditText passwordField;
    private CheckBox rememberCheckBox;

    private FirebaseAuth mAuth;
//    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final String SETTINGS_FOLDER_NAME = "pontaj_settings.txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        initFields();
        isRememberMe();
        mAuth = FirebaseAuth.getInstance();
//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user == null){
//                    Toast.makeText(LoginPage.this, "Forced log out", Toast.LENGTH_SHORT).show();
//                    Timer t = new Timer();
//                    TimerTask tt  = new TimerTask() {
//                        @Override
//                        public void run() {
//                            LoginPage.this.finish();
//                        }
//                    };
//                    t.schedule(tt, 5000);
//                }
//
//            }
//        };
    }


    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in_button:
                if (isFieldsEmpty()) {
                    Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
                }else {
                    showProgressDialog();
                    writeProperties(rememberCheckBox.isChecked());
                    String emailStr = emailField.getText().toString() + "@computervoice.ro";
                    signInAction(emailStr, passwordField.getText().toString());
                }
                break;
            case R.id.sign_up_button:
                openRegistrationPage();
                break;
            default: break;
        }
    }

    private void openRegistrationPage(){
        Intent intent = new Intent(this, RegistrationPage.class);
        startActivity(intent);
    }

    private void signInAction(String email, String pass){
      mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
              if (task.isSuccessful()) {
                  // Sign in success, update UI with the signed-in user's information
                  Log.d("LoginPage", "signInWithEmail:success");
                  openMainPage();
                  LoginPage.this.finish();
              } else {
                  // If sign in fails, display a message to the user.
                  Log.w("LoginPage", "signInWithEmail:failure", task.getException());
                  Toast.makeText(LoginPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
              }
              hideProgressDialog();
          }
      });
    }

    private boolean isFieldsEmpty(){
        return  emailField.getText().toString().isEmpty() ||
                passwordField.getText().toString().isEmpty();
    }

    private void initFields(){
        emailField = findViewById(R.id.email_textfield);
        emailField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    emailField.clearFocus();
                    passwordField.requestFocus();
                }
                return false;
            }
        });

        passwordField = findViewById(R.id.passwordfield);
        passwordField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    passwordField.clearFocus();

                    //hide keyboard
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        rememberCheckBox = findViewById(R.id.remember_checkbox);
    }

    private void openMainPage(){
        Intent intent = new Intent(this, MainPage.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
//        if (mAuthListener != null){
//            mAuth.removeAuthStateListener(mAuthListener);
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mAuth.addAuthStateListener(mAuthListener);
    }

    private boolean writeProperties(boolean remember){
        String value = remember ? "true" : "false";
        StringBuffer string = new StringBuffer("rememberMe : " + value);
        if (remember){
            string.append("\n" + emailField.getText().toString());
            string.append("\n" + passwordField.getText().toString());
        }
        FileOutputStream fos = null;
        try{
            fos = openFileOutput(SETTINGS_FOLDER_NAME, MODE_PRIVATE);
            fos.write(string.toString().getBytes());
            fos.close();
        }catch (Exception e){
            return false;
        }

        return true;
    }

    private boolean isRememberMe(){
        boolean result = false;
        FileInputStream fis = null;
        try{
            fis = openFileInput(SETTINGS_FOLDER_NAME);
            if (fis != null){
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                String shouldRemember = br.readLine();
                if (shouldRemember != null && !shouldRemember.isEmpty()){
                    if (shouldRemember.indexOf("true") > 0){
                        rememberCheckBox.setChecked(true);
                        emailField.setText(br.readLine());
                        passwordField.setText(br.readLine());
                        result = true;
                    }
                }
                br.close();
                isr.close();
                fis.close();
            }

            return result;
        }catch (Exception e){
            return false;
        }
    }
}
