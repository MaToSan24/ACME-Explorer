package com.example.acmeexplorer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    public static final String EMAIL_PARAM = "email_parameter";

    private AutoCompleteTextView login_email_et;
    private AutoCompleteTextView login_pass_et;
    private AutoCompleteTextView login_pass_confirm_et;

    private TextInputLayout login_email;
    private TextInputLayout login_pass;
    private TextInputLayout login_pass_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        String emailParam = getIntent().getStringExtra(EMAIL_PARAM);

        login_email_et = findViewById(R.id.login_email_et);
        login_pass_et = findViewById(R.id.login_pass_et);
        login_pass_confirm_et = findViewById(R.id.login_pass_confirm_et);

        login_email = findViewById(R.id.login_email);
        login_pass = findViewById(R.id.login_pass);
        login_pass_confirm = findViewById(R.id.login_pass_confirm);

        if (emailParam != null) {
            login_email_et.setText(emailParam);
        }

        findViewById(R.id.signup_button_google).setOnClickListener(v -> {
            if (login_email_et.getText().toString().isEmpty()) {
                login_email.setError("El email es obligatorio");
            } else if (!isValidEmail(login_email_et.getText().toString())) {
                login_email.setError("El email no es válido");
            } else if (login_pass_et.getText().toString().isEmpty()) {
                login_pass.setError("La contraseña es obligatoria");
            } else if (login_pass_confirm_et.getText().toString().isEmpty()) {
                login_pass_confirm.setError("La confirmación de la contraseña es obligatoria");
            } else if (login_pass_et.getText().toString().length() < 6) {
                login_pass.setError("La contraseña debe tener al menos 6 caracteres");
            } else if (!login_pass_et.getText().toString().equals(login_pass_confirm_et.getText().toString())) {
                login_pass_confirm.setError("Las contraseñas no coinciden");
            } else {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(login_email_et.getText().toString(), login_pass_et.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Usuario creado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al crear el usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}