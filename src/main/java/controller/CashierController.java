package controller;

import model.Sale;
import model.Stock;
import service.SaleService;
import service.StockService;
import exception.handler.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

import static util.ConsoleUtil.*;
import static util.InputHandler.getIntInput;
import static util.InputHandler.getLongInput;

public class CashierController extends BaseController {
    private final StockService stockService;
    private final SaleService saleService;

    public CashierController() {
        stockService = StockService.getInstance();
        saleService = SaleService.getInstance();
    }

    @Override
    public void showMenu() {
        createMenu("Меню Кассира")
                .addMenuItem("Продать товар", this::sellProduct)
                .addMenuItem("Просмотреть продажи", this::viewSales)
                .addExitItem("Выйти из системы")
                .show();
    }

    private void sellProduct() {
        ExceptionHandler.execute(() -> {
            List<Stock> availableStock = stockService.getAvailableProducts();
            showEntitiesTable(availableStock, "Доступные товары");

            Long productId = getLongInput("Введите ID продукта: ");
            Stock stock = stockService.getStockByProductId(productId);

            int quantity = getIntInput("Введите количество: ");

            if (stock.getQuantity() < quantity) {
                showError("Недостаточно товара на складе. Доступно: " + stock.getQuantity());
                return;
            }

            Sale sale = saleService.addSale(productId, quantity, LocalDateTime.now());
            showSuccess("Продажа успешно совершена. Сумма к оплате: " + sale.getTotalAmount() + " руб.");

            printCheck(sale);
        });
    }

    private void viewSales() {
        ExceptionHandler.execute(() -> showDateRangeMenu((startDate, endDate) -> {
            List<Sale> sales = saleService.getSalesByDateRange(startDate, endDate);
            showEntitiesTable(sales, "Список продаж за период " + startDate + " - " + endDate);
        }));
    }

    private void printCheck(Sale sale) {
        printHeader("ЧЕК");
        println("Товар: " + sale.getProduct().getName());
        println("Количество: " + sale.getQuantity());
        println("Цена за единицу: " + sale.getProduct().getSellPrice() + " руб.");
        println("Итого: " + sale.getTotalAmount() + " руб.");
        println("Дата: " + LocalDateTime.now());
        printDivider();
    }
}