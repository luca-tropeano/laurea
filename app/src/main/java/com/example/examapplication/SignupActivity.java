package com.example.examapplication;

import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SignupActivity extends AppCompatActivity {
    EditText signupName, signupUsername, signupEmail, signupPassword;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Controllo se l'utente è già autenticato
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // L'utente è già autenticato, avvia MainActivity e chiudi SignupActivity
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;  // Esce dal metodo se l'utente è già autenticato
        }

        // Aggiorna l'URL del database e abilita la persistenza
        database = FirebaseDatabase.getInstance("https://examapplication-a6835-default-rtdb.europe-west1.firebasedatabase.app");
        database.setPersistenceEnabled(true);

        setContentView(R.layout.activity_sign_up);

        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);

        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = signupName.getText().toString();
                String email = signupEmail.getText().toString();
                String username = signupUsername.getText().toString();
                String password = signupPassword.getText().toString();

                // Validazione dell'email senza emoticon
                if (!isValidEmail(email)) {
                    Toast.makeText(SignupActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validazione della password senza emoticon
                if (!isValidPassword(password)) {
                    Toast.makeText(SignupActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // L'utente è stato registrato con successo
                                    Log.d("FirebaseRegistration", "User registered successfully");

                                    // Ottieni l'ID dell'utente appena registrato
                                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                                    // Salva il nome, lo username e l'email nel nodo "users" nel tuo database Firebase
                                    reference = database.getReference("users");
                                    User user = new User(name, username, email);
                                    reference.child("users").child(userId).setValue(user);

                                    Toast.makeText(SignupActivity.this, "You have signed up successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // La registrazione dell'utente ha fallito
                                    Log.e("FirebaseRegistration", "Sign up failed", task.getException());
                                    Toast.makeText(SignupActivity.this, "Sign up failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean isValidEmail(String email) {
        // Utilizza un'espressione regolare per validare l'email
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidPassword(String password) {
        // Utilizza un'espressione regolare per validare la password (nessuna emoticon)
        String passwordRegex = "^[^\\p{Emoji}]+$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
