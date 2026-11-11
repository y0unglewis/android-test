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

// Adapter chuyển từng Product thành một card có ImageView và TextView.
public class ProductAdapter extends ArrayAdapter<Product> {
    public ProductAdapter(Context context, List<Product> products) {
        // Gửi Context và danh sách Product lên ArrayAdapter cha.
        super(context, 0, products);
    }

    @Override
    // ListView gọi getView() khi cần dòng ở vị trí position.
    public View getView(int position, View convertView, ViewGroup parent) {
        // Tái sử dụng convertView nếu có để tránh tạo View mới khi cuộn.
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_product, parent, false);
        }

        // Lấy đúng đối tượng Product thuộc vị trí cần hiển thị.
        Product product = getItem(position);
        // Tìm các View nằm trong bố cục riêng của một sản phẩm.
        ImageView imgProduct = itemView.findViewById(R.id.imgProduct);
        TextView tvName = itemView.findViewById(R.id.tvProductName);
        TextView tvOrigin = itemView.findViewById(R.id.tvProductOrigin);
        TextView tvDescription = itemView.findViewById(R.id.tvProductDescription);
        TextView tvPrice = itemView.findViewById(R.id.tvProductPrice);

        // Luôn cập nhật dữ liệu, kể cả khi dùng lại một itemView cũ.
        if (product != null) {
            // Lấy ID drawable từ Product rồi đặt ảnh vào ImageView.
            imgProduct.setImageResource(product.getImageResId());
            imgProduct.setContentDescription(product.getName());
            tvName.setText(product.getName());
            tvOrigin.setText("Xuất xứ: " + product.getOrigin());
            tvDescription.setText(product.getDescription());
            // Format giá theo định dạng Việt Nam, ví dụ 50.000 đ/kg.
            String price = String.format(Locale.forLanguageTag("vi-VN"),
                    "%,d đ/kg", product.getPrice());
            tvPrice.setText(price);
        }
        // Trả View hoàn chỉnh cho ListView hiển thị.
        return itemView;
    }
}
