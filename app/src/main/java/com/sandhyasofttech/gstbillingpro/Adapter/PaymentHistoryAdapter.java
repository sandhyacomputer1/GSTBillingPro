package com.sandhyasofttech.gstbillingpro.Adapter;

import android.view.*;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.sandhyasofttech.gstbillingpro.Model.PaymentHistoryModel;
import com.sandhyasofttech.gstbillingpro.R;
import java.util.List;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.VH> {

    List<PaymentHistoryModel> list;

    public PaymentHistoryAdapter(List<PaymentHistoryModel> list) {
        this.list = list;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.row_payment_history, p, false));
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        PaymentHistoryModel m = list.get(i);
        h.tvDate.setText(m.date + " " + m.time);
        h.tvAmount.setText("Paid ₹" + m.paidNow);
        h.tvMode.setText(m.paymentMode);
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvAmount, tvMode;
        VH(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDate);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvMode = v.findViewById(R.id.tvMode);
        }
    }
}
