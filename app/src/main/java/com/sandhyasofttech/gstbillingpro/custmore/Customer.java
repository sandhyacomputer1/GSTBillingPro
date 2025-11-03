package com.sandhyasofttech.gstbillingpro.custmore;

public class Customer {
    public String id;  // mobile number used as id
    public String name;
    public String phone;
    public String email;
    public String gstin;
    public String address;

    public Customer() { }

    public Customer(String id, String name, String phone, String email, String gstin, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.gstin = gstin;
        this.address = address;
    }
}
