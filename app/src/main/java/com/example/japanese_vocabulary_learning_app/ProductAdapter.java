
package com.example.japanese_vocabulary_learning_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends ArrayAdapter<Product> {

    public ProductAdapter(Context context,
                          List<Product> products) {
        super(context, 0, products);
    }

    @Override
    public View getView(int position,
                        View convertView,
                        ViewGroup parent) {

        // Tái sử dụng View nếu đã có
        View itemView = convertView;

        if (itemView == null) {
            itemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_product, parent, false);
        }

        // Lấy sản phẩm tại vị trí hiện tại
        Product product = getItem(position);

        // Tìm các TextView trong item_product.xml
        TextView tvName =
                itemView.findViewById(R.id.tvProductName);

        TextView tvOrigin =
                itemView.findViewById(R.id.tvProductOrigin);

        TextView tvDescription =
                itemView.findViewById(R.id.tvProductDescription);

        TextView tvPrice =
                itemView.findViewById(R.id.tvProductPrice);

        // Đổ dữ liệu vào từng TextView
        if (product != null) {

            tvName.setText(product.getName());

            tvOrigin.setText(
                    "Xuất xứ: " + product.getOrigin()
            );

            tvDescription.setText(
                    product.getDescription()
            );

            String price = String.format(
                    Locale.forLanguageTag("vi-VN"),
                    "%,d đ/kg",
                    product.getPrice()
            );

            tvPrice.setText(price);
        }

        return itemView;
    }
}
