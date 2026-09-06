package com.elifelize.webdev2eli;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopProperties shopProperties;

    public ProductService(ProductRepository productRepository, ShopProperties shopProperties) {
        this.productRepository = productRepository;
        this.shopProperties = shopProperties;
    }

    public List<Product> getProductsAbovePrice(double threshold) {
        return productRepository.findAll().stream()
                .filter(product -> product.getPrice() > threshold)
                .collect(Collectors.toList());
    }

    public String getShopName() {
        return shopProperties.getName();
    }

    public String getShopCurrency() {
        return shopProperties.getCurrency();
    }
}