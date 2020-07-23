package com.artur.private_messenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.artur.private_messenger.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {


    private ListView messageListView;
    private AwesomeMessageAdapter awesomeMessageAdapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private EditText messageEditText;
    private Button sendMessageButton;

    private String userName;
    private String recipientUserId;
    private String recipientUserName;

    private static final int RC_IMAGE_PICKER = 123;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ChildEventListener messagesChildEventListener;
    private DatabaseReference messagesDatabaseReference;
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;

    private FirebaseStorage storage;
    private StorageReference chatImagesStorageReference;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        getIntentFromUserListActivity(intent);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        messagesDatabaseReference = database.getReference().child("messages");
        usersDatabaseReference = database.getReference().child("users");
        chatImagesStorageReference = storage.getReference().child("chat_images");

        progressBar = findViewById(R.id.messageProgressBar);
        sendImageButton =  findViewById(R.id.sendPhotoButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);



        messageListView = findViewById(R.id.messageListView);
        List<AwesomeMessage> awesomeMessages = new ArrayList<>();
        awesomeMessageAdapter = new AwesomeMessageAdapter(this, R.layout.message_item,
                awesomeMessages);
        messageListView.setAdapter(awesomeMessageAdapter);

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() > 0){
                    sendMessageButton.setEnabled(true);
                }
                else{
                    sendMessageButton.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});

        sendMessageButton.setOnClickListener((View v) -> {
            AwesomeMessage message = new AwesomeMessage();
            message.setText(messageEditText.getText().toString());
            message.setName(userName);
            message.setSender(auth.getCurrentUser().getUid());
            message.setRecipient(recipientUserId);
            message.setImageUrl(null);

            messagesDatabaseReference.push().setValue(message);

            messageEditText.setText("");
        });

        sendImageButton.setOnClickListener((View v) -> {
            Intent imageContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
            imageContentIntent.setType("image/jpeg");

            imageContentIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(imageContentIntent, "Choose an image"), RC_IMAGE_PICKER);

        });
        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                AwesomeMessage awesomeMessage = dataSnapshot.getValue(AwesomeMessage.class);

                if (awesomeMessage.getSender().equals(auth.getCurrentUser().getUid())
                        && awesomeMessage.getRecipient().equals(recipientUserId)) {
                    awesomeMessage.setMine(true);
                    awesomeMessageAdapter.add(awesomeMessage);

                }
                else if (awesomeMessage.getRecipient().equals(auth.getCurrentUser().getUid())
                        && awesomeMessage.getSender().equals(recipientUserId)) {
                    awesomeMessage.setMine(false);
                    awesomeMessageAdapter.add(awesomeMessage);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        };

    messagesDatabaseReference.addChildEventListener(messagesChildEventListener);

    usersChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            User user = dataSnapshot.getValue(User.class);
            if(user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                userName = user.getName();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    usersDatabaseReference.addChildEventListener(usersChildEventListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
           final StorageReference imageReference = chatImagesStorageReference
                    .child(selectedImageUri.getLastPathSegment());

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    AwesomeMessage message = new AwesomeMessage();
                    message.setImageUrl(downloadUri.toString());
                    message.setName(userName);
                    message.setRecipient(recipientUserId);
                    message.setSender(auth.getCurrentUser().getUid());
                    messagesDatabaseReference.push().setValue(message);
                } else {
                }
            });
        }
    }

    public void getIntentFromUserListActivity(Intent intent){
        if(intent != null){
            recipientUserId = intent.getStringExtra("recipientUserId");
            userName = intent.getStringExtra("userName");
            recipientUserName = intent.getStringExtra("recipientUserName");
            setTitle("Chat with " + recipientUserName);
        }
    }
}
