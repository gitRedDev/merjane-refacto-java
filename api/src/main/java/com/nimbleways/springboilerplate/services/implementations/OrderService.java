package com.nimbleways.springboilerplate.services.implementations;


import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enumerations.ProductTypeEnum;
import com.nimbleways.springboilerplate.exceptions.NotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final ProductService productService;
    private final OrderRepository orderRepository;


    private Order getOrderById(Long orderId) throws NotFoundException {
        log.info("Start service getOrderById with id : {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id " + orderId));

        log.info("End service getOrderById with id : {}", orderId);
        return order;

    }

    public ProcessOrderResponse processOrder(Long orderId) throws NotFoundException {
        log.info("Start service processOrder with id : {}", orderId);

        Order order = getOrderById(orderId);
        List<Product> productsToSave = new ArrayList<>();

        Map<ProductTypeEnum, Function<Product, Boolean>> productHandlers = Map.of(
                ProductTypeEnum.NORMAL, productService::handleNormalProduct,
                ProductTypeEnum.SEASONAL, productService::handleSeasonalProduct,
                ProductTypeEnum.EXPIRABLE, productService::handleExpirableProduct
        );

        for (Product p : order.getItems()) {
            boolean toSave = productHandlers.get(p.getType()).apply(p);
            if (toSave) {
                productsToSave.add(p);
            }
        }
        productService.saveProducts(productsToSave);

        log.info("End service processOrder with id : {}", order.getId());
        return new ProcessOrderResponse(order.getId());
    }

}
