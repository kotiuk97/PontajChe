package com.example.pontajche;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends BaseActivity{

    private EditText emailField;
    private EditText passwordField;
    private CheckBox rememberCheckBox;
    private CheckBox connectCheckBox;

    private FirebaseAuth mAuth;
    private LoginProperties loginProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        mAuth = FirebaseAuth.getInstance();
        initFields();
        loadSettings();
        checkIsConnectAtStartup();
    }

    private void loadSettings() {
        loginProperties = StorageManager.getInstance().getLoginProperties();
        if (loginProperties.isRememberMe()){
            emailField.setText(loginProperties.getEmail());
            passwordField.setText(loginProperties.getPassword());
            rememberCheckBox.setChecked(loginProperties.isRememberMe());
            connectCheckBox.setChecked(loginProperties.isConnectAtStartup());
        }
    }

    private void checkIsConnectAtStartup() {
        if (loginProperties.isConnectAtStartup()){
            String emailStr = loginProperties.getEmail() + "@computervoice.ro";
            signInAction(emailStr, loginProperties.getPassword());
        }
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in_button:
                if (isFieldsEmpty()) {
                    Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
                }else {
                    showProgressDialog("Validating your account");
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
                  StorageManager.getInstance().saveProperties(getCurrentLoginProperties());
                  openMainPage(task.getResult().getUser().getUid());
//                  LoginPage.this.finish();
              } else {
                  // If sign in fails, display a message to the user.
                  Toast.makeText(LoginPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
              }
              hideProgressDialog();
          }
      });
    }

    private LoginProperties getCurrentLoginProperties(){
        String email = emailField.getText().toString();
        String pass = passwordField.getText().toString();
        boolean isRemember = rememberCheckBox.isChecked();
        boolean isConnect = isRemember && connectCheckBox.isChecked();

        return new LoginProperties(email, pass, isRemember, isConnect);
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
        rememberCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                connectCheckBox.setEnabled(isChecked);
            }
        });

        connectCheckBox = findViewById(R.id.connect_checkbox);
        connectCheckBox.setEnabled(false);
    }

    private void openMainPage(String uid){
        Intent intent = new Intent(this, MainPage.class);
        intent.putExtra("uid", uid);
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

}
