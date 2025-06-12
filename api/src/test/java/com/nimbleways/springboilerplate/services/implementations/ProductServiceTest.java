package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @Spy
    @InjectMocks
    private ProductService productService;

    @Test
    void handleNormalProductAvailable() {
        //Given
        Product product = Product.builder().available(5).build();

        //When
        boolean result = productService.handleNormalProduct(product);

        //Then
        assertThat(result).isTrue();
        assertThat(product.getAvailable()).isEqualTo(4);
    }

    @Test
    void handleNormalProductNotAvailable() {
        //Given
        Product product = Product.builder().leadTime(5).available(0).build();
        Mockito.doNothing().when(notificationService).sendDelayNotification(product.getLeadTime(), product.getName());

        //When
        boolean result = productService.handleNormalProduct(product);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void handleSeasonalProductAvailable() {
        //Given
        Product product = Product.builder()
                .available(5)
                .seasonStartDate(LocalDate.now().minusDays(1))
                .seasonEndDate(LocalDate.now().plusDays(1))
                .build();

        //When
        boolean result = productService.handleSeasonalProduct(product);

        //Then
        assertThat(result).isTrue();
        assertThat(product.getAvailable()).isEqualTo(4);
    }

    @Test
    void handleSeasonalProductNotAvailable() {
        //Given
        Product product = Product.builder()
                .available(5)
                .leadTime(15)
                .seasonStartDate(LocalDate.now().minusDays(5))
                .seasonEndDate(LocalDate.now().minusDays(4))
                .build();

        //When
        boolean result = productService.handleSeasonalProduct(product);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void handleExpirableProductAvailable() {
        //Given
        Product product = Product.builder()
                .available(5)
                .expiryDate(LocalDate.now().plusDays(10))
                .build();

        //When
        boolean result = productService.handleExpirableProduct(product);

        //Then
        assertThat(result).isTrue();
        assertThat(product.getAvailable()).isEqualTo(4);
    }

    @Test
    void handleExpirableProductNotAvailable() {
        //Given
        Product product = Product.builder()
                .available(5)
                .expiryDate(LocalDate.now().minusDays(10))
                .build();
        Mockito.doNothing().when(notificationService).sendExpirationNotification(any(), any());

        //When
        boolean result = productService.handleExpirableProduct(product);

        //Then
        assertThat(result).isFalse();
    }

    @Test
    void saveProducts() {
        //Given
        Product product = Product.builder()
                .available(5)
                .expiryDate(LocalDate.now().minusDays(10))
                .build();
        List<Product> products = List.of(product);

        Mockito.doReturn(products).when(productRepository).saveAll(products);

        //When
        productService.saveProducts(products);

        //Then
        verify(productRepository, times(1)).saveAll(products);
    }
}
