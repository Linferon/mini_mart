package service;

import model.Product;
import model.Sale;
import model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SaleService {
    List<Sale> getAllSales();

    Sale getSaleById(Long id);

    List<Sale> getSalesByProduct(Long productId);

    List<Sale> getSalesByCashier(Long cashierId);

    List<Sale> getSalesByDateRange(LocalDate startDate, LocalDate endDate);

    Long addSale(Sale sale);

    Long addSale(Long productId, Integer quantity, LocalDateTime saleDateTime);

    boolean updateSale(Sale sale);

    boolean deleteSale(Long id);

    BigDecimal getTotalSalesAmount(LocalDate startDate, LocalDate endDate);

    Map<Product, Integer> getTopSellingProducts(LocalDate startDate, LocalDate endDate, int limit);

    Map<User, BigDecimal> getSalesByCashiers(LocalDate startDate, LocalDate endDate);

    BigDecimal getDailySalesAverage(LocalDate startDate, LocalDate endDate);
}
