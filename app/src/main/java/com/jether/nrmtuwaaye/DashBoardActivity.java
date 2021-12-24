package com.jether.nrmtuwaaye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jether.nrmtuwaaye.Fragments.ChatListFragment;
import com.jether.nrmtuwaaye.Fragments.GroupChatFragment;
import com.jether.nrmtuwaaye.Fragments.HomeFragment;
import com.jether.nrmtuwaaye.Fragments.NewsFragment;
import com.jether.nrmtuwaaye.Fragments.ProfileFragment;
import com.jether.nrmtuwaaye.Fragments.UsersFragment;

import eu.dkaratzas.android.inapp.update.Constants;
import eu.dkaratzas.android.inapp.update.InAppUpdateManager;
import eu.dkaratzas.android.inapp.update.InAppUpdateStatus;

public class DashBoardActivity extends AppCompatActivity implements InAppUpdateManager.InAppUpdateHandler {
    ActionBar actionBar;

    InAppUpdateManager inAppUpdateManager;


    private FirebaseAuth firebaseAuth;

    String mUID;

    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //actionbar and itd title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        firebaseAuth = FirebaseAuth.getInstance();

        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        //home fragment default on start
        actionBar.setTitle("Home");
        HomeFragment fragment1= new HomeFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();


        inAppUpdateManager=InAppUpdateManager.Builder(this,101)
                .resumeUpdates(true)
                .mode(Constants.UpdateMode.IMMEDIATE)
                .snackBarAction("Update has just been downloaded")
                .snackBarAction("Restart")
                .handler(this);


                inAppUpdateManager.checkForAppUpdate();


    }







    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // handle item clicks

                    switch(item.getItemId()){
                        case R.id.nav_home:
                            actionBar.setTitle("Home");
                            HomeFragment fragment1= new HomeFragment();
                            FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();
                            return true;

                        case R.id.nav_profile:
                            actionBar.setTitle("Profile");
                            ProfileFragment fragment2= new ProfileFragment();
                            FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;

                        case R.id.nav_users:
                            actionBar.setTitle("Users");
                            UsersFragment fragment3= new UsersFragment();
                            FragmentTransaction ft3=getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;

                        case R.id.nav_chat:
                            actionBar.setTitle("Chat");
                            ChatListFragment fragment4= new ChatListFragment();
                            FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4, "");
                            ft4.commit();
                            return true;

                            case R.id.nav_more:
                            showMoreOptions();
                            return true;


                    }
                    return false;
                }
            };

    private void showMoreOptions() {
        //pop up menu to show options
        PopupMenu popupMenu= new PopupMenu(this,navigationView, Gravity.END);
        //items to show in pop up menu
        popupMenu.getMenu().add(Menu.NONE,0,0,"Group Chats");

        //menu clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id=menuItem.getItemId();

                if(id==0){
                    //group chat clicked
                    actionBar.setTitle("Group Chats");
                    GroupChatFragment fragment5= new GroupChatFragment();
                    FragmentTransaction ft5=getSupportFragmentManager().beginTransaction();
                    ft5.replace(R.id.content, fragment5, "");
                    ft5.commit();
                    return true;


                }
                return false;
            }
        });
        popupMenu.show();
    }


    private void checkUserStatus(){
        //get current user

        FirebaseUser user=firebaseAuth.getCurrentUser();

        if(user!=null){
            //user is signed in stay here
            //profileTv.setText(user.getEmail());




        }else{
            //user is not signed in go to main activity
            startActivity(new Intent(DashBoardActivity.this,MainActivity.class));
            finish();

        }
    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();
    }

    //inflate options menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //handle menu item clicks


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInAppUpdateError(int code, Throwable error) {

    }

    @Override
    public void onInAppUpdateStatus(InAppUpdateStatus status) {
        if(status.isDownloaded()) {
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            Snackbar snackbar = Snackbar.make(view,
                    "An update has just been downloaded.",
                    Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction("", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inAppUpdateManager.completeUpdate();
                }
            });

            snackbar.show();
        }
    }
}
