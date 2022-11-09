package com.example.inventorymanagementsystem.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inventorymanagementsystem.R;
import com.example.inventorymanagementsystem.classes.ComponentManager;
import com.example.inventorymanagementsystem.classes.Credentials;
import com.example.inventorymanagementsystem.classes.DateTime;
import com.example.inventorymanagementsystem.classes.Enums;
import com.example.inventorymanagementsystem.classes.MyDatabaseHelper;
import com.example.inventorymanagementsystem.data_model.Expiration;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ExpirationFormDialog implements View.OnClickListener {

    private TextView tvDialogTitle;
    private TextView tvExpirationDate;
    private ImageView imgCalendar;
    private EditText etTag;
    private TextView tvExpirationDateError, tvTagError;
    private Button btnSubmit;

    private final Context context;
    private Dialog dialog;

    private LoadingDialog loadingDialog;
    private MessageDialog messageDialog;

    private ComponentManager componentManager;

    private int id, productId;
    private long expirationDateTime = new DateTime().getDateTimeValue();
    private String expirationDate;
    private String tag;

    public ExpirationFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_expiration_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        tvExpirationDate = dialog.findViewById(R.id.tvExpirationDate);
        tvExpirationDateError = dialog.findViewById(R.id.tvExpirationDateError);
        imgCalendar = dialog.findViewById(R.id.imgCalendar);
        etTag = dialog.findViewById(R.id.etTag);
        tvTagError = dialog.findViewById(R.id.tvTagError);
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        ImageView imgClose = dialog.findViewById(R.id.imgClose);

        List<TextView> errorTextViewList =
                Arrays.asList(tvTagError);
        List<EditText> errorEditTextList;
        errorEditTextList = Arrays.asList(etTag);

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        btnSubmit.setOnClickListener(view -> {
            componentManager.checkDate(expirationDate, true, context.getString(R.string.expiration_date), tvExpirationDateError);
            componentManager.checkInput(tag, true, context.getString(R.string.tag), tvTagError, etTag);

            if (componentManager.isNoInputError() && expirationDate != null) {
                submit();
            }
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        tvExpirationDate.setOnClickListener(this);

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
                if (targetTextView == tvExpirationDate)
                    expirationDateTime = dateTime;
            }

            @Override
            public void onSelect(String date, TextView targetTextView) {
                expirationDate = date;

                targetTextView.setText(date);
                targetTextView.setVisibility(View.VISIBLE);
                componentManager.checkDate(expirationDate, true, context.getString(R.string.expiration_date), tvExpirationDateError);
            }
        });

        etTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                tag = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etTag, !Credentials.isEmpty(tag), Enums.CLEAR_TEXT);
                componentManager.checkInput(tag, true, context.getString(R.string.tag), tvTagError, etTag);
            }
        });
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();

        if (id == 0) {
            tvDialogTitle.setText(context.getString(R.string.add_record, "Expiration"));
            btnSubmit.setText(context.getString(R.string.add));
        } else {
            tvDialogTitle.setText(context.getString(R.string.update_record, "Expiration"));
            btnSubmit.setText(context.getString(R.string.update));
        }
    }

    public void dismissDialog() {
        dialog.dismiss();

        id = 0;

        tvExpirationDate.setText(null);
        etTag.getText().clear();

        componentManager.hideInputErrors();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == tvExpirationDate.getId() || view.getId() == imgCalendar.getId()) {
            Calendar calendar = Calendar.getInstance();

            calendar.setTimeInMillis(expirationDateTime);

            componentManager.setCalendar(calendar);
            componentManager.showDatePickerDialog(tvExpirationDate);
        }
    }

    private void submit() {
        loadingDialog.showDialog();

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context);

        if (id == 0) {
            myDatabaseHelper.addProductExpiration(productId, expirationDateTime, tag);
        } else {
            myDatabaseHelper.updateProductExpiration(id, productId, expirationDateTime, tag);
        }

        loadingDialog.dismissDialog();
        dismissDialog();

        if (dialogListener != null) {
            dialogListener.onSubmit();
        }
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setUpdateData(Expiration expiration) {
        this.id = expiration.getId();
        this.productId = expiration.getProductId();
        this.expirationDateTime = expiration.getDate();
        this.tag = expiration.getTag();
        this.expirationDate = new DateTime(expirationDateTime).getDateText();

        tvExpirationDate.setText(expirationDate);
        etTag.setText(tag);
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onSubmit();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
