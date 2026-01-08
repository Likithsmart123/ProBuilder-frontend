package com.example.probuilder;

import java.util.ArrayList;
import java.util.List;

public class SupplierRepository {
    private static SupplierRepository instance;
    private List<Supplier> suppliers;

    private SupplierRepository() {
        suppliers = new ArrayList<>();
        // Mock Data
        suppliers.add(new Supplier("1", "BuildSafe Cements", "Cement", "+91 98765 43210", "Active", 4.5f));
        suppliers.add(new Supplier("2", "Steel Strong Traders", "Steel", "+91 98765 43211", "Active", 4.2f));
        suppliers.add(new Supplier("3", "City Key Bricks", "Bricks", "+91 98765 43212", "Inactive", 3.8f));
        suppliers.add(new Supplier("4", "Electro World", "Electrical", "+91 98765 43213", "Active", 4.8f));
        suppliers.add(new Supplier("5", "Aqua Pipes & Sanitaries", "Plumbing", "+91 98765 43214", "Active", 4.0f));
    }

    public static synchronized SupplierRepository getInstance() {
        if (instance == null) {
            instance = new SupplierRepository();
        }
        return instance;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }
}
