package com.elhazent.picodiploma.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.elhazent.picodiploma.messenger.fragment.ChatFragment;
import com.elhazent.picodiploma.messenger.fragment.ProfileFragment;
import com.elhazent.picodiploma.messenger.fragment.UserFragment;
import com.elhazent.picodiploma.messenger.model.User;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.profileimg)
    CircleImageView profileimg;
    @BindView(R.id.username)
    TextView username;
    @BindView(R.id.toolbaar)
    Toolbar toolbar;
    @BindView(R.id.tablayout)
    TabLayout tablayout;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseApp.initializeApp(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("no")){
                    profileimg.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Picasso.get().load(user.getImageURL()).into(profileimg);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //        todo 28 : create adapter for view pager
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
//        todo create 3 new fragments what we need
        viewPagerAdapter.addFragment(new ChatFragment(), "Chats");
        viewPagerAdapter.addFragment(new UserFragment(), "Users");
        viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
//        todo set adapter for viewpager
        viewpager.setAdapter(viewPagerAdapter);
//        todo create/set tablayout with view pager
        tablayout.setupWithViewPager(viewpager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }
        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragments;
        ArrayList<String> titles;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }
//todo dua
        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        //        todo create method for add new fragment
        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }
// todo satu
        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status){
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
        status("offline");
    }

}
