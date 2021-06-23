package com.example.workflow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.workflow.common.SaveSharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class authentication extends AppCompatActivity {
    private TextInputEditText phoneNumberEditText, otpEditText,operatorNameEditText;
    MaterialButton sendOtp;
    MaterialButton signIn;
    String mobile;
    String mVerificationId;

    RadioGroup operationGroup;
    RadioButton handworkRadio;
    RadioButton stitchingRadio;
    RadioButton cuttingRadio;
    private FirebaseAuth mAuth;

    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference users;
    DatabaseReference activeUsers;

    String operation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        mAuth=FirebaseAuth.getInstance();
        users=database.getReference("users");
        activeUsers=database.getReference("activeUsers");

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        operatorNameEditText=findViewById(R.id.operatorNameEditText);
        otpEditText = findViewById(R.id.otpEditText);
        sendOtp = findViewById(R.id.send);

        operationGroup=findViewById(R.id.operationGroup);
        handworkRadio=findViewById(R.id.handworkRadio);
        stitchingRadio=findViewById(R.id.stitchingRadio);
        cuttingRadio=findViewById(R.id.cuttingRadio);

        sendOtp.setOnClickListener(v -> {
            sendOtp.setText("sended");
            Toast.makeText(authentication.this, "selected", Toast.LENGTH_SHORT).show();
            mobile = phoneNumberEditText.getText().toString().trim();
            sendVerificationCode(mobile);
        });

        signIn=findViewById(R.id.singIn);
        signIn.setOnClickListener(v-> {


                String otp=otpEditText.getText().toString().trim();
                verifyVerificationCode(otp);

        });
        getOpration();
    }

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    mVerificationId = s;

                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        otpEditText.setText(code);
                        verifyVerificationCode(code);
                    }
                    signInWithPhoneAuthCredential(phoneAuthCredential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(authentication.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            };


    private void verifyVerificationCode(String otp) {
        //creating the credential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(authentication.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(authentication.this, "Successfull", Toast.LENGTH_SHORT).show();
                            //verification successful we will start the profile activity

                            SaveSharedPref.setOperation(getApplicationContext(),operation);
                            SaveSharedPref.setOperatorName(getApplicationContext(),operatorNameEditText.getText().toString().trim());
                            String currentUser=mAuth.getCurrentUser().getUid();
                            users.child(currentUser).child("name").setValue(operatorNameEditText.getText().toString().trim());
                            activeUsers.child(currentUser).setValue(false);

                            Intent intent = new Intent(authentication.this, enterOrderNo.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {

                            //verification unsuccessful.. display an error message

                            String message = "Somthing is wrong, we will fix it soon...";

                            Toast.makeText(authentication.this, "Unnsucessfull", Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }

                            Snackbar snackbar = Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }

    private void getOpration() {
        operationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int caseId) {
                switch (caseId){
                    case R.id.handworkRadio:
                        operation="handwork";
                        break;
                    case R.id.stitchingRadio:
                        operation="stitching";
                        break;
                    case R.id.cuttingRadio:
                        operation="cutting";
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent = new Intent(authentication.this, codeScanner.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
//8848399652