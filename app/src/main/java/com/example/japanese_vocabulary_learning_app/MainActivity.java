package com.example.japanese_vocabulary_learning_app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] items = DataSource.getItems();

        ListView listView = findViewById(R.id.listView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );

        listView.setAdapter(adapter);

        Button btnOpenProducts =
                findViewById(R.id.btnOpenProducts);

        btnOpenProducts.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    ProductListActivity.class
            );

            startActivity(intent);
        });

    }
}
