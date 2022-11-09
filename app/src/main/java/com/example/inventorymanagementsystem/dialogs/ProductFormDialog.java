package com.example.inventorymanagementsystem.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inventorymanagementsystem.R;
import com.example.inventorymanagementsystem.classes.ComponentManager;
import com.example.inventorymanagementsystem.classes.Credentials;
import com.example.inventorymanagementsystem.classes.Enums;
import com.example.inventorymanagementsystem.classes.MyDatabaseHelper;
import com.example.inventorymanagementsystem.data_model.Product;

import java.util.Arrays;
import java.util.List;

public class ProductFormDialog {

    private TextView tvDialogTitle;
    private EditText etProductName;
    private TextView tvProductNameError;
    private Button btnSubmit;

    private final Context context;
    private Dialog dialog;

    private LoadingDialog loadingDialog;
    private MessageDialog messageDialog;

    private ComponentManager componentManager;

    private int id;
    private String productName;

    public ProductFormDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_product_form_layout);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        etProductName = dialog.findViewById(R.id.etProductName);
        tvProductNameError = dialog.findViewById(R.id.tvProductNameError);
        btnSubmit = dialog.findViewById(R.id.btnSubmit);
        ImageView imgClose = dialog.findViewById(R.id.imgClose);

        List<TextView> errorTextViewList =
                Arrays.asList(tvProductNameError);
        List<EditText> errorEditTextList;
        errorEditTextList = Arrays.asList(etProductName);

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        componentManager = new ComponentManager(context);
        componentManager.initializeErrorComponents(errorTextViewList, errorEditTextList);

        btnSubmit.setOnClickListener(view -> {
            componentManager.checkInput(productName, true, context.getString(R.string.product_name), tvProductNameError, etProductName);

            if (componentManager.isNoInputError()) {
                submit();
            }
        });

        imgClose.setOnClickListener(view -> dismissDialog());

        etProductName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                productName = editable != null ? editable.toString() : "";

                componentManager.setInputRightDrawable(etProductName, !Credentials.isEmpty(productName), Enums.CLEAR_TEXT);
                componentManager.checkInput(productName, true, context.getString(R.string.product_name), tvProductNameError, etProductName);
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
            tvDialogTitle.setText(context.getString(R.string.add_record, "Product"));
            btnSubmit.setText(context.getString(R.string.add));
        } else {
            tvDialogTitle.setText(context.getString(R.string.update_record, "Product"));
            btnSubmit.setText(context.getString(R.string.update));
        }
    }

    public void dismissDialog() {
        dialog.dismiss();

        id = 0;

        etProductName.getText().clear();

        componentManager.hideInputErrors();
    }

    private void submit() {
        loadingDialog.showDialog();

        MyDatabaseHelper myDatabaseHelper = new MyDatabaseHelper(context);

        if (id == 0) {
            myDatabaseHelper.addProduct(Credentials.fullTrim(productName));
        } else {
            myDatabaseHelper.updateProduct(id, Credentials.fullTrim(productName));
        }

        loadingDialog.dismissDialog();
        dismissDialog();

        if (dialogListener != null) {
            dialogListener.onSubmit();
        }
    }

    public void setUpdateData(Product product) {
        this.id = product.getId();
        this.productName = product.getName();

        etProductName.setText(productName);
    }

    DialogListener dialogListener;

    public interface DialogListener {
        void onSubmit();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
