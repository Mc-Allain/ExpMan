package com.example.inventorymanagementsystem.adapters.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorymanagementsystem.R;
import com.example.inventorymanagementsystem.data_model.Product;
import com.example.inventorymanagementsystem.dialogs.ProductFormDialog;

import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter {

    private final LayoutInflater layoutInflater;

    private final List<Product> productList;

    public ProductListAdapter(Context context, List<Product> productList) {
        this.productList = productList;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.template_product_layout, parent, false);
        return new ProductHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProductHolder productHolder = (ProductHolder) holder;

        CardView cardRecord = productHolder.cardRecord;
        TextView tvProductName = productHolder.tvProductName;
        TextView tvExpirationCount = productHolder.tvExpirationCount;

        Product product = productList.get(position);

        tvProductName.setText(product.getName());
        String expirationCount = product.getExpirations().size() + "×";
        tvExpirationCount.setText(expirationCount);

        cardRecord.setOnClickListener(view -> {
            if (adapterListener != null) {
                adapterListener.onClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {

        private final CardView cardRecord;
        private final TextView tvProductName;
        private final TextView tvExpirationCount;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);

            cardRecord = itemView.findViewById(R.id.cardRecord);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvExpirationCount = itemView.findViewById(R.id.tvExpirationCount);

            setIsRecyclable(false);
        }
    }

    AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(Product product);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
