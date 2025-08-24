package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, phoneNumber, emailET,SignupPassword, signup_c_Password;
    Spinner signupPatientType;
    Button sendVerificationCodeButton, verifyButton, signupButton;
    TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        signupName = findViewById(R.id.signup_name);
        phoneNumber = findViewById(R.id.phone_number);
        emailET = findViewById(R.id.signup_email);
        signupPatientType = findViewById(R.id.signup_PatientType);
        SignupPassword = findViewById(R.id.signup_password);
        signup_c_Password = findViewById(R.id.signup_c_password);
//        sendVerificationCodeButton = findViewById(R.id.send_verification_code_button);
//        verifyButton = findViewById(R.id.verify_button);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        // Set up the spinner
        String[] patientTypes = {"Doctor", "Patient"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, patientTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        signupPatientType.setAdapter(adapter);
        signupPatientType.setSelection(0);


//        // Set up button listeners
//        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Code to send verification code
//                Toast.makeText(SignupActivity.this, "Verification code sent", Toast.LENGTH_SHORT).show();
//            }
//        });

//        verifyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Code to verify the verification code
//                Toast.makeText(SignupActivity.this, "Verification code verified", Toast.LENGTH_SHORT).show();
//            }
//        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (true) {String name = signupName.getText().toString();
                    String phone = phoneNumber.getText().toString();
//        String code = verificationCode.getText().toString().trim();
                    String email = emailET.getText().toString();
                    String type = signupPatientType.getSelectedItem().toString(); // Get selected item from Spinner
                    String password = SignupPassword .getText().toString();
                    String C_password = signup_c_Password.getText().toString();
//                    registerUser();
//                    FirebaseUser user= Firebase
                    FirebaseAuth auth= FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                FirebaseUser user =auth.getCurrentUser();
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            registerUser();
                                            Toast.makeText(SignupActivity.this, "Please verify your mail", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validateInputs(String name,String phone,String type,String password,String C_password,String email) {


//        if (name.isEmpty()) {
//            signupName.setError("Name cannot be empty");
//            return false;
//        }

//        if (phone.isEmpty()) {
//            phoneNumber.setError("Phone number cannot be empty");
//            return false;
//        }if (email.isEmpty()) {
//            emailET.setError("Email cannot be empty");
//            return false;
//        }

//        if (code.isEmpty()) {
//            verificationCode.setError("Verification code cannot be empty");
//            return false;
//        }

        if (password.isEmpty()) {
            SignupPassword .setError("Username cannot be empty");
            return false;
        }

        if (C_password.isEmpty()) {
            signup_c_Password.setError("Password cannot be empty");
            return false;
        }
        if(password!=C_password){
            signup_c_Password.setError("Password not match");
        }

        return true;
    }

    private void registerUser() {
        String name = signupName.getText().toString().trim();
        String phone = phoneNumber.getText().toString().trim();
        String type = signupPatientType.getSelectedItem().toString(); // Get selected item from Spinner
        String password = SignupPassword .getText().toString().trim();
//        String password = signupPassword.getText().toString().trim();
        String email=emailET.getText().toString().replace(".","");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users");

        Helperclass user = new Helperclass(name,email, type,phone, password);
        reference.child(email).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });




    }


}
