package com.example.inventorymanagementsystem.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorymanagementsystem.R;
import com.example.inventorymanagementsystem.adapters.recyclerview.ProductListAdapter;
import com.example.inventorymanagementsystem.classes.ComponentManager;
import com.example.inventorymanagementsystem.classes.Credentials;
import com.example.inventorymanagementsystem.classes.Enums;
import com.example.inventorymanagementsystem.classes.MyDatabaseHelper;
import com.example.inventorymanagementsystem.data_model.Expiration;
import com.example.inventorymanagementsystem.data_model.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsDialog {

    private TextView tvDialogTitle, tvRecordMessage;
    private EditText etSearch;

    private final Context context;
    private Dialog dialog;

    private ComponentManager componentManager;

    private String searchProduct = "";

    private MyDatabaseHelper myDatabaseHelper;

    private final List<Product> productList = new ArrayList<>();
    private List<Product> copyProductList;

    private ProductListAdapter productListAdapter;

    public ProductsDialog(Context context) {
        this.context = context;

        createDialog();
    }

    private void createDialog() {
        setDialog();
        setDialogWindow();
    }

    private void setDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_products_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        etSearch = dialog.findViewById(R.id.etSearch);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        tvRecordMessage = dialog.findViewById(R.id.tvRecordMessage);
        ImageView imgClose = dialog.findViewById(R.id.imgClose);

        componentManager = new ComponentManager(context);

        myDatabaseHelper = new MyDatabaseHelper(context);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        productListAdapter = new ProductListAdapter(context, productList);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(productListAdapter);

        getAllRecords();

        productListAdapter.setAdapterListener(product -> {
            if (dialogListener != null) {
                dialogListener.onSubmit(product);
            }
        });

        imgClose.setOnClickListener(view -> dismissDialog());

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

    private void setDialogWindow() {
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();

        etSearch.getText().clear();
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

            tvRecordMessage.setText(context.getString(R.string.no_record, "Product"));
        } else tvRecordMessage.setVisibility(View.GONE);
        tvRecordMessage.bringToFront();
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onSubmit(Product product);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
