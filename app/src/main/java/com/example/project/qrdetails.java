package com.example.project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class qrdetails extends AppCompatActivity {

    ImageView im;
    StorageReference storageRef;
    File localfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrdetails);

        im = findViewById(R.id.genratedQr);

        String uniqKey = getIntent().getStringExtra("UNIQ_KEY");
        if (uniqKey != null) {
            storageRef = FirebaseStorage.getInstance().getReference("User").child("Image" + uniqKey);

            final long ONE_MEGABYTE = 1024 * 1024;
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    im.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Firebase", "Error downloading image: " + exception.getMessage());
                    Toast.makeText(qrdetails.this, "QR code not found!", Toast.LENGTH_SHORT).show();
                    im.setImageResource(R.drawable.scan); // Show default placeholder
                }
            });

            // Handle file download for local storage
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "QrImage");
            if (!root.exists()) {
                root.mkdirs();
            }
            localfile = new File(root, "img.jpg");
            if (localfile != null) {
                storageRef.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(qrdetails.this, "Download successful", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "File created: " + localfile.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "File not created: " + e.toString());
                    }
                });
            }

            // Get download URL
            storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        Glide.with(getApplicationContext()).load(uri).into(im);
                    } else {
                        Log.e("Firebase", "Download URL not available.");
                        Toast.makeText(qrdetails.this, "Failed to load QR code.", Toast.LENGTH_SHORT).show();
                        im.setImageResource(R.drawable.scan); // Show placeholder if URL fails
                    }
                }
            });
        } else {
            Toast.makeText(this, "Invalid QR Key!", Toast.LENGTH_SHORT).show();
            im.setImageResource(R.drawable.scan);
        }
    }
}
