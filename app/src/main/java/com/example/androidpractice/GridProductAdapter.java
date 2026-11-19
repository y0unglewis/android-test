package com.example.androidpractice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

// Adapter riêng cho GridView ở trang Sản phẩm; không ảnh hưởng card ListView của Home.
public class GridProductAdapter extends ArrayAdapter<Product> {
    public GridProductAdapter(Context context, List<Product> products) {
        super(context, 0, products);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_product_grid, parent, false);
        }

        Product product = getItem(position);
        if (product != null) {
            ImageView image = itemView.findViewById(R.id.imgGridProduct);
            TextView name = itemView.findViewById(R.id.tvGridProductName);
            TextView origin = itemView.findViewById(R.id.tvGridProductOrigin);
            TextView price = itemView.findViewById(R.id.tvGridProductPrice);

            image.setImageResource(product.getImageResId());
            image.setContentDescription(product.getName());
            name.setText(product.getName());
            origin.setText("Xuất xứ: " + product.getOrigin());
            price.setText(String.format(Locale.forLanguageTag("vi-VN"),
                    "%,d đ/kg", product.getPrice()));
        }
        return itemView;
    }
}
