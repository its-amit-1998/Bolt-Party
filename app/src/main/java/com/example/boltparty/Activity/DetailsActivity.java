package com.example.boltparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView img1, img2, img3, img4, img5;
    private TextView txtAllPhotos, txtTitle, txtDistance, txtAddress, txtRestaurantNumber, txtOpeningDateAndTime, txtRatingBar;
    private RatingBar ratingBar;
    private Button btnBooking, btnDone;
    private NumberPicker pickerTime, pickerWeek, pickerMonth, pickerDate;
    private LinearLayout layoutConfirmed;
    private BottomSheetBehavior bottomSheetBehavior;
    private String cafeName, ratingText, distance, address, number, bookingTime, bookingDate, bookingWeek, bookingMonth, date;
    private float rating;
    private boolean firstTime = true;
    private boolean favorite = true;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private Booked booked;
    private Favorite fav;
    private Menu menu;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        invalidateOptionsMenu();
        getValues();
        setActionBar();
        initComponent();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        databaseReference.child("Booked").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    booked = snapshot.getValue(Booked.class);
                    if (booked.getCafeName().equals(cafeName)) {
                        layoutConfirmed.setVisibility(View.VISIBLE);
                        firstTime = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child("Favorite").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    fav = snapshot.getValue(Favorite.class);
                    if (fav.getCafeName().equals(cafeName)) {
                        favorite = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        txtAllPhotos.setOnClickListener(this);
        btnBooking.setOnClickListener(this);
    }

    private void getValues() {
        Bundle extras = getIntent().getExtras();
        cafeName = extras.getString("CafeName");
        rating = extras.getFloat("Rating");
        ratingText = extras.getString("RatingText");
        distance = extras.getString("Distance");
        address = extras.getString("Address", "abc");
        number = extras.getString("Number");
    }

    private void setActionBar() {
        getSupportActionBar().setTitle(cafeName);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initComponent() {

        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);
        img5 = findViewById(R.id.img5);
        txtAllPhotos = findViewById(R.id.txtAllPhotos);
        txtTitle = findViewById(R.id.txtTitle);
        txtDistance = findViewById(R.id.txtDistance);
        txtAddress = findViewById(R.id.txtAddress);
        txtRestaurantNumber = findViewById(R.id.txtRestaurantNumber);
        txtOpeningDateAndTime = findViewById(R.id.txtOpeningDateAndTime);
        txtRatingBar = findViewById(R.id.txtRatingBar);
        ratingBar = findViewById(R.id.ratingBar);
        btnBooking = findViewById(R.id.btnBooking);
        layoutConfirmed = findViewById(R.id.layoutConfirmed);

        txtTitle.setText(cafeName);
        ratingBar.setRating(rating);
        txtRatingBar.setText(ratingText);
        txtDistance.setText(distance + " KM away");
        txtAddress.setText(address);
        txtRestaurantNumber.setText(number);

        setImage();

        // get the bottom sheet view
        View bottomSheet = findViewById(R.id.bottom_sheet_picker);

        //init the View item
        pickerTime = bottomSheet.findViewById(R.id.pickerTime);
        pickerDate = bottomSheet.findViewById(R.id.pickerDate);
        pickerWeek = bottomSheet.findViewById(R.id.pickerWeek);
        pickerMonth = bottomSheet.findViewById(R.id.pickerMonth);
        btnDone = bottomSheet.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(this);

        String[] time = getResources().getStringArray(R.array.time);
        String[] week = getResources().getStringArray(R.array.week);
        String[] month = getResources().getStringArray(R.array.month);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // change the state of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheet.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        bottomSheet.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        pickerTime.setMaxValue(time.length - 1);
        pickerTime.setMinValue(0);
        pickerTime.setDisplayedValues(time);

        pickerDate.setMaxValue(32);
        pickerDate.setMinValue(1);

        pickerWeek.setMaxValue(week.length - 1);
        pickerWeek.setMinValue(0);
        pickerWeek.setDisplayedValues(week);

        pickerMonth.setMaxValue(month.length - 1);
        pickerMonth.setMinValue(0);
        pickerMonth.setDisplayedValues(month);

        bookingTime = time[pickerTime.getValue()];
        bookingWeek = week[pickerWeek.getValue()] + ", ";
        bookingMonth = month[pickerMonth.getValue()] + "-";
        bookingDate = String.valueOf(pickerDate.getValue());
        pickerTime.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                bookingTime = time[newValue];
            }
        });

        pickerWeek.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                bookingWeek = week[newValue] + ", ";
            }
        });

        pickerMonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                bookingMonth = month[newValue] + "-";
            }
        });

        pickerDate.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                bookingDate = String.valueOf(newValue);
            }
        });
    }

    private void setImage() {
        switch (cafeName) {
            case "The Owl Cafe":
                img1.setImageResource(R.drawable.owlcafe);
                img2.setImageResource(R.drawable.owl2);
                img3.setImageResource(R.drawable.owl3);
                img4.setImageResource(R.drawable.owl4);
                img5.setImageResource(R.drawable.owl5);
                break;
            case "Diggin Cafe":
                img1.setImageResource(R.drawable.diggincafe);
                img2.setImageResource(R.drawable.diggin2);
                img3.setImageResource(R.drawable.diggin3);
                img4.setImageResource(R.drawable.diggin4);
                img5.setImageResource(R.drawable.diggin5);
                break;
            default:
                img1.setImageResource(R.mipmap.ic_launcher);
                img2.setImageResource(R.mipmap.ic_launcher);
                img3.setImageResource(R.mipmap.ic_launcher);
                img4.setImageResource(R.mipmap.ic_launcher);
                img5.setImageResource(R.mipmap.ic_launcher);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        int statusInfo = NetworkInfo.getNetworkStatus(this);
        if (statusInfo == NetworkInfo.NOT_CONNECTED) {
            startActivity(new Intent(this, NoNetworkActivity.class));
        }

    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtAllPhotos:
                Intent intent = new Intent(DetailsActivity.this, ImagesActivity.class);
                intent.putExtra("CafeName", cafeName);
                startActivity(intent);
                break;
            case R.id.btnBooking:
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                break;
            case R.id.btnDone:
                date = bookingWeek + bookingMonth + bookingDate;
                if (firstTime) {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("please wait...");
                    progressDialog.show();

                    String imageView = getCafeImageFromStorage(cafeName);
                    booked = new Booked(cafeName, address, date, bookingTime, imageView);
                    databaseReference.child("Booked").child(cafeName).setValue(booked).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendEmail();
                            } else {
                                progressDialog.dismiss();
                                Log.d("Details", "Data has not been saved");
                            }
                        }
                    });
                } else {
                    Toast.makeText(this, "You have already booked the Cafe.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private String getCafeImageFromStorage(String cafeName) {
        if (cafeName.equals("The Owl Cafe")) {
            return "https://firebasestorage.googleapis.com/v0/b/bolt-party-e315f.appspot.com/o/booked%2FThe%20Owl%20Cafe.jpg?alt=media&token=3a6a4c97-4b18-44d4-a6a0-ac7c033d2b11";
        }
        return "https://firebasestorage.googleapis.com/v0/b/bolt-party-e315f.appspot.com/o/booked%2FDiggin%20Cafe.jpg?alt=media&token=433918f6-c7a4-4179-8dc5-f7bdb07a69ee";

    }

//    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
//                                                         int reqWidth, int reqHeight) {
//
//        // First decode with inJustDecodeBounds=true to check dimensions
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(res, resId, options);
//
//        // Calculate inSampleSize
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//
//        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeResource(res, resId, options);
//    }
//
//    public static int calculateInSampleSize(
//            BitmapFactory.Options options, int reqWidth, int reqHeight) {
//        // Raw height and width of image
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//
//            final int halfHeight = height / 2;
//            final int halfWidth = width / 2;
//
//            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//            // height and width larger than the requested height and width.
//            while ((halfHeight / inSampleSize) >= reqHeight
//                    && (halfWidth / inSampleSize) >= reqWidth) {
//                inSampleSize *= 2;
//            }
//        }
//
//        return inSampleSize;
//    }

    private void sendEmail() {
        String body = new StringBuilder().append("You have booked the Cafe from BoltParty").append("\n\n").append("Name: ").append(cafeName).append("\n").append("Date: ").append(date).append("\n").append("Time: ").append(bookingTime).toString();
        progressDialog.dismiss();
        SendMail sendMail = new SendMail(this, "amitsingh4466019@gmail.com", "Bolt Party", body);
        sendMail.execute();
        firstTime = false;
        layoutConfirmed.setVisibility(View.VISIBLE);
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        this.menu = menu;

        if (favorite)
            menu.getItem(0).setIcon(R.drawable.ic_favorite);
        else
            menu.getItem(0).setIcon(R.drawable.ic_favorite_selected);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            super.onBackPressed();
        switch (item.getItemId()) {
            case R.id.logout:
                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                });
                break;
            case R.id.favorite:
                if (favorite) {
                    String imageView = getCafeImageFromStorage(cafeName);
                    fav = new Favorite(cafeName, address, imageView);
                    databaseReference.child("Favorite").child(cafeName).setValue(fav).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                menu.getItem(0).setIcon(R.drawable.ic_favorite_selected);
                                favorite = false;
                            } else {
                                progressDialog.dismiss();
                                Log.d("Details", "Data has not been saved");
                            }
                        }
                    });
                } else {
                    databaseReference.child("Favorite").child(cafeName).setValue(null);
                    menu.getItem(0).setIcon(R.drawable.ic_favorite);
                    favorite = true;
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}

