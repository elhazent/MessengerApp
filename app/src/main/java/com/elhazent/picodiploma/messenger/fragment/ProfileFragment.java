package com.elhazent.picodiploma.messenger.fragment;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.elhazent.picodiploma.messenger.R;
import com.elhazent.picodiploma.messenger.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {


    CircleImageView img;
    TextView username;
    DatabaseReference root;
    FirebaseUser firebaseUser;

    StorageReference referenceStrorage;
    private static final int IMAGE_REQUEST = 1;
    private Uri imgUri;
    StorageTask uploadtask;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        img = view.findViewById(R.id.imgprofile);
        username = view.findViewById(R.id.userprofile);


        referenceStrorage = FirebaseStorage.getInstance().getReference("uploads");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        root  = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("no")){
                    img.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Picasso.get().load(user.getImageURL()).into(img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImg();
            }
        });
        return view;
    }

    private void openImg() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtention(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading.....");
        pd.show();

        if (imgUri != null){
            final StorageReference fileReference = referenceStrorage.child(System.currentTimeMillis()
                    + "."+getFileExtention(imgUri));

            uploadtask = fileReference.putFile(imgUri);
            uploadtask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadurl = task.getResult();
                        String mUri = downloadurl.toString();

                        root = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("imageURL", mUri);
                        root.updateChildren(map);
                        pd.dismiss();
                    }else {
                        Toast.makeText(getContext(), "Failed..!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getLocalizedMessage()+", " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }else {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data!= null && data.getData() != null){
            imgUri = data.getData();

            if (uploadtask != null && uploadtask.isInProgress()){
                Toast.makeText(getContext(), "Upload in Progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }
}
