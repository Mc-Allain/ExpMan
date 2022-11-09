package com.example.inventorymanagementsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.inventorymanagementsystem.classes.Credentials;
import com.example.inventorymanagementsystem.classes.DateTime;
import com.example.inventorymanagementsystem.classes.MyDatabaseHelper;
import com.example.inventorymanagementsystem.classes.Units;
import com.example.inventorymanagementsystem.data_model.Expiration;
import com.example.inventorymanagementsystem.data_model.Product;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ConstraintLayout layoutCalendar, layoutProducts, layoutExpirations;
    private TextView tvExpiringCount, tvProductCount, tvExpirationCount;

    private Context context;

    private MyDatabaseHelper myDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutCalendar = findViewById(R.id.layoutCalendar);
        layoutProducts = findViewById(R.id.layoutProducts);
        layoutExpirations = findViewById(R.id.layoutExpirations);
        TextView tvCalendarDate = findViewById(R.id.tvCalendarDate);
        tvExpiringCount = findViewById(R.id.tvExpiringCount);
        tvProductCount = findViewById(R.id.tvProductCount);
        tvExpirationCount = findViewById(R.id.tvExpirationCount);

        context = MainActivity.this;

        myDatabaseHelper = new MyDatabaseHelper(context);

        String currentDate = new DateTime().getDateText();

        tvCalendarDate.setText(currentDate);

        getExpiringCount();
        getProductCount();

        layoutCalendar.setOnClickListener(this);

        layoutProducts.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        if (view.getId() == layoutCalendar.getId()) {
            intent = new Intent(context, CalendarActivity.class);
        } else if (view.getId() == layoutProducts.getId()) {
            intent = new Intent(context, ProductsActivity.class);
        }
        startActivity(intent);
    }

    private void getExpiringCount() {
        int count = 0;

        Cursor cursor = myDatabaseHelper.readAllProducts();

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Product product = new Product(
                        cursor.getInt(0),
                        cursor.getString(1),
                        getProductExpirations(cursor.getInt(0))
                );

                for (Expiration expiration : product.getExpirations()) {
                    int daysDiff = (int) Units.msToDay(expiration.getDate() - new DateTime().getDateTimeValue());
                    if (daysDiff >= 0 && daysDiff <= 7) {
                        count++;
                    }
                }
            }
        }

        String expiringCount = count + "×";
        tvExpiringCount.setText(expiringCount);
    }

    public List<Expiration> getProductExpirations(int productId) {
        List<Expiration> expirationList = new ArrayList<>();

        Cursor cursor = myDatabaseHelper.readAllProductExpirations(productId);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                expirationList.add(
                        new Expiration(
                                cursor.getInt(0),
                                cursor.getInt(1),
                                cursor.getLong(2),
                                cursor.getString(3)
                        )
                );
            }
        }

        return expirationList;
    }

    private void getProductCount() {
        Cursor cursor = myDatabaseHelper.readAllProducts();

        String productCount = cursor.getCount() + "×";
        tvProductCount.setText(productCount);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getExpiringCount();
        getProductCount();
    }
}