package com.example.japanese_vocabulary_learning_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends ArrayAdapter<Product> {
    public ProductAdapter(Context context, List<Product> products) {
        super(context, 0, products);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_product, parent, false);
        }

        Product product = getItem(position);
        ImageView imgProduct = itemView.findViewById(R.id.imgProduct);
        TextView tvName = itemView.findViewById(R.id.tvProductName);
        TextView tvOrigin = itemView.findViewById(R.id.tvProductOrigin);
        TextView tvDescription = itemView.findViewById(R.id.tvProductDescription);
        TextView tvPrice = itemView.findViewById(R.id.tvProductPrice);

        if (product != null) {
            imgProduct.setImageResource(product.getImageResId());
            imgProduct.setContentDescription(product.getName());
            tvName.setText(product.getName());
            tvOrigin.setText("Xuất xứ: " + product.getOrigin());
            tvDescription.setText(product.getDescription());
            String price = String.format(Locale.forLanguageTag("vi-VN"),
                    "%,d đ/kg", product.getPrice());
            tvPrice.setText(price);
        }
        return itemView;
    }
}
