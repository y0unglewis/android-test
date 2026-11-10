package com.example.japanese_vocabulary_learning_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ArrayList<Product> products = DataSource.getProducts();
        ListView listView = findViewById(R.id.listViewProducts);
        ProductAdapter adapter = new ProductAdapter(this, products);
        listView.setAdapter(adapter);

        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());
    }
}
