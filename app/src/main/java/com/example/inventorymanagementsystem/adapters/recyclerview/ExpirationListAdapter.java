package com.example.inventorymanagementsystem.adapters.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorymanagementsystem.R;
import com.example.inventorymanagementsystem.classes.DateTime;
import com.example.inventorymanagementsystem.classes.Units;
import com.example.inventorymanagementsystem.data_model.Expiration;

import java.util.List;

public class ExpirationListAdapter extends RecyclerView.Adapter {

    private final LayoutInflater layoutInflater;

    private final List<Expiration> expirationList;

    private final Context context;

    public ExpirationListAdapter(Context context, List<Expiration> expirationList) {
        this.expirationList = expirationList;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.template_product_expirations_layout, parent, false);
        return new ProductHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProductHolder productHolder = (ProductHolder) holder;

        CardView cardRecord = productHolder.cardRecord;
        ConstraintLayout layoutMarkColor = productHolder.layoutMarkColor;
        TextView tvExpirationDate = productHolder.tvExpirationDate;
        TextView tvTag = productHolder.tvTag;
        ImageView imgUpdate = productHolder.imgUpdate,
                imgDelete = productHolder.imgDelete;

        Expiration expiration = expirationList.get(position);

        String expirationDate = new DateTime(expiration.getDate()).getDateText();

        tvExpirationDate.setText(expirationDate);
        tvTag.setText(expiration.getTag());

        int daysDiff = (int) Units.msToDay(expiration.getDate() - new DateTime().getDateTimeValue());
        if (daysDiff < 0) {
            layoutMarkColor.setBackgroundColor(context.getResources().getColor(R.color.dark));
        } else if (daysDiff <= 1) {
            layoutMarkColor.setBackgroundColor(context.getResources().getColor(R.color.red));
        } else if (daysDiff <= 7) {
            layoutMarkColor.setBackgroundColor(context.getResources().getColor(R.color.dark_orange));
        } else if (daysDiff <= 30) {
            layoutMarkColor.setBackgroundColor(context.getResources().getColor(R.color.yellow));
        } else {
            layoutMarkColor.setBackgroundColor(context.getResources().getColor(R.color.lime));
        }

        imgUpdate.setOnClickListener(view -> {
            if (adapterListener != null) {
                adapterListener.onUpdate(expiration);
            }
        });

        imgDelete.setOnClickListener(view -> {
            if (adapterListener != null) {
                adapterListener.onDelete(expiration);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expirationList.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {

        private final CardView cardRecord;
        private final ConstraintLayout layoutMarkColor;
        private final TextView tvExpirationDate;
        private final TextView tvTag;
        private final ImageView imgUpdate, imgDelete;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);

            cardRecord = itemView.findViewById(R.id.cardRecord);
            layoutMarkColor = itemView.findViewById(R.id.layoutMarkColor);
            tvExpirationDate = itemView.findViewById(R.id.tvExpirationDate);
            tvTag = itemView.findViewById(R.id.tvTag);
            imgUpdate = itemView.findViewById(R.id.imgUpdate);
            imgDelete = itemView.findViewById(R.id.imgDelete);

            setIsRecyclable(false);
        }
    }

    AdapterListener adapterListener;

    public interface AdapterListener {
        void onUpdate(Expiration expiration);
        void onDelete(Expiration expiration);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
