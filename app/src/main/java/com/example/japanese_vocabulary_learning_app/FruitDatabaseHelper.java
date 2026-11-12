package com.example.japanese_vocabulary_learning_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Quản lý SQLite nội bộ ứng dụng.
 *
 * onCreate() tạo bảng fruits và thêm 15 trái cây mẫu một lần duy nhất.
 * Home và Detail đọc dữ liệu từ SQLite qua getAllNames() / getAllProducts().
 */
public class FruitDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fruits.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_FRUITS = "fruits";

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_ORIGIN = "origin";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_PRICE = "price";
    private static final String COL_IMAGE_NAME = "image_name";

    private final Context appContext;

    public FruitDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE chỉ chạy khi fruits.db chưa tồn tại trên thiết bị.
        String sql = "CREATE TABLE " + TABLE_FRUITS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL UNIQUE, "
                + COL_ORIGIN + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT NOT NULL, "
                + COL_PRICE + " INTEGER NOT NULL, "
                + COL_IMAGE_NAME + " TEXT NOT NULL"
                + ")";
        db.execSQL(sql);

        // Lưu 15 loại trái cây mẫu vào SQLite trong lần tạo database đầu tiên.
        // Giữ tên drawable (fruit_apple) thay vì ID R.drawable.* vì ID có thể đổi khi build.
        for (Product product : DataSource.getProducts()) {
            ContentValues values = new ContentValues();
            values.put(COL_NAME, product.getName());
            values.put(COL_ORIGIN, product.getOrigin());
            values.put(COL_DESCRIPTION, product.getDescription());
            values.put(COL_PRICE, product.getPrice());

            String imageName = appContext.getResources()
                    .getResourceEntryName(product.getImageResId());
            values.put(COL_IMAGE_NAME, imageName);

            // insertOrThrow() báo lỗi nếu dữ liệu vi phạm ràng buộc của bảng.
            db.insertOrThrow(TABLE_FRUITS, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Hiện database ở phiên bản 1, chưa cần nâng cấp.
        // Nếu tăng DATABASE_VERSION, hãy viết migration bằng ALTER TABLE
        // hoặc câu lệnh thích hợp, không DROP TABLE làm mất dữ liệu người dùng.
    }

    // SELECT toàn bộ thông tin trái cây để ProductAdapter hiển thị ở Detail.
    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {
                COL_NAME,
                COL_ORIGIN,
                COL_DESCRIPTION,
                COL_PRICE,
                COL_IMAGE_NAME
        };

        // try-with-resources tự đóng Cursor, tránh rò rỉ tài nguyên.
        try (Cursor cursor = db.query(
                TABLE_FRUITS,
                columns,
                null,
                null,
                null,
                null,
                COL_ID + " ASC"
        )) {
            int nameCol = cursor.getColumnIndexOrThrow(COL_NAME);
            int originCol = cursor.getColumnIndexOrThrow(COL_ORIGIN);
            int descriptionCol = cursor.getColumnIndexOrThrow(COL_DESCRIPTION);
            int priceCol = cursor.getColumnIndexOrThrow(COL_PRICE);
            int imageCol = cursor.getColumnIndexOrThrow(COL_IMAGE_NAME);

            while (cursor.moveToNext()) {
                String imageName = cursor.getString(imageCol);

                // Chuyển "fruit_apple" thành ID drawable để ImageView sử dụng.
                int imageResId = appContext.getResources().getIdentifier(
                        imageName,
                        "drawable",
                        appContext.getPackageName()
                );
                if (imageResId == 0) {
                    imageResId = R.drawable.fruit_apple;
                }

                products.add(new Product(
                        cursor.getString(nameCol),
                        cursor.getString(originCol),
                        cursor.getString(descriptionCol),
                        cursor.getInt(priceCol),
                        imageResId
                ));
            }
        }

        return products;
    }

    // SELECT chỉ cột name; thứ tự id giống getAllProducts() để Home mở đúng sản phẩm.
    public String[] getAllNames() {
        ArrayList<String> names = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_FRUITS,
                new String[]{COL_NAME},
                null,
                null,
                null,
                null,
                COL_ID + " ASC"
        )) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0));
            }
        }

        return names.toArray(new String[0]);
    }
}
