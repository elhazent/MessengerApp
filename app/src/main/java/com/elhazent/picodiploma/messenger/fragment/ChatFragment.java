package com.elhazent.picodiploma.messenger.fragment;


import android.os.Bundle;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elhazent.picodiploma.messenger.R;
import com.elhazent.picodiploma.messenger.adapter.UserAdapter;
import com.elhazent.picodiploma.messenger.model.Chatlist;
import com.elhazent.picodiploma.messenger.model.User;
import com.elhazent.picodiploma.messenger.notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> mUsers;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    List<Chatlist> chatlists;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.recycleChat);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        chatlists = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlists.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    Chatlist chat = ds.getValue(Chatlist.class);
                    chatlists.add(chat);
                }
                chatlist();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void chatlist() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    for (Chatlist chatlist:chatlists){
                        if (user.getId().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }
                adapter = new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
