package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    EditText mEmail,mPassword;
    Button mRegisterBn;
    TextView mHaveCount;

    //progressbar to display while registering.
    ProgressDialog progressDialog;

    //Declare  an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //action bar and its title
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create Account");

        //Enable the back Button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //in the on create() method initialise the FirebaseAuth instance;
        mAuth=FirebaseAuth.getInstance();

        //initialize

        mEmail=findViewById(R.id.emailEt);
        mPassword=findViewById(R.id.passwordEt);
        mRegisterBn= findViewById(R.id.registerBtn);
        mHaveCount=findViewById(R.id.have_accountTv);

        progressDialog= new ProgressDialog(this);
        progressDialog.setMessage("Registering User.....");


        //handle register btn click
        mRegisterBn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.FROYO)
            @Override
            public void onClick(View v) {
                //input email and password.
                String email=mEmail.getText().toString().trim();
                String password=mPassword.getText().toString().trim();

                //validate

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email edit text
                    mEmail.setError("Invalid Email");
                    mEmail.setFocusable(true);
                }else if(password.length()<6){

                    //set error and focus to password edit text
                    mPassword.setError("Password length at least 6 characters");
                    mPassword.setFocusable(true);
                }else{
                    registerUser(email,password);
                }
            }
        });

        mHaveCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });
    }






    private void registerUser(String email, String password) {
        //email and password valid show progress dialog and start registering user.
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete( @NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    //sign in is a success,dismiss dailog and start register activity.
                    progressDialog.dismiss();
                    FirebaseUser user= mAuth.getCurrentUser();
                    //get user emailn and uid from auth
                    String email=user.getEmail();
                    String uid=user.getUid();
                    //when the user is stored store his info in the realtime database too
                    //using hashmap

                    HashMap<Object,String> hashMap= new HashMap<>();
                    hashMap.put("email",email);
                    hashMap.put("uid",uid);
                    hashMap.put("name","");
                    hashMap.put("onlineStatus","online");
                    hashMap.put("typingTo","noOne");
                    hashMap.put("phone","");//
                    hashMap.put("image","");
                    hashMap.put("cover","");

                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                    //put data within hashmap in database
                    ref.child(uid).setValue(hashMap);


                    Toast.makeText(RegisterActivity.this,"Registered....."+user.getEmail(),Toast.LENGTH_SHORT).show();
                    startActivity( new Intent(RegisterActivity.this,DashBoardActivity.class));
                    finish();

                }else{
                    //if sign in fails display message to user
                    Toast.makeText(RegisterActivity.this,"Authentication Failed",Toast.LENGTH_SHORT).show();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error,dismiss progress dialog and get the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();//go to previous activity
        return super.onSupportNavigateUp();
    }
}
