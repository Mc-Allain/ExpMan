package com.example.inventorymanagementsystem.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.inventorymanagementsystem.R;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PRODUCT_NAME = "products";
    private static final String COLUMN_PRODUCT_ID = "_id";
    private static final String COLUMN_PRODUCT_NAME = "product_name";

    private static final String TABLE_EXPIRATION_NAME = "expirations";
    private static final String COLUMN_EXPIRATION_ID = "_id";
    private static final String COLUMN_EXPIRATION_PRODUCT_ID = "product_id";
    private static final String COLUMN_EXPIRATION_DATE = "expiration_date";
    private static final String COLUMN_EXPIRATION_TAG = "tag";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_PRODUCT_NAME
                + " (" + COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PRODUCT_NAME + " TEXT);";
        sqLiteDatabase.execSQL(query);

        query = "CREATE TABLE " + TABLE_EXPIRATION_NAME
                + " (" + COLUMN_EXPIRATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_EXPIRATION_PRODUCT_ID + " INTEGER, "
                + COLUMN_EXPIRATION_DATE + " BIGINT(32), "
                + COLUMN_EXPIRATION_TAG + " TEXT);";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPIRATION_NAME);
        onCreate(sqLiteDatabase);
    }

    public void addProduct(String productName) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PRODUCT_NAME, productName);

        long result = sqLiteDatabase.insert(TABLE_PRODUCT_NAME, null, contentValues);

        if (result == -1) {
            Toast.makeText(context,  context.getString(R.string.failed_message, "add", "Product"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context,  context.getString(R.string.success_message, "added", "Product"), Toast.LENGTH_LONG).show();
        }
    }

    public void updateProduct(int id, String productName) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_PRODUCT_NAME, productName);

        long result = sqLiteDatabase.update(TABLE_PRODUCT_NAME, contentValues,
                COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(id)});

        if (result == -1) {
            Toast.makeText(context,  context.getString(R.string.failed_message, "update", "Product"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context,  context.getString(R.string.success_message, "updated", "Product"), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteProduct(int id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        long result = sqLiteDatabase.delete(TABLE_PRODUCT_NAME,
                COLUMN_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(id)});

        if (result == -1) {
            Toast.makeText(context, context.getString(R.string.failed_message, "delete", "Product"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.success_message, "deleted", "Product"), Toast.LENGTH_LONG).show();
        }
    }

    public Cursor readAllProducts() {
        String query = "SELECT * FROM " + TABLE_PRODUCT_NAME;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        if (sqLiteDatabase != null) {
            return sqLiteDatabase.rawQuery(query, null);
        }

        return null;
    }

    public Cursor readProductById(int id) {
        String query = "SELECT * FROM " + TABLE_PRODUCT_NAME + " WHERE _id = " + id;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        if (sqLiteDatabase != null) {
            return sqLiteDatabase.rawQuery(query, null);
        }

        return null;
    }

    public void addProductExpiration(int productId, long dateTime, String tag) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EXPIRATION_PRODUCT_ID, productId);
        contentValues.put(COLUMN_EXPIRATION_DATE, dateTime);
        contentValues.put(COLUMN_EXPIRATION_TAG, tag);

        long result = sqLiteDatabase.insert(TABLE_EXPIRATION_NAME, null, contentValues);

        if (result == -1) {
            Toast.makeText(context, context.getString(R.string.failed_message, "add", "Expiration"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.success_message, "added", "Expiration"), Toast.LENGTH_LONG).show();
        }
    }

    public void updateProductExpiration(int id, int productId, long dateTime, String tag) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_EXPIRATION_PRODUCT_ID, productId);
        contentValues.put(COLUMN_EXPIRATION_DATE, dateTime);
        contentValues.put(COLUMN_EXPIRATION_TAG, tag);


        long result = sqLiteDatabase.update(TABLE_EXPIRATION_NAME, contentValues,
                COLUMN_EXPIRATION_ID + " = ?",
                new String[]{String.valueOf(id)});

        if (result == -1) {
            Toast.makeText(context,  context.getString(R.string.failed_message, "update", "Expiration"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context,  context.getString(R.string.success_message, "updated", "Expiration"), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteProductExpiration(int id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        long result = sqLiteDatabase.delete(TABLE_EXPIRATION_NAME,
                COLUMN_EXPIRATION_ID + " = ?",
                new String[]{String.valueOf(id)});

        if (result == -1) {
            Toast.makeText(context, context.getString(R.string.failed_message, "delete", "Expiration"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.success_message, "deleted", "Expiration"), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteProductExpirations(int productId) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        long result = sqLiteDatabase.delete(TABLE_EXPIRATION_NAME,
                COLUMN_EXPIRATION_PRODUCT_ID + " = ?",
                new String[]{String.valueOf(productId)});

        if (result == -1) {
            Toast.makeText(context, context.getString(R.string.failed_message, "delete", "Expirations"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.success_message, "deleted", "Expirations"), Toast.LENGTH_LONG).show();
        }
    }

    public Cursor readAllProductExpirations(int productId) {
        String query = "SELECT * FROM " + TABLE_EXPIRATION_NAME
                + " WHERE product_id = " + productId;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        if (sqLiteDatabase != null) {
            return sqLiteDatabase.rawQuery(query, null);
        }

        return null;
    }

    public Cursor readProductExpirationById(int productId, int id) {
        String query = "SELECT * FROM " + TABLE_EXPIRATION_NAME
                + " WHERE product_id = " + productId
                + " AND _id = " + id;
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        if (sqLiteDatabase != null) {
            return sqLiteDatabase.rawQuery(query, null);
        }

        return null;
    }
}
