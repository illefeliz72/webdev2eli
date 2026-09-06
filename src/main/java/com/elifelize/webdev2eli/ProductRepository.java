package com.elifelize.webdev2eli;

import org.springframework.stereotype.Repository;
import java.util.Arrays;
import java.util.List;

@Repository
public class ProductRepository {

    public List<Product> findAll() {
        return Arrays.asList(
            new Product("Laptop", 45000),
            new Product("Mouse", 800),
            new Product("Keyboard", 1500),
            new Product("Monitor", 12000),
            new Product("Headset", 2500),
            new Product("Printer", 8500)
        );
    }
}