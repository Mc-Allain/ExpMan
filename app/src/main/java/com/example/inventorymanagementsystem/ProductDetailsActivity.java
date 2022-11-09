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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorymanagementsystem.adapters.recyclerview.ExpirationListAdapter;
import com.example.inventorymanagementsystem.classes.ComponentManager;
import com.example.inventorymanagementsystem.classes.Credentials;
import com.example.inventorymanagementsystem.classes.Enums;
import com.example.inventorymanagementsystem.classes.MyDatabaseHelper;
import com.example.inventorymanagementsystem.data_model.Expiration;
import com.example.inventorymanagementsystem.data_model.Product;
import com.example.inventorymanagementsystem.dialogs.ConfirmationDialog;
import com.example.inventorymanagementsystem.dialogs.ExpirationFormDialog;
import com.example.inventorymanagementsystem.dialogs.ProductFormDialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvProductName, tvExpirationCount, tvRecordMessage;
    private ImageView imgAdd, imgUpdate, imgDelete;
    private EditText etSearch;

    private Context context;

    private ComponentManager componentManager;

    private ExpirationFormDialog expirationFormDialog;
    private ProductFormDialog productFormDialog;
    private ConfirmationDialog confirmationDialog;

    private Product product;
    private Expiration selectedExpiration;

    private String searchTag = "";
    private int confirmationActionMode = 0;

    private MyDatabaseHelper myDatabaseHelper;

    private final List<Expiration> expirationList = new ArrayList<>();
    private List<Expiration> copyExpirationList;

    private ExpirationListAdapter expirationListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        tvProductName = findViewById(R.id.tvProductName);
        tvExpirationCount = findViewById(R.id.tvExpirationCount);
        tvRecordMessage = findViewById(R.id.tvRecordMessage);
        imgAdd = findViewById(R.id.imgAdd);
        imgUpdate = findViewById(R.id.imgUpdate);
        imgDelete = findViewById(R.id.imgDelete);
        etSearch = findViewById(R.id.etSearch);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        context = ProductDetailsActivity.this;

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        expirationFormDialog  = new ExpirationFormDialog(context);
        productFormDialog  = new ProductFormDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        componentManager = new ComponentManager(context);

        myDatabaseHelper = new MyDatabaseHelper(context);

        Intent currentIntent = getIntent();

        int productId = currentIntent.getIntExtra("productId", 0);

        expirationFormDialog.setProductId(productId);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        expirationListAdapter = new ExpirationListAdapter(context, expirationList);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(expirationListAdapter);

        getRecordById(productId);

        expirationListAdapter.setAdapterListener(new ExpirationListAdapter.AdapterListener() {
            @Override
            public void onUpdate(Expiration expiration) {
                expirationFormDialog.setUpdateData(expiration);
                expirationFormDialog.showDialog();
            }

            @Override
            public void onDelete(Expiration expiration) {
                confirmationDialog.setMessage(context.getString(R.string.confirmation_prompt, "delete the expiration?"));
                confirmationDialog.showDialog();

                selectedExpiration = expiration;
                confirmationActionMode = 2;
            }
        });

        expirationFormDialog.setDialogListener(() -> {
            getRecordById(productId);
            searchRecords(Credentials.fullTrim(searchTag));
        });

        productFormDialog.setDialogListener(() -> {
            getRecordById(productId);
            searchRecords(Credentials.fullTrim(searchTag));
        });

        confirmationDialog.setDialogListener(() -> {
            switch (confirmationActionMode) {
                case 1:
                    myDatabaseHelper.deleteProductExpiration(selectedExpiration.getId());
                    getRecordById(productId);
                    searchRecords(Credentials.fullTrim(searchTag));

                    break;
                case 2:
                    myDatabaseHelper.deleteProduct(productId);
                    myDatabaseHelper.deleteProductExpirations(productId);

                    finish();
                    break;
                default:
                    break;
            }
        });

        imgAdd.setOnClickListener(this);

        imgUpdate.setOnClickListener(this);

        imgDelete.setOnClickListener(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchTag = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etSearch, !Credentials.isEmpty(searchTag), Enums.CLEAR_TEXT);

                searchRecords(Credentials.fullTrim(searchTag));
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == imgAdd.getId()) {
            expirationFormDialog.showDialog();
        } else if (view.getId() == imgUpdate.getId()) {
            productFormDialog.setUpdateData(product);
            productFormDialog.showDialog();
        } else if (view.getId() == imgDelete.getId()) {
            confirmationDialog.setMessage(context.getString(R.string.confirmation_prompt, "delete the product?"));
            confirmationDialog.showDialog();

            confirmationActionMode = 2;
        }
    }

    public void getRecordById(int productId) {
        Cursor cursor = myDatabaseHelper.readProductById(productId);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();

            product = new Product(
                    cursor.getInt(0),
                    cursor.getString(1),
                    getProductExpirations(productId)
            );
        }

        expirationList.clear();
        expirationList.addAll(product.getExpirations());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            expirationList.sort(Comparator.comparingLong(Expiration::getDate));
        }

        copyExpirationList = new ArrayList<>(expirationList);

        tvProductName.setText(product.getName());
        String expirationCount = expirationList.size() + "×";
        tvExpirationCount.setText(expirationCount);

        checkExpirationCount();
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
        expirationList.clear();

        if (search.length() != 0) {
            for (Expiration expiration : copyExpirationList) {
                if (expiration.getTag().toLowerCase().contains(search.toLowerCase())) {
                    expirationList.add(expiration);
                }
            }
        } else {
            expirationList.addAll(copyExpirationList);
        }

        checkExpirationCount();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void checkExpirationCount() {
        expirationListAdapter.notifyDataSetChanged();

        if (expirationList.size() == 0) {
            tvRecordMessage.setVisibility(View.VISIBLE);

            tvRecordMessage.setText(getString(R.string.no_record, "Expiration"));
        } else tvRecordMessage.setVisibility(View.GONE);
        tvRecordMessage.bringToFront();
    }
}