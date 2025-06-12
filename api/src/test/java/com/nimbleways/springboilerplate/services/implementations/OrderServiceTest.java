package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enumerations.ProductTypeEnum;
import com.nimbleways.springboilerplate.exceptions.NotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {


    @Mock
    private ProductService productService;
    @Mock
    private OrderRepository orderRepository;

    @Spy
    @InjectMocks
    private OrderService orderService;


    @Test
    void processOrder() throws NotFoundException {
        //Given
        Product product = Product.builder()
                .available(5)
                .type(ProductTypeEnum.EXPIRABLE)
                .expiryDate(LocalDate.now().minusDays(10))
                .build();

        Order order = new Order(
                1L,
                Set.of(product)
        );


        doReturn(Optional.of(order)).when(orderRepository).findById(1L);
        doReturn(false).when(productService).handleExpirableProduct(any());
        doNothing().when(productService).saveProducts(anyList());

        //When
        ProcessOrderResponse p = orderService.processOrder(1L);

        //Then
        Assertions.assertThat(p.id()).isEqualTo(1L);
    }
}