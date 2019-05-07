package com.elhazent.picodiploma.messenger.notification;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String refreshtoken = FirebaseInstanceId.getInstance().getToken();
        if (firebaseUser != null){
            updateToken(refreshtoken);
        }
    }

    private void updateToken(String refreshtoken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference root = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshtoken);
        root.child(firebaseUser.getUid()).setValue(token);
    }
}
