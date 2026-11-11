package com.example.japanese_vocabulary_learning_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String[] items = DataSource.getItems();
        ListView listView = findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        Button btnOpenProducts = findViewById(R.id.btnOpenProducts);
        btnOpenProducts.setOnClickListener(v -> openProducts(-1));
        listView.setOnItemClickListener((parent, view, position, id) ->
                openProducts(position));

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_products) {
                openProducts(-1);
                return true;
            }
            return item.getItemId() == R.id.nav_home;
        });
    }

    private void openProducts(int position) {
        Intent intent = new Intent(this, DetailActivity.class);
        if (position >= 0) {
            intent.putExtra("selected_position", position);
        }
        startActivity(intent);
    }
}
