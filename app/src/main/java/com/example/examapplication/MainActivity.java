package com.example.examapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth firebaseAuth;
    private TextView userEmailTextView;
    private TextView userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance(); // Inizializza l'istanza di FirebaseAuth

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Inizializza il riferimento ai TextView nell'header
        View headerView = navigationView.getHeaderView(0);
        userEmailTextView = headerView.findViewById(R.id.nav_header_email);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Ottieni l'utente corrente e imposta il nome e l'email
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();

            // Imposta i valori nei TextView
            userEmailTextView.setText(userEmail != null ? userEmail : "Email not available");

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (itemId == R.id.nav_pomodoro) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PomodoroFragment()).commit();
        } else if (itemId == R.id.nav_blurting) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FlashcardsFragment()).commit();
        } else if (itemId == R.id.nav_three_two_one) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Sq3rWizardFragment()).commit();
        }else if (itemId == R.id.nav_statistics) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new StatisticsFragment()).commit();
        }else if (itemId == R.id.nav_research) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ResearchFragment()).commit();
        } else if (itemId == R.id.nav_logout) {
            // Esegui il logout
            firebaseAuth.signOut();

            // Avvia l'Activity di accesso (LoginActivity)
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            // Chiudi l'Activity corrente
            finish();
        } else if (itemId == R.id.nav_buy_pro) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HelpFragment()).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
