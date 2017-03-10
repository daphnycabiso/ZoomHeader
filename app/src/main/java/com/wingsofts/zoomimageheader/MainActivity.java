package com.wingsofts.zoomimageheader;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";

  private RecyclerView mRecyclerView;
  private ViewPager mViewPager;
  private ZoomHeaderView mZoomHeader;
  private boolean isFirst = true;

    private String mUsername;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    public static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

  private RelativeLayout mBottomView;

  public static int bottomY;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

      mUsername = ANONYMOUS;
      mFirebaseDatabase =FirebaseDatabase.getInstance();
      mFirebaseAuth = FirebaseAuth.getInstance();

      mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");

    mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    mViewPager = (ViewPager) findViewById(R.id.viewpager);
    mZoomHeader = (ZoomHeaderView) findViewById(R.id.zoomHeader);
    mViewPager.setAdapter(new Adapter());
    mViewPager.setOffscreenPageLimit(4);
    CtrlLinearLayoutManager layoutManager = new CtrlLinearLayoutManager(this);


    layoutManager.setScrollEnabled(false);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(new ListAdapter());
    mRecyclerView.setAlpha(0);
    mBottomView = (RelativeLayout) findViewById(R.id.rv_bottom);



      mAuthStateListener = new FirebaseAuth.AuthStateListener(){
          @Override
          public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
              FirebaseUser user = firebaseAuth.getCurrentUser();
              if (user != null){
                  //if user is sign in
                  onSignedInInitialize(user.getDisplayName());
              }else {
                  //if user is sign out
                  onSignedOutCleanup();
                  startActivityForResult(
                          AuthUI.getInstance()
                                  .createSignInIntentBuilder()
                                  .setIsSmartLockEnabled(false)
                                  .setProviders(
                                          AuthUI.EMAIL_PROVIDER,
                                          AuthUI.GOOGLE_PROVIDER,
                                          AuthUI.FACEBOOK_PROVIDER)

                                  .build(),
                          RC_SIGN_IN);
              }
          }
      };



  }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            }else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Signed out!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirst) {
            for (int i = 0; i < mViewPager.getChildCount(); i++) {
                View v = mViewPager.getChildAt(i).findViewById(R.id.ll_bottom);
                v.setY(mViewPager.getChildAt(i).findViewById(R.id.imageView).getHeight());
                v.setX(MarginConfig.MARGIN_LEFT_RIGHT);

                mZoomHeader.setY(mZoomHeader.getY() - 1);
                isFirst = false;
            }
        }


        bottomY = (int) mBottomView.getY();
        mBottomView.setTranslationY(mBottomView.getY() + mBottomView.getHeight());
        mZoomHeader.setBottomView(mBottomView, bottomY);
    }

    class Adapter extends PagerAdapter {
        public Adapter() {
            views = new ArrayList<>();
            views.add(View.inflate(MainActivity.this, R.layout.item_img, null));
            views.get(0).findViewById(R.id.btn_buy).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Buy", Toast.LENGTH_SHORT).show();
                }
            });

            views.add(View.inflate(MainActivity.this, R.layout.item_img, null));
            views.get(0).findViewById(R.id.btn_buy).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Buy", Toast.LENGTH_SHORT).show();
                }
            });


            views.add(View.inflate(MainActivity.this, R.layout.item_img, null));
            views.get(0).findViewById(R.id.btn_buy).setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "Buy", Toast.LENGTH_SHORT).show();
                }
            });
        }



        private ArrayList<View> views;
        private int[] imgs = { R.drawable.rossa, R.drawable.glamorous, R.drawable.infinity, R.drawable.enchantme, R.drawable.exuberant};

        @Override public int getCount() {
            return views.size();
        }

        @Override public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override public Object instantiateItem(ViewGroup container, int position) {
            views.get(position).findViewById(R.id.imageView).setBackgroundResource(imgs[position]);
            container.addView(views.get(position));

            return views.get(position);
        }

        @Override public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }
    }


    @Override public void onBackPressed() {

        if (mZoomHeader.isExpand()) {
            mZoomHeader.restore(mZoomHeader.getY());
        } else {
            finish();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSignedInInitialize(String username){
        mUsername = username;
        attachDatabaseReadListener();


    }

    private void onSignedOutCleanup(){
        mUsername = ANONYMOUS;
        detachDatabaseReadListener();

    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


}

