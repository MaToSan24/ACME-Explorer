package com.example.acmeexplorer;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.acmeexplorer.entity.Trip;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private ArrayList<Trip> trips;
    private FirebaseAuth mAuth;
    private Button loginButtonGoogle;
    private Button loginButtonMail;
    private Button loginButtonRegister;
    private ProgressBar progressBar;
    private TextInputLayout loginEmail;
    private TextInputLayout loginPass;
    private AutoCompleteTextView loginEmailEt;
    private AutoCompleteTextView loginPassEt;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress);
        loginEmailEt = findViewById(R.id.login_email_et);
        loginPassEt = findViewById(R.id.login_pass_et);
        loginEmail = findViewById(R.id.login_email);
        loginPass = findViewById(R.id.login_pass);
        loginButtonGoogle = findViewById(R.id.login_button_google);
        loginButtonMail = findViewById(R.id.login_button_mail);
        loginButtonRegister = findViewById(R.id.login_button_register);

        trips = getIntent().getParcelableArrayListExtra("Trips");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() == null) {
                System.out.println("Error: result.getData() is null");
                return;
            }
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            if (!task.isSuccessful()) {
                // handle task error
                Exception exception = task.getException();
                if (exception != null) {
                    StackTraceElement[] error = exception.getStackTrace();
                    for (StackTraceElement element : error) {
                        System.out.println("Error 1: " + element.toString());
                    }
                }
                return;
            }
            try {
                GoogleSignInAccount account = task.getResult();
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        FirebaseUser user = task1.getResult().getUser();
                        checkUserDatabaseLogin(user);
                    } else {
                        StackTraceElement[] error = task1.getException().getStackTrace();
                        for (StackTraceElement element : error) {
                            System.out.println("Error 2: " + element.toString());
                        }
                    }
                });
            } catch (Exception e) {
                StackTraceElement[] error = e.getStackTrace();
                for (StackTraceElement element : error) {
                    System.out.println("Error 3: " + element.toString());
                }
            }
        });


        loginButtonGoogle.setOnClickListener(v -> attemptLoginGoogle(gso));
        loginButtonMail.setOnClickListener(v -> attemptLoginEmail());
        loginButtonRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLoginGoogle(GoogleSignInOptions gso) {
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        launcher.launch(signInIntent);
    }

    private void attemptLoginEmail() {
        String email = loginEmailEt.getText().toString();
        String password = loginPassEt.getText().toString();

        if (email.isEmpty()) {
            loginEmail.setError("Email is required");
            loginEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            loginPass.setError("Password is required");
            loginPass.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = task.getResult().getUser();
                checkUserDatabaseLogin(user);
            } else if (task.getException() != null) {
                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserDatabaseLogin(FirebaseUser user) {
        if (user != null) {
            System.out.println("User logged in: " + user.getUid() + " " + user.getEmail());
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putParcelableArrayListExtra("Trips", trips);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_LONG).show();
        }
    }
}

