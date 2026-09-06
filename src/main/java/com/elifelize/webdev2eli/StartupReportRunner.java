package com.elifelize.webdev2eli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupReportRunner implements CommandLineRunner {

    private final ProductService productService;

    public StartupReportRunner(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("================================");
        System.out.println("       PRODUCT REPORT");
        System.out.println("================================");
        System.out.println("Shop: " + productService.getShopName());
        System.out.println("Currency: " + productService.getShopCurrency());
        System.out.println();
        System.out.println("Products above " + productService.getShopCurrency() + " 5000:");
        System.out.println();

        for (Product p : productService.getProductsAbovePrice(5000)) {
            System.out.println(p.getName() + " - " + productService.getShopCurrency() + " " + (int) p.getPrice());
        }
        System.out.println("================================");
    }
}