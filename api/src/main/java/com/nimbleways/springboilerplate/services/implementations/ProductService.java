package com.nimbleways.springboilerplate.services.implementations;


import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {


    private final NotificationService notificationService;
    private final ProductRepository productRepository;


    //returns whether the product has changed and its changes should be persisted
    public boolean handleNormalProduct(Product p) {
        if (p.getAvailable() > 0) {
            decrementProductStock(p);
            return true;
        } else {
            notifyDelay(p);
        }
        return false;
    }

    //returns whether the product has changed and its changes should be persisted
    public boolean handleSeasonalProduct(Product p) {
        if (p.getAvailable() > 0) {
            if ((LocalDate.now().isAfter(p.getSeasonStartDate()) && LocalDate.now().isBefore(p.getSeasonEndDate()))) {
                decrementProductStock(p);
                return true;
            }
        } else {
            if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonStartDate()) && LocalDate.now().plusDays(p.getLeadTime()).isBefore(p.getSeasonEndDate())) {
                notifyDelay(p);
            }
            else if (LocalDate.now().plusDays(p.getLeadTime()).isBefore(p.getSeasonEndDate())) {
                notificationService.sendOutOfStockNotification(p.getName());
            }
        }
        return false;
    }

    //returns whether the product has changed and its changes should be persisted
    public boolean handleExpirableProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            decrementProductStock(p);
            return true;
        } else {
            notificationService.sendExpirationNotification(p.getName(), p.getExpiryDate());
        }
        return false;
    }

    private void decrementProductStock(Product product) {
        log.info("Decrementing stock for product id: {}", product.getId());
        product.setAvailable(product.getAvailable() - 1);
    }

    public void saveProducts(List<Product> products){
        log.info("Start service saveProducts | products size: {}", products.size());
        productRepository.saveAll(products);
        log.info("End service saveProducts | products size: {}", products.size());
    }

    private void notifyDelay(Product p) {
        int leadTime = p.getLeadTime();
        if (leadTime > 0) {
            notificationService.sendDelayNotification(leadTime, p.getName());
        }
    }
}