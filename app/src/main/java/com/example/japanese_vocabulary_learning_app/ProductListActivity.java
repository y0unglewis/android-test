
package com.example.japanese_vocabulary_learning_app;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ProductListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_product_list);

        // Lấy dữ liệu từ DataSource
        ArrayList<Product> products =
                DataSource.getProducts();

        // Tìm ListView trong XML
        ListView listView =
                findViewById(R.id.listViewProducts);

        // Tạo Custom Adapter
        ProductAdapter adapter =
                new ProductAdapter(this, products);

        // Đổ dữ liệu lên UI
        listView.setAdapter(adapter);
    }
}
