package com.elhazent.picodiploma.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {


    FirebaseAuth auth;
    DatabaseReference reference;
    @BindView(R.id.edt_username)
    EditText edtUsername;
    @BindView(R.id.edt_email)
    EditText edtEmail;
    @BindView(R.id.edt_password)
    EditText edtPassword;
    @BindView(R.id.btn_register)
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

    }

    private void register(final String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("id", userid);
                            map.put("username", username);
                            map.put("imageURL", "no");
                            map.put("status", "offline");
                            map.put("search", username.toLowerCase());

                            reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @OnClick(R.id.btn_register)
    public void onViewClicked() {
        String username = edtUsername.getText().toString();
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
        } else {
            register(username, email, password);
        }
    }
}
