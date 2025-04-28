package controller;

import exception.nsee.ProductNotFoundException;
import model.*;
import service.*;
import util.*;
import exception.handler.ExceptionHandler;

import java.math.BigDecimal;
import java.util.List;

public class StockKeeperController extends BaseController {
    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final StockService stockService;
    private final PurchaseService purchaseService;
    private final List<ProductCategory> categories;

    public StockKeeperController() {
        productService = ProductService.getInstance();
        categoryService = ProductCategoryService.getInstance();
        stockService = StockService.getInstance();
        purchaseService = PurchaseService.getInstance();
        categories = ProductCategoryService.getInstance().getAllCategories();
    }

    @Override
    public void showMenu() {
        createMenu("Меню Кладовщика")
                .addMenuItem("Управление продуктами", this::manageProducts)
                .addMenuItem("Управление запасами", this::manageStock)
                .addMenuItem("Управление закупками", this::managePurchases)
                .addExitItem("Выйти из системы")
                .show();
    }

    private void manageProducts() {
        createMenu("Управление продуктами")
                .addMenuItem("Добавить продукт", this::addProduct)
                .addMenuItem("Просмотреть продукты", this::viewProducts)
                .addMenuItem("Обновить продукт", this::updateProduct)
                .addMenuItem("Удалить продукт", this::deleteProduct)
                .addExitItem("Назад")
                .show();
    }

    private void addProduct() {
        ExceptionHandler.execute(() -> {
            String name = InputHandler.getStringInput("Введите название продукта: ");

            ConsoleUtil.showEntitiesTable(categories, "Доступные категории продуктов");

            Long categoryId = InputHandler.getLongInput("Введите ID категории продукта: ");
            BigDecimal buyPrice = InputHandler.getBigDecimalInput("Введите цену закупки: ");
            BigDecimal sellPrice = InputHandler.getBigDecimalInput("Введите цену продажи: ");

            productService.addProduct(name, categoryId, buyPrice, sellPrice);
            showSuccess("Продукт успешно добавлен.");
        });
    }

    private void viewProducts() {
        ExceptionHandler.execute(() -> {
            List<Product> products = productService.getAllProducts();
            ConsoleUtil.showEntitiesTable(products, "Список продуктов");
        });
    }

    private void updateProduct() {
        ExceptionHandler.execute(() -> {
            viewProducts();
            Long productId = InputHandler.getLongInput("Введите ID продукта для обновления: ");
            Product product = productService.getProductById(productId);

            ConsoleUtil.showEntityDetails(product, "Текущие данные продукта");

            updateProductFields(product);
            productService.updateProduct(product);
            showSuccess("Продукт успешно обновлен.");
        });
    }

    private void updateProductFields(Product product) {
        String name = InputHandler.getStringInput("Введите новое название (или Enter для текущего): ");
        if (!name.isEmpty()) {
            product.setName(name);
        }

        BigDecimal buyPrice = InputHandler.getBigDecimalInput("Введите новую цену закупки (или 0 для текущей): ");
        if (buyPrice.compareTo(BigDecimal.ZERO) > 0) {
            product.setBuyPrice(buyPrice);
        }

        BigDecimal sellPrice = InputHandler.getBigDecimalInput("Введите новую цену продажи (или 0 для текущей): ");
        if (sellPrice.compareTo(BigDecimal.ZERO) > 0) {
            product.setSellPrice(sellPrice);
        }

        showConfirmationMenu("Хотите изменить категорию?", () -> {
            ConsoleUtil.showEntitiesTable(categories, "Доступные категории");
            Long categoryId = InputHandler.getLongInput("Введите ID категории: ");
            product.setCategory(categoryService.getCategoryById(categoryId));
        });
    }

    private void deleteProduct() {
        ExceptionHandler.execute(() -> {
            viewProducts();
            Long productId = InputHandler.getLongInput("Введите ID продукта для удаления: ");
            Product product = productService.getProductById(productId);

            ConsoleUtil.showEntityDetails(product, "Вы собираетесь удалить продукт:");

            showConfirmationMenu("Вы уверены, что хотите удалить этот продукт?", () -> {
                productService.deleteProduct(productId);
                showSuccess("Продукт успешно удален.");
            });
        });
    }

    private void manageStock() {
        createMenu("Управление запасами")
                .addMenuItem("Добавить товар на склад (вручную)", this::addStockManually)
                .addMenuItem("Просмотреть остатки", this::viewAllStock)
                .addMenuItem("Просмотреть товары с низким остатком", this::viewLowStock)
                .addMenuItem("Просмотреть отсутствующие товары", this::viewOutOfStock)
                .addMenuItem("Обновить количество товара", this::updateStockQuantity)
                .addMenuItem("Удалить товар со склада", this::deleteStock)
                .addExitItem("Назад")
                .show();
    }

    private void addStockManually() {
        ExceptionHandler.execute(() -> {
            displayStockAndProducts();

            Long productId = InputHandler.getLongInput("Введите ID продукта для добавления на склад: ");
            int quantity = InputHandler.getIntInput("Введите количество: ");
            Product product = productService.getProductById(productId);

            Stock stock;
            try {
                stock = stockService.getStockByProductId(productId);
                stock.setQuantity(stock.getQuantity() + quantity);
                stockService.updateStockQuantity(productId, stock.getQuantity());
                showSuccess("Количество товара обновлено (ручное добавление).");

            } catch (ProductNotFoundException e) {
                stock = new Stock(product, quantity);
                stockService.addStock(stock);
                showSuccess("Продукт добавлен на склад вручную.");
            }
        });
    }

    private void viewAllStock() {
        ExceptionHandler.execute(() -> {
            List<Stock> stocks = stockService.getAllStock();
            ConsoleUtil.showEntitiesTable(stocks, "Остатки на складе");
        });
    }

    private void viewLowStock() {
        ExceptionHandler.execute(() -> {
            int threshold = InputHandler.getValidIntInput("Введите порог низкого остатка: ");
            List<Stock> lowStocks = stockService.getLowStockProducts(threshold);
            ConsoleUtil.showEntitiesTable(lowStocks, "Товары с низким остатком (меньше " + threshold + ")");
        });
    }

    private void viewOutOfStock() {
        ExceptionHandler.execute(() -> {
            List<Stock> outOfStocks = stockService.getOutOfStockProducts();
            ConsoleUtil.showEntitiesTable(outOfStocks, "Товары, отсутствующие на складе");
        });
    }

    private void updateStockQuantity() {
        ExceptionHandler.execute(() -> {
            viewAllStock();
            Long productId = InputHandler.getLongInput("Введите ID продукта: ");

            Stock currentStock = stockService.getStockByProductId(productId);
            Product product = currentStock.getProduct();
            ConsoleUtil.println("Текущий остаток товара '" + product.getName() + "': " + currentStock.getQuantity());

            int quantity = InputHandler.getValidIntInput("Введите новое количество товара");
            stockService.updateStockQuantity(productId, quantity);
            showSuccess("Количество товара успешно обновлено.");
        });
    }

    private void deleteStock() {
        ExceptionHandler.execute(() -> {
            viewAllStock();
            Long productId = InputHandler.getLongInput("Введите ID продукта для удаления со склада: ");
            Stock stock = stockService.getStockByProductId(productId);

            ConsoleUtil.showEntityDetails(stock, "Вы собираетесь удалить следующую запись склада:");

            showConfirmationMenu("Вы уверены, что хотите удалить эту запись склада?", () -> {
                boolean deleted = stockService.deleteStock(productId);
                if (deleted) {
                    showSuccess("Запись склада успешно удалена.");
                } else {
                    ConsoleUtil.println("Не удалось удалить запись склада.");
                }
            });
        });
    }

    private void managePurchases() {
        createMenu("Управление закупками")
                .addMenuItem("Добавить закупку", this::addPurchase)
                .addMenuItem("Просмотреть закупки", this::viewPurchases)
                .addMenuItem("Обновить закупку", this::updatePurchase)
                .addMenuItem("Удалить закупку", this::deletePurchase)
                .addExitItem("Назад")
                .show();
    }

    private void addPurchase() {
        ExceptionHandler.execute(() -> {
            displayStockAndProducts();
            Long productId = InputHandler.getLongInput("Введите ID продукта: ");
            int quantity = InputHandler.getIntInput("Введите количество: ");

            Product product = productService.getProductById(productId);

            BigDecimal totalCost = product.getBuyPrice().multiply(BigDecimal.valueOf(quantity));
            ConsoleUtil.println("Стоимость закупки (по текущей цене): " + totalCost);

            purchaseService.addPurchase(productId, quantity, totalCost);
            showSuccess("Закупка успешно добавлена.");
        });
    }

    private void updatePurchase() {
        ExceptionHandler.execute(() -> {
            viewPurchases();
            Long purchaseId = InputHandler.getLongInput("Введите ID закупки для обновления: ");
            Purchase purchase = purchaseService.getPurchaseById(purchaseId);

            ConsoleUtil.showEntityDetails(purchase, "Текущая информация о закупке:");

            int newQuantity = InputHandler.getValidIntInput("Введите новое количество: ");
            purchaseService.updatePurchase(purchaseId, newQuantity);
            showSuccess("Закупка успешно обновлена.");
        });
    }

    private void deletePurchase() {
        ExceptionHandler.execute(() -> {
            viewPurchases();
            Long purchaseId = InputHandler.getLongInput("Введите ID закупки для удаления: ");
            Purchase purchase = purchaseService.getPurchaseById(purchaseId);

            ConsoleUtil.showEntityDetails(purchase, "Вы собираетесь удалить следующую закупку:");

            showConfirmationMenu("Вы уверены, что хотите удалить эту закупку?", () -> {
                purchaseService.deletePurchase(purchaseId);
                showSuccess("Закупка успешно удалена.");
            });
        });
    }

    private void displayStockAndProducts() {
        List<Stock> stocks = stockService.getAllStock();
        ConsoleUtil.showEntitiesTable(stocks, "Текущие остатки товаров");

        List<Product> products = productService.getAllProducts();
        ConsoleUtil.showEntitiesTable(products, "Список всех продуктов");
    }

    private void viewPurchases() {
        showDateRangeMenu((startDate, endDate) -> {
            List<Purchase> purchases = purchaseService.getPurchasesByDateRange(
                    DateTimeUtils.startOfDay(startDate),
                    DateTimeUtils.endOfDay(endDate)
            );

            ConsoleUtil.showEntitiesTable(purchases, "Список закупок за период " + startDate + " - " + endDate);
        });
    }
}