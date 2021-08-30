package com.example.unipin.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.unipin.R;
import com.example.unipin.model.ProfileModel;
import com.example.unipin.model.UniversityModel;
import com.example.unipin.adapters.UniversityAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Universities extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseUser user;
    FirebaseAuth mAuth;
    RecyclerView mUniversitiesList;
    ArrayList<UniversityModel> mUni;
    UniversityAdapter mUniversityAdapter;
    LinearLayoutManager mLinearLayoutManager;
    ProgressDialog progressDialog;
    DatabaseReference databaseReference;
    NavigationView navigationView;
    private DrawerLayout drawer;
    ImageView profilePicture;
    TextView profileName,profileEmail;

    private DatabaseReference profileRef;
    private String currentUser;
    boolean count = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_universities);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        if (savedInstanceState == null) {
            hideKeyboard(this);


        }

        init();
        loadFireBaseData();

        checkForProgress();

        profileRef.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                     ProfileModel profileModel=snapshot.getValue(ProfileModel.class);
                    if(currentUser.equals(profileModel.getEmail())) {
                       //profilePicture=(CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image);
                        profileEmail=(TextView) navigationView.getHeaderView(0).findViewById(R.id.profile_email);
                        profileName=(TextView) navigationView.getHeaderView(0).findViewById(R.id.profile_name);
                       // Glide.with(MainActivity.this).load(picture).into(profilePicture);
                        profileName.setText(profileModel.getFirstName()+ " "+profileModel.getLastName());
                        profileEmail.setText(profileModel.getEmail());
                    }



                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void init() {
        currentUser=mAuth.getCurrentUser().getEmail();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        profileRef = FirebaseDatabase.getInstance().getReference();
        mUniversitiesList = findViewById(R.id.my_uni_view);
        mUni = new ArrayList<UniversityModel>();
        mLinearLayoutManager= new LinearLayoutManager(this);
        mUniversitiesList.setLayoutManager(mLinearLayoutManager);
        progressDialog = new ProgressDialog(Universities.this);
        progressDialog.setMessage("Data loading");
        progressDialog.show();

    }

    private void loadFireBaseData() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("universities");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    progressDialog.dismiss();
                    count = true;
                    UniversityModel univ = dataSnapshot.getValue(UniversityModel.class);
                    mUni.add(univ);
                    mUniversityAdapter = new UniversityAdapter(mUni,Universities.this);
                    mUniversityAdapter.notifyDataSetChanged();
                    mUniversitiesList.setAdapter(mUniversityAdapter);

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
        });

    }

    private void checkForProgress() {

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                if(snapshot.hasChild("universities")){
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Universities.this);
                    dialog.setIcon(R.drawable.exit_app);
                    dialog.setTitle("Exit Application");
                    dialog.setMessage("No Data Found! \nDo you want to exit App ?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    dialog.create();
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(user.getEmail().equalsIgnoreCase("shahid@gmail.com")){
            MenuInflater menuInflater = getMenuInflater();
            menuInflater.inflate(R.menu.add_university,menu);
            return true;
        }else {
            return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add:
                Intent intent = new Intent(Universities.this, AddUniversity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String currentUser =user.getEmail();
        switch (item.getItemId()) {

            case R.id.nav_profile:
                hideKeyboard(this);
                Intent i = new Intent(Universities.this,myProfile.class);
                i.putExtra("email",currentUser);
                startActivity(i);
                finish();


                break;
            case R.id.nav_logout:
            {
                AlertDialog.Builder ad =new AlertDialog.Builder(Universities.this);
                ad.setTitle("App will log you out");
                ad.setMessage("Are u sure you want to log out?");
                ad.setCancelable(false);
                ad.setIcon(R.drawable.ic_baseline_warning_24);
                ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseAuth.getInstance().signOut();
                        Intent backIntent = new Intent(Universities.this,LoginScreen.class);
                        startActivity(backIntent);
                        finish();
                    }
                });
                ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                ad.show();
            }

            break;

            case R.id.nav_quit:
            {
                AlertDialog.Builder ad =new AlertDialog.Builder(Universities.this);
                ad.setTitle("App will close");
                ad.setMessage("Are u sure you want to exit?");
                ad.setCancelable(false);
                ad.setIcon(R.drawable.ic_baseline_warning_24);
                ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finishAndRemoveTask();
                    }
                });
                ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                ad.show();
            }
            break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}