package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.*;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttech.gstbillingpro.Model.InvModel;
import com.sandhyasofttech.gstbillingpro.R;
import java.util.List;

public class InvAdapter extends RecyclerView.Adapter<InvAdapter.VH> {

    List<InvModel> list;

    public InvAdapter(List<InvModel> list) {
        this.list = list;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.row_invoice_item, p, false));
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        InvModel m = list.get(i);
        h.tvName.setText(m.productName);
        h.tvQty.setText("Qty: " + m.quantity);
        h.tvRate.setText("Rate: ₹" + m.rate);
        h.tvTax.setText("Tax: " + m.taxPercent + "%");
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvRate, tvTax;
        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvProductName);
            tvQty = v.findViewById(R.id.tvQty);
            tvRate = v.findViewById(R.id.tvRate);
            tvTax = v.findViewById(R.id.tvTax);
        }
    }
}
