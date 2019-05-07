package com.elhazent.picodiploma.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elhazent.picodiploma.messenger.adapter.MessageAdapter;
import com.elhazent.picodiploma.messenger.fragment.APIService;
import com.elhazent.picodiploma.messenger.model.ChatItem;
import com.elhazent.picodiploma.messenger.model.User;
import com.elhazent.picodiploma.messenger.notification.Client;
import com.elhazent.picodiploma.messenger.notification.Data;
import com.elhazent.picodiploma.messenger.notification.MyResponse;
import com.elhazent.picodiploma.messenger.notification.Sender;
import com.elhazent.picodiploma.messenger.notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    @BindView(R.id.profileimgmsg)
    CircleImageView profileimgmsg;
    @BindView(R.id.usernamemsg)
    TextView usernamemsg;
    @BindView(R.id.toolber)
    Toolbar toolber;
    @BindView(R.id.recyclerM)
    RecyclerView recyclerM;
    @BindView(R.id.edtmessage)
    EditText edtmessage;
    @BindView(R.id.btnsend)
    ImageButton btnsend;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    List<ChatItem> list;
    Intent intent;
    ValueEventListener seenListener;
    String userid;
    boolean notify = false;
    private MessageAdapter adapter;
    private APIService apiservice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        setSupportActionBar(toolber);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolber.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // and this
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
// todo notif
        apiservice = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        recyclerM.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerM.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        userid = intent.getStringExtra("userId");


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                usernamemsg.setText(user.getUsername());
                if (user.getImageURL().equals("no")) {
                    profileimgmsg.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Picasso.get().load(user.getImageURL()).into(profileimgmsg);
                }

                readMessage(firebaseUser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);
    }

    private void seenMessage(final String userId) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ChatItem chat = ds.getValue(ChatItem.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("isseen", true);
                        ds.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage(final String id, final String userid, final String imgurl) {
        list = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ChatItem chat = ds.getValue(ChatItem.class);
                    if (chat.getReceiver().equals(id) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(id)) {
                        list.add(chat);
                    }

                    adapter = new MessageAdapter(MessageActivity.this, list, imgurl);
                    recyclerM.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @OnClick(R.id.btnsend)
    public void onViewClicked() {
        notify = true;
        String msg = edtmessage.getText().toString();
        if (!msg.equals("")) {
            sendMessage(firebaseUser.getUid(), userid, msg);
        } else {
            Toast.makeText(this, "You can't send empty message", Toast.LENGTH_SHORT).show();
        }
        edtmessage.setText("");
    }

    private void sendMessage(final String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("message", message);
        map.put("isseen", false);

        reference.child("Chats").push().setValue(map);


        // add user to chat fragment
        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(firebaseUser.getUid())
                .child(userid);
        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatReference.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

// todo notif
        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(sender, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String receiver, final String username, final String message) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.mipmap.ic_launcher, username + ": " + message, "New Message", userid);
                    Sender sender = new Sender(data, token.getToken());
                    apiservice.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("status", status);
        reference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }
}
