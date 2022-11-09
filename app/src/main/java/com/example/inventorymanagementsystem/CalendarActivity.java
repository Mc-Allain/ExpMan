package com.example.inventorymanagementsystem;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.example.inventorymanagementsystem.adapters.recyclerview.ExpirationProductListAdapter;
import com.example.inventorymanagementsystem.classes.ComponentManager;
import com.example.inventorymanagementsystem.classes.Credentials;
import com.example.inventorymanagementsystem.classes.DateTime;
import com.example.inventorymanagementsystem.classes.Enums;
import com.example.inventorymanagementsystem.classes.MyDatabaseHelper;
import com.example.inventorymanagementsystem.data_model.Expiration;
import com.example.inventorymanagementsystem.data_model.Product;
import com.example.inventorymanagementsystem.dialogs.CalendarExpirationFormDialog;
import com.example.inventorymanagementsystem.dialogs.CalendarProductFormDialog;
import com.example.inventorymanagementsystem.dialogs.ConfirmationDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgAdd;
    private TextView tvCalendarDate;
    private ImageView imgCalendar;
    private TextView tvRecordMessage;
    private EditText etSearch;

    private Context context;

    private ComponentManager componentManager;

    private CalendarExpirationFormDialog calendarExpirationFormDialog;
    private ConfirmationDialog confirmationDialog;

    private String selectedDate;
    private long selectedDateTime;
    private Expiration selectedExpiration;

    private String search = "";

    private MyDatabaseHelper myDatabaseHelper;

    private final List<Product> productList = new ArrayList<>();
    private final List<Expiration> expirationList = new ArrayList<>();
    private List<Expiration> copyExpirationList;

    private ExpirationProductListAdapter expirationProductListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        imgAdd = findViewById(R.id.imgAdd);
        tvCalendarDate = findViewById(R.id.tvCalendarDate);
        imgCalendar = findViewById(R.id.imgCalendar);
        tvRecordMessage = findViewById(R.id.tvRecordMessage);
        etSearch = findViewById(R.id.etSearch);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        context = CalendarActivity.this;

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        componentManager = new ComponentManager(context);

        calendarExpirationFormDialog  = new CalendarExpirationFormDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        myDatabaseHelper = new MyDatabaseHelper(context);

        selectedDate = new DateTime().getDateText();
        selectedDateTime = new DateTime().getDateTimeValue();

        tvCalendarDate.setText(selectedDate);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        expirationProductListAdapter = new ExpirationProductListAdapter(context, productList, expirationList);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(expirationProductListAdapter);

        getAllRecords();

        expirationProductListAdapter.setAdapterListener(new ExpirationProductListAdapter.AdapterListener() {
            @Override
            public void onUpdate(Expiration expiration, Product product) {
                calendarExpirationFormDialog.setUpdateData(expiration);
                calendarExpirationFormDialog.setProduct(product);
                calendarExpirationFormDialog.showDialog();
            }

            @Override
            public void onDelete(Expiration expiration) {
                confirmationDialog.setMessage(context.getString(R.string.confirmation_prompt, "delete the expiration?"));
                confirmationDialog.showDialog();

                selectedExpiration = expiration;
            }
        });

        calendarExpirationFormDialog.setDialogListener(() -> {
            getAllRecords();
            searchRecords(Credentials.fullTrim(search));
        });

        confirmationDialog.setDialogListener(() -> {
            myDatabaseHelper.deleteProductExpiration(selectedExpiration.getId());
            getAllRecords();
            searchRecords(Credentials.fullTrim(search));
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
                search = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etSearch, !Credentials.isEmpty(search), Enums.CLEAR_TEXT);

                searchRecords(Credentials.fullTrim(search));
            }
        });

        tvCalendarDate.setOnClickListener(this);

        imgCalendar.setOnClickListener(this);

        componentManager.setDatePickerListener(new ComponentManager.DatePickerListener() {
            @Override
            public void onSelect(long dateTime, EditText targetEditText) {
            }

            @Override
            public void onSelect(String date, EditText targetEditText) {
            }

            @Override
            public void onSelect(long dateTime, TextView targetTextView) {
                selectedDateTime = dateTime;
            }

            @Override
            public void onSelect(String date, TextView targetTextView) {
                selectedDate = date;

                targetTextView.setText(date);
                targetTextView.setVisibility(View.VISIBLE);

                getAllRecords();
                searchRecords(Credentials.fullTrim(search));
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == imgAdd.getId()) {
            calendarExpirationFormDialog.setExpirationDateTime(selectedDateTime);
            calendarExpirationFormDialog.showDialog();
        } else if (view.getId() == tvCalendarDate.getId() || view.getId() == imgCalendar.getId()) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(selectedDateTime);

            componentManager.setCalendar(calendar);
            componentManager.showDatePickerDialog(tvCalendarDate);
        }
    }

    public void getAllRecords() {
        productList.clear();
        expirationList.clear();

        Cursor cursor = myDatabaseHelper.readAllProducts();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Product product =
                        new Product(
                                cursor.getInt(0),
                                cursor.getString(1),
                                getProductExpirations(cursor.getInt(0))
                        );

                productList.add(product);

                for (Expiration expiration : product.getExpirations()) {
                    String expirationDate = new DateTime(expiration.getDate()).getDateText();
                    if (expirationDate.equals(selectedDate)) {
                        expirationList.add(expiration);
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            expirationList.sort((expiration, t1) -> {
                Product product = new Product(), product1 = new Product();

                for (Product product2 : productList) {
                    if (expiration.getProductId() == product2.getId()) {
                        product = product2;
                        break;
                    }
                }

                for (Product product2 : productList) {
                    if (t1.getProductId() == product2.getId()) {
                        product1 = product2;
                        break;
                    }
                }

                return product.getName().compareToIgnoreCase(product1.getName());
            });
        }

        copyExpirationList = new ArrayList<>(expirationList);

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
        expirationList.clear();

        if (search.length() != 0) {
            for (Expiration expiration : copyExpirationList) {
                if (isProductFound(search, expiration) || isTagFound(search, expiration)) {
                    expirationList.add(expiration);
                }
            }
        } else {
            expirationList.addAll(copyExpirationList);
        }

        checkRecordCount();
    }

    public boolean isProductFound(String search, Expiration expiration) {
        for (Product product : productList) {
            if (expiration.getProductId() == product.getId()) {
                return product.getName().toLowerCase().contains(search.toLowerCase());
            }
        }

        return false;
    }

    public boolean isTagFound(String search, Expiration expiration) {
        return expiration.getTag().toLowerCase().contains(search.toLowerCase());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void checkRecordCount() {
        expirationProductListAdapter.notifyDataSetChanged();

        if (expirationList.size() == 0) {
            tvRecordMessage.setVisibility(View.VISIBLE);

            tvRecordMessage.setText(getString(R.string.no_record, "Product"));
        } else tvRecordMessage.setVisibility(View.GONE);
        tvRecordMessage.bringToFront();
    }
}