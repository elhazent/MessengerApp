package com.elhazent.picodiploma.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResetActivity extends AppCompatActivity {

    @BindView(R.id.reset_email)
    EditText resetEmail;
    @BindView(R.id.btn_login)
    Button btnLogin;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.btn_login)
    public void onViewClicked() {
        String email = resetEmail.getText().toString();
        if (email.equals("")) {
            Toast.makeText(this, "Enter your Email address", Toast.LENGTH_SHORT).show();
        } else {
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetActivity.this, "Please check your email", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetActivity.this, LoginActivity.class));
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(ResetActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }
}
