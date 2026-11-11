package com.example.japanese_vocabulary_learning_app;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageButton btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());

        ArrayList<Product> products = DataSource.getProducts();
        TextView tvProductCount = findViewById(R.id.tvProductCount);
        tvProductCount.setText(products.size() + " loại trái cây");

        ListView listView = findViewById(R.id.listViewProducts);
        ProductAdapter adapter = new ProductAdapter(this, products);
        listView.setAdapter(adapter);

        int selected = getIntent().getIntExtra("selected_position", -1);
        if (selected >= 0 && selected < products.size()) {
            listView.post(() -> listView.setSelection(selected));
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_products);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                finish();
                return true;
            }
            return item.getItemId() == R.id.nav_products;
        });
    }
}
