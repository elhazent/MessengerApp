package com.elhazent.picodiploma.messenger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends AppCompatActivity {

    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_signup)
    Button btnSignup;
//    declare firebase user for auto login
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        check for user is loged
        if (firebaseUser != null){
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @OnClick({R.id.btn_login, R.id.btn_signup})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
                break;
            case R.id.btn_signup:
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
                break;
        }
    }
}
