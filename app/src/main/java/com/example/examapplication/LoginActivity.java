package com.example.examapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {

    private EditText loginUsername, loginPassword;
    private Button loginButton;
    private TextView signupRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        loginButton.setOnClickListener(view -> {
            if (validateUsername() && validatePassword()) {
                authenticateUser();
            }
        });

        signupRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty");
            return false;        } else {
            loginUsername.setError(null);
            return true;        }
    }

    private boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;        } else {
            loginPassword.setError(null);
            return true;        }
    }

    private void authenticateUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(userUsername, userPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Accesso riuscito
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user != null) {
                            // L'utente è autenticato
                            String nameFromDB = user.getDisplayName();
                            String emailFromDB = user.getEmail();
                            String usernameFromDB = user.getUid();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                            intent.putExtra("name", nameFromDB);
                            intent.putExtra("email", emailFromDB);
                            intent.putExtra("username", usernameFromDB);

                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Accesso non riuscito, gestisci l'errore
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
