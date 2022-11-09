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
import com.example.inventorymanagementsystem.data_model.Product;

import java.util.Arrays;
import java.util.List;

public class CalendarProductFormDialog implements View.OnClickListener {

    private TextView tvDialogTitle, tvProductName;
    private ImageView imgSelectProduct;
    private EditText etTag;
    private TextView tvProductNameError, tvTagError;
    private Button btnSubmit;

    private final Context context;
    private Dialog dialog;

    private LoadingDialog loadingDialog;
    private MessageDialog messageDialog;

    private ComponentManager componentManager;

    private Product product;
    private long expirationDateTime = new DateTime().getDateTimeValue();
    private String tag;

    public CalendarProductFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_calendar_product_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        tvProductName = dialog.findViewById(R.id.tvProductName);
        tvProductNameError = dialog.findViewById(R.id.tvProductNameError);
        imgSelectProduct = dialog.findViewById(R.id.imgSelectProduct);
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
            componentManager.checkInput(tag, true, context.getString(R.string.tag), tvTagError, etTag);

            if (componentManager.isNoInputError() && product.getId() != 0) {
                submit();
            }
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        tvProductName.setOnClickListener(this);

        imgSelectProduct.setOnClickListener(this);

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

        DateTime dateTime = new DateTime(expirationDateTime);

        String dialogTitle = "Expiration: "
                + dateTime.getMonthShortText() + " "
                + dateTime.getDay() + ", "
                + dateTime.getYear();

        tvDialogTitle.setText(dialogTitle);
        btnSubmit.setText(context.getString(R.string.add));
    }

    public void dismissDialog() {
        dialog.dismiss();

        tvProductName.setText(null);
        etTag.getText().clear();

        componentManager.hideInputErrors();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == tvProductName.getId() || view.getId() == imgSelectProduct.getId()) {

        }
    }

    private void submit() {
        loadingDialog.showDialog();

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context);

        myDatabaseHelper.addProductExpiration(product.getId(), expirationDateTime, tag);

        loadingDialog.dismissDialog();
        dismissDialog();

        if (dialogListener != null) {
            dialogListener.onSubmit();
        }
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setExpirationDateTime(long expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onSubmit();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
