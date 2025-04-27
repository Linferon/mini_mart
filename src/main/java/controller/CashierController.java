package controller;

import model.Sale;
import model.Stock;
import service.SaleService;
import service.StockService;
import util.ConsoleUtil;
import exception.handler.ExceptionHandler;
import util.InputHandler;
import util.TableFormatter;

import java.time.LocalDateTime;
import java.util.List;

public class CashierController extends BaseController {
    private final StockService stockService = StockService.getInstance();
    private final SaleService saleService = SaleService.getInstance();

    @Override
    public void showMenu() {
        createMenu("Меню Кассира")
                .addMenuItem("Продать товар", this::sellProduct)
                .addMenuItem("Просмотреть продажи", this::viewSales)
                .addMenuItem("Статистика продаж", this::salesStatistics)
                .addExitItem("Выйти из системы")
                .show();
    }

    private void sellProduct() {
        ExceptionHandler.execute(() -> {
            List<Stock> availableStock = stockService.getAvailableProducts();

            ConsoleUtil.printHeader("Доступные товары");
            ConsoleUtil.println(TableFormatter.formatTable(availableStock));

            Long productId = InputHandler.getLongInput("Введите ID продукта: ");
            Stock stock = stockService.getStockByProductId(productId);

            int quantity = InputHandler.getIntInput("Введите количество: ");

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

            ConsoleUtil.printHeader("Список продаж за период " + startDate + " - " + endDate);
            ConsoleUtil.println(TableFormatter.formatTable(sales));

        }));

    }

    private void salesStatistics() {
        ExceptionHandler.execute(() -> showDateRangeMenu((startDate, endDate) -> {
            List<Sale> sales = saleService.getSalesByDateRange(startDate, endDate);
            ConsoleUtil.printHeader("Статистика продаж за период " + startDate + " - " + endDate);

            int totalQuantity = sales.stream().mapToInt(Sale::getQuantity).sum();

            ConsoleUtil.println("Общее количество проданных товаров: " + totalQuantity + " шт.");
            ConsoleUtil.println("Количество продаж: " + sales.size());

        }));
    }

    private void printCheck(Sale sale) {
        ConsoleUtil.printHeader("ЧЕК");
        ConsoleUtil.println("Товар: " + sale.getProduct().getName());
        ConsoleUtil.println("Количество: " + sale.getQuantity());
        ConsoleUtil.println("Цена за единицу: " + sale.getProduct().getSellPrice() + " руб.");
        ConsoleUtil.println("Итого: " + sale.getTotalAmount() + " руб.");
        ConsoleUtil.println("Дата: " + LocalDateTime.now());
        ConsoleUtil.printDivider();
    }
}