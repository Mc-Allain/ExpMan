package com.example.inventorymanagementsystem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorymanagementsystem.adapters.recyclerview.ProductListAdapter;
import com.example.inventorymanagementsystem.classes.ComponentManager;
import com.example.inventorymanagementsystem.classes.Credentials;
import com.example.inventorymanagementsystem.classes.Enums;
import com.example.inventorymanagementsystem.classes.MyDatabaseHelper;
import com.example.inventorymanagementsystem.data_model.Expiration;
import com.example.inventorymanagementsystem.data_model.Product;
import com.example.inventorymanagementsystem.dialogs.ProductFormDialog;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etSearch;
    private ConstraintLayout categoryDropdownLayout;
    private TextView tvCategory;
    private ImageView imgCategoryDropdown;
    private ImageView imgAdd;
    private TextView tvRecordMessage;

    private Context context;

    private ComponentManager componentManager;

    private ProductFormDialog productFormDialog;

    private String searchProduct = "";

    private MyDatabaseHelper myDatabaseHelper;

    private final List<Product> productList = new ArrayList<>();
    private List<Product> copyProductList;

    private ProductListAdapter productListAdapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        etSearch = findViewById(R.id.etSearch);
        categoryDropdownLayout = findViewById(R.id.categoryDropdownLayout);
        tvCategory = findViewById(R.id.tvCategory);
        imgCategoryDropdown = findViewById(R.id.imgCategoryDropdown);
        imgAdd = findViewById(R.id.imgAdd);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        tvRecordMessage = findViewById(R.id.tvRecordMessage);

        context = ProductsActivity.this;

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        productFormDialog = new ProductFormDialog(context);

        componentManager = new ComponentManager(context);

        myDatabaseHelper = new MyDatabaseHelper(context);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        productListAdapter = new ProductListAdapter(context, productList);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(productListAdapter);

        getAllRecords();

        productListAdapter.setAdapterListener(product -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });

        productFormDialog.setDialogListener(() -> {
            getAllRecords();
            searchRecords(Credentials.fullTrim(searchProduct));
            productListAdapter.notifyDataSetChanged();
        });

        imgAdd.setOnClickListener(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchProduct = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etSearch, !Credentials.isEmpty(searchProduct), Enums.CLEAR_TEXT);

                searchRecords(Credentials.fullTrim(searchProduct));
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == imgAdd.getId()) {
            productFormDialog.showDialog();
        }
    }

    public void getAllRecords() {
        productList.clear();

        Cursor cursor = myDatabaseHelper.readAllProducts();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                productList.add(
                        new Product(
                                cursor.getInt(0),
                                cursor.getString(1),
                                getProductExpirations(cursor.getInt(0))
                        )
                );
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            productList.sort((product, t1) -> product.getName().compareToIgnoreCase(t1.getName()));
        }

        copyProductList = new ArrayList<>(productList);

        checkRecordCount();
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

    public void searchRecords(String search) {
        productList.clear();

        if (search.length() != 0) {
            for (Product product : copyProductList) {
                if (product.getName().toLowerCase().contains(search.toLowerCase())) {
                    productList.add(product);
                }
            }
        } else {
            productList.addAll(copyProductList);
        }

        checkRecordCount();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void checkRecordCount() {
        productListAdapter.notifyDataSetChanged();

        if (productList.size() == 0) {
            tvRecordMessage.setVisibility(View.VISIBLE);

            tvRecordMessage.setText(getString(R.string.no_record, "Product"));
        } else tvRecordMessage.setVisibility(View.GONE);
        tvRecordMessage.bringToFront();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getAllRecords();
        searchRecords(Credentials.fullTrim(searchProduct));
    }
}