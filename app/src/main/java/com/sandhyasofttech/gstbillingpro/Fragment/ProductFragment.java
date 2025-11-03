package com.sandhyasofttech.gstbillingpro.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class ProductFragment extends Fragment {

    private RecyclerView rvProducts;
    private FloatingActionButton fabAddProduct;

    private ArrayList<Product> productList = new ArrayList<>();
    private ProductsAdapter adapter;

    private String userMobile;
    private DatabaseReference productsRef;

    private static final int REQUEST_CODE_ADD_PRODUCT = 100;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProducts = view.findViewById(R.id.rvProducts);
        fabAddProduct = view.findViewById(R.id.fabAddProduct);

        SharedPreferences prefs = requireActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        userMobile = prefs.getString("USER_MOBILE", null);
        if (userMobile == null) {
            Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }
        productsRef = FirebaseDatabase.getInstance().getReference("users").child(userMobile).child("products");

        adapter = new ProductsAdapter(productList);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        loadProducts();

        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NewProductActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_PRODUCT);
        });
    }

    private void loadProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Product product = ds.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_PRODUCT && getActivity() != null && resultCode == getActivity().RESULT_OK) {
            loadProducts();
        }
    }
}
