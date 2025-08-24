package com.example.project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.model.deta_class;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;

public class QrgeneretorActivity extends AppCompatActivity {
    private EditText nameEditText, ageEditText, mobileEditText, addressEditText, problemEditText, bloodGroupEditText, additionalInfoEditText;
    private RadioGroup genderRadioGroup;
    private RadioButton maleRadioButton, femaleRadioButton;
    private Button uploadImageButton, generateButton, showqr;
    private ImageView imageView;
    private Uri imageUri;
    private StorageReference storageReference;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    String name, phone, gender, addres, problem, bd_group, add_info, age, email = auth.getCurrentUser().getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrgeneretor);

        // Initialize views
        nameEditText = findViewById(R.id.name_edittext);
        ageEditText = findViewById(R.id.age_edittext);
        mobileEditText = findViewById(R.id.mobile_edittext);
        addressEditText = findViewById(R.id.address_edittext);
        problemEditText = findViewById(R.id.problem_edittext);
        bloodGroupEditText = findViewById(R.id.bloodgroup_edittext);
        additionalInfoEditText = findViewById(R.id.Aditionalinformation_edittext);
        genderRadioGroup = findViewById(R.id.gender_radiogroup);
        maleRadioButton = findViewById(R.id.male_radiobutton);
        femaleRadioButton = findViewById(R.id.female_radiobutton);
        uploadImageButton = findViewById(R.id.upload_image_button);
        generateButton = findViewById(R.id.Generate_buttot);
        showqr = findViewById(R.id.show_button);
        imageView = findViewById(R.id.image_view);

        // Set listeners
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Upload image logic
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        showqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QrgeneretorActivity.this, qrdetails.class);
                startActivity(intent);
            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate QR code logic
                name = nameEditText.getText().toString();
                phone = mobileEditText.getText().toString();
                age = ageEditText.getText().toString();
                gender = ((RadioButton) findViewById(genderRadioGroup.getCheckedRadioButtonId())).getText().toString();
                addres = addressEditText.getText().toString();
                problem = problemEditText.getText().toString();
                bd_group = bloodGroupEditText.getText().toString();
                add_info = additionalInfoEditText.getText().toString();

                if (name.isEmpty() || phone.isEmpty() || gender.isEmpty() || addres.isEmpty() || problem.isEmpty() || bd_group.isEmpty() || add_info.isEmpty()) {
                    Toast.makeText(QrgeneretorActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else {
                    deta_class dc = new deta_class(name, phone, gender, addres, problem, bd_group, add_info);
                    String data = "name:" + name + "\nage:" + age + "\ngender:" + gender + "\naddress:" + addres + "\nproblem:" + problem + "\nBlood Group:" + bd_group + "\nAdditional Info:" + add_info;
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("Doctor/" + name + "/Data");
                    ref.setValue(dc).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(QrgeneretorActivity.this, "Your Data Uploaded", Toast.LENGTH_SHORT).show();
                                gen_qr_code(data, email);
                            }
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);  // Display the selected image

            // Upload image to Firebase Storage
            if (imageUri != null) {
                storageReference = FirebaseStorage.getInstance().getReference("images/your_file_name.jpg");
                storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(QrgeneretorActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QrgeneretorActivity.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

    private void gen_qr_code(String data, String email) {
        BarcodeEncoder encoder = new BarcodeEncoder();
        try {
            // If using image URL
            String imageUrl = imageUri != null ? imageUri.toString() : "";
            String qrData = data + "\nImage URL: " + imageUrl;
String uniq_key=generateUniqueKey();
            Bitmap qrBitmap = encoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference("User").child("Image"+uniq_key);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();
            UploadTask uploadTask = storageRef.putBytes(imageData);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(QrgeneretorActivity.this, "QR Code is Uploaded", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(QrgeneretorActivity.this, qrdetails.class);
                        intent.putExtra("UNIQ_KEY", uniq_key);
                        startActivity(intent);
                    }
                }
            });
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private Result decodeQRCode(Bitmap bitmap) {
        if (bitmap == null) {
            return null; // or throw an exception
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        MultiFormatReader reader = new MultiFormatReader();
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {
            Log.e("QRCodeDecoder", "Error decoding QR code", e);
            // or show an error message to the user
        }
        return result;
    }

    private void decodeAndDisplayQRCode(Bitmap qrBitmap) {
        Result qrResult = decodeQRCode(qrBitmap);

        if (qrResult != null) {
            String qrData = qrResult.getText();
            String imageUrl = extractImageUrlFromQRData(qrData);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                loadImageFromUrl(imageUrl);
            } else {
                Toast.makeText(this, "No image URL found in QR code", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to decode QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractImageUrlFromQRData(String qrData) {
        String[] dataLines = qrData.split("\n");
        for (String line : dataLines) {
            if (line.startsWith("Image URL: ")) {
                return line.replace("Image URL: ", "").trim();
            }
        }
        return null;
    }
    private static String generateUniqueKey() {
        long timestamp = System.currentTimeMillis();
        int randomValue = (int) (Math.random() * 100000); // Adjust the range as needed
        return timestamp + "_" + randomValue;
    }
    private void loadImageFromUrl(String imageUrl) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QrgeneretorActivity.this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
