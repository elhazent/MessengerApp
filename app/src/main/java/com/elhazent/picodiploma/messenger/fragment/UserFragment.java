package com.elhazent.picodiploma.messenger.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.elhazent.picodiploma.messenger.R;
import com.elhazent.picodiploma.messenger.adapter.UserAdapter;
import com.elhazent.picodiploma.messenger.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class UserFragment extends Fragment {


    List<User> mUser;
    RecyclerView recyclerUser;
    private UserAdapter adapter;
    EditText edtsearch;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user, container, false);
        // Inflate the layout for this fragment
        recyclerUser = view.findViewById(R.id.recyclerUser);
        recyclerUser.setHasFixedSize(true);
        recyclerUser.setLayoutManager(new LinearLayoutManager(getContext()));
        mUser = new ArrayList<>();
        edtsearch = view.findViewById(R.id.searchuser);
        edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        readUser();
        return view;
    }
    private void searchUser(String s) {
        final FirebaseUser firebaseUsers = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    if(!user.getId().equals(firebaseUsers.getUid())){
                        mUser.add(user);
                    }
                }

                adapter = new UserAdapter(getContext(), mUser, false);
                recyclerUser.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUser() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (edtsearch.getText().toString().equals("")) {
                    mUser.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        if (!user.getId().equals(firebaseUser.getUid())) {
                            mUser.add(user);
                        }
                    }
                    adapter = new UserAdapter(getContext(), mUser, false);
                    recyclerUser.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
