package com.sandhyasofttech.gstbillingpro.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.sandhyasofttech.gstbillingpro.Activity.NewProductActivity;
import com.sandhyasofttech.gstbillingpro.Adapter.ProductsAdapter;
import com.sandhyasofttech.gstbillingpro.Model.Product;
import com.sandhyasofttech.gstbillingpro.R;

import java.util.ArrayList;

public class ProductFragment extends Fragment implements ProductsAdapter.OnProductActionListener {

    private RecyclerView rvProducts;
    private FloatingActionButton fabAddProduct;
    private ProductsAdapter adapter;
    private ArrayList<Product> productList = new ArrayList<>();
    private ArrayList<Product> filteredList = new ArrayList<>();
    private DatabaseReference productsRef;
    private String userMobile;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProducts = view.findViewById(R.id.rvProducts);
        fabAddProduct = view.findViewById(R.id.fabAddProduct);
        searchView = view.findViewById(R.id.searchView);

        SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);
        if (userMobile == null) return;

        productsRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("products");

        adapter = new ProductsAdapter(filteredList, this);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        loadProducts();

        fabAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NewProductActivity.class));
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return false;
            }
        });
    }

    private void loadProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);
                    if (product != null) productList.add(product);
                }
                filteredList.clear();
                filteredList.addAll(productList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            query = query.toLowerCase();
            for (Product p : productList) {
                if (p.getName().toLowerCase().contains(query)) filteredList.add(p);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEditProduct(Product product, int position) {
        showEditDialog(product);
    }

    @Override
    public void onDeleteProduct(Product product, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    productsRef.child(product.getProductId()).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showEditDialog(Product product) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_product, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etHsn = dialogView.findViewById(R.id.etHsn);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etGst = dialogView.findViewById(R.id.etGst);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);

        // Pre-fill data
        etName.setText(product.getName());
        etHsn.setText(product.getHsnCode());
        etPrice.setText(String.valueOf(product.getPrice()));
        etGst.setText(String.valueOf(product.getGstRate()));
        etQuantity.setText(String.valueOf(product.getStockQuantity()));

        btnUpdate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String hsn = etHsn.getText().toString().trim();
            double price = Double.parseDouble(etPrice.getText().toString().trim());
            double gst = Double.parseDouble(etGst.getText().toString().trim());
            int quantity = Integer.parseInt(etQuantity.getText().toString().trim());

            // Update product object
            product.setName(name);
            product.setHsnCode(hsn);
            product.setPrice(price);
            product.setGstRate(gst);
            product.setStockQuantity(quantity);

            // Update in Firebase
            productsRef.child(product.getProductId()).setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }
}
