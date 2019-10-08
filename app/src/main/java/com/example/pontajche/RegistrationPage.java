package com.example.pontajche;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationPage extends BaseActivity {

    private EditText email;
    private EditText pass;
    private EditText pass2;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_page);
        initFields();
        mAuth = FirebaseAuth.getInstance();

    }

    public void onClickRegistration(View view){
        if (isFieldsEmpty()){
            Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show();
        }else if (!pass.getText().toString().equals(pass2.getText().toString())) {
            Toast.makeText(this, "Passwords are different!", Toast.LENGTH_SHORT).show();
            pass.setText("");
            pass2.setText("");
        } else{
            showProgressDialog("Creating a new account");
            String emailStr = email.getText().toString() + "@computervoice.ro";
            registrationAction(emailStr, pass.getText().toString());
        }
    }

    private void initFields(){
        email = findViewById(R.id.registration_email_textfield);
        email.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    email.clearFocus();
                    pass.requestFocus();
                }
                return false;
            }
        });

        pass = findViewById(R.id.registration_passwordfield);
        pass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
//                    pass.clearFocus();
//                    pass2.requestFocus();
                }
                return false;
            }
        });
        pass2 = findViewById(R.id.registration_passwordfield2);
        pass2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    pass2.clearFocus();

                    //hide keyboard
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });
    }

    private boolean isFieldsEmpty(){
        return email.getText().toString().isEmpty() ||
                pass.getText().toString().isEmpty() ||
                pass2.getText().toString().isEmpty();
    }

    private void registrationAction(String email, String pass){
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FireBaseAuth", "createUserWithEmail:success");
//                    FirebaseUser user = mAuth.getCurrentUser();
//                    openLoginPage();
                    RegistrationPage.this.finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FireBaseAuth", "createUserWithEmail:failure", task.getException());
                    Toast.makeText(RegistrationPage.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();

                }
                hideProgressDialog();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }

//    private void openLoginPage(){
//        Intent intent = new Intent(this, LoginPage.class);
//        startActivity(intent);
//    }
}
