package ui;

import exception.nsee.ProductNotFoundException;
import model.*;
import service.*;
import util.*;
import exception.handler.ExceptionHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class StockKeeperUI extends BaseUI {
    private final ProductService productService = ProductService.getInstance();
    private final ProductCategoryService categoryService = ProductCategoryService.getInstance();
    private final StockService stockService = StockService.getInstance();
    private final PurchaseService purchaseService = PurchaseService.getInstance();
    private static final List<ProductCategory> categories = ProductCategoryService.getInstance().getAllCategories();

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

            ConsoleUtil.printHeader("Доступные категории продуктов");
            ConsoleUtil.println(TableFormatter.formatTable(categories));

            Long categoryId = InputHandler.getLongInput("Введите ID категории продукта: ");
            ProductCategory category = categoryService.getCategoryById(categoryId);

            BigDecimal buyPrice = InputHandler.getBigDecimalInput("Введите цену закупки: ");
            BigDecimal sellPrice = InputHandler.getBigDecimalInput("Введите цену продажи: ");

            Product product = new Product(null, name, category, buyPrice, sellPrice);
            productService.addProduct(product);
            showSuccess("Продукт успешно добавлен.");
        }, "Ошибка при добавлении продукта");
    }

    private void viewProducts() {
        ExceptionHandler.execute(() -> {
            List<Product> products = productService.getAllProducts();

            ConsoleUtil.printHeader("Список продуктов");
            ConsoleUtil.println(TableFormatter.formatTable(products));
        }, "Ошибка при просмотре продуктов");
    }

    private void updateProduct() {
        ExceptionHandler.execute(() -> {
            viewProducts();
            Long productId = InputHandler.getLongInput("Введите ID продукта для обновления: ");
            Product product = productService.getProductById(productId);

            ConsoleUtil.printHeader("Текущие данные продукта");
            ConsoleUtil.println(product.toString());
            ConsoleUtil.printDivider();

            updateProductFields(product);
            productService.updateProduct(product);
            showSuccess("Продукт успешно обновлен.");
        }, "Ошибка при обновлении продукта");
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

        if (InputHandler.getStringInput("Хотите изменить категорию? (да/нет): ").equalsIgnoreCase("да")) {
            ConsoleUtil.printHeader("Доступные категории");
            ConsoleUtil.println(TableFormatter.formatTable(categories));

            Long categoryId = InputHandler.getLongInput("Введите ID категории: ");
            product.setCategory(categoryService.getCategoryById(categoryId));
        }
    }

    private void deleteProduct() {
        ExceptionHandler.execute(() -> {
            viewProducts();
            Long productId = InputHandler.getLongInput("Введите ID продукта для удаления: ");
            Product product = productService.getProductById(productId);

            ConsoleUtil.println("Вы собираетесь удалить продукт:");
            ConsoleUtil.println(product.toString());

            showConfirmationMenu("Вы уверены, что хотите удалить этот продукт?", () -> {
                productService.deleteProduct(productId);
                showSuccess("Продукт успешно удален.");
            });
        }, "Ошибка при удалении продукта");
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

        }, "Ошибка при ручном добавлении товара на склад");
    }



    private void viewAllStock() {
        ExceptionHandler.execute(() -> {
            List<Stock> stocks = stockService.getAllStock();

            ConsoleUtil.printHeader("Остатки на складе");
            ConsoleUtil.println(TableFormatter.formatTable(stocks));

        }, "Ошибка при просмотре остатков");
    }

    private void viewLowStock() {
        ExceptionHandler.execute(() -> {
            int threshold = InputHandler.getIntInput("Введите порог низкого остатка: ");
            List<Stock> lowStocks = stockService.getLowStockProducts(threshold);

            ConsoleUtil.printHeader("Товары с низким остатком (меньше " + threshold + ")");
            ConsoleUtil.println(TableFormatter.formatTable(lowStocks));
        }, "Ошибка при просмотре товаров с низким остатком");
    }

    private void viewOutOfStock() {
        ExceptionHandler.execute(() -> {
            List<Stock> outOfStocks = stockService.getOutOfStockProducts();

            ConsoleUtil.printHeader("Товары, отсутствующие на складе");
            ConsoleUtil.println(TableFormatter.formatTable(outOfStocks));
        }, "Ошибка при просмотре отсутствующих товаров");
    }

    private void updateStockQuantity() {
        ExceptionHandler.execute(() -> {
            viewAllStock();
            Long productId = InputHandler.getLongInput("Введите ID продукта: ");

            Stock currentStock = stockService.getStockByProductId(productId);
            Product product = currentStock.getProduct();

            ConsoleUtil.println("Текущий остаток товара '" + product.getName() + "': " + currentStock.getQuantity());
            int quantity;

            do {
                quantity = InputHandler.getIntInput("Введите новое количество: ");
                if(quantity <= 0) {
                    ConsoleUtil.println("Вы не можете установить отрицательное количество продукта!");
                }
            } while (quantity < 0);

            if (quantity < 5) {
                ConsoleUtil.println("Внимание! Установлен низкий остаток товара. Рекомендуется пополнить запасы.");
            }

            stockService.updateStockQuantity(productId, quantity);
            showSuccess("Количество товара успешно обновлено.");
        }, "Ошибка при обновлении количества товара");
    }

    private void deleteStock() {
        ExceptionHandler.execute(() -> {
            viewAllStock();
            Long productId = InputHandler.getLongInput("Введите ID продукта для удаления со склада: ");

            Stock stock = stockService.getStockByProductId(productId);

            ConsoleUtil.println("Вы собираетесь удалить следующую запись склада:");
            ConsoleUtil.println(stock.toString());

            showConfirmationMenu("Вы уверены, что хотите удалить эту запись склада?", () -> {
                boolean deleted = stockService.deleteStock(productId);
                if (deleted) {
                    showSuccess("Запись склада успешно удалена.");
                } else {
                    ConsoleUtil.println("Не удалось удалить запись склада.");
                }
            });
        }, "Ошибка при удалении записи склада");
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

            handlePriceUpdate(product, quantity, totalCost);
        }, "Ошибка при добавлении закупки");
    }

    private void updatePurchase() {
        ExceptionHandler.execute(() -> {
            viewPurchases();
            Long purchaseId = InputHandler.getLongInput("Введите ID закупки для обновления: ");
            Purchase purchase = purchaseService.getPurchaseById(purchaseId);

            ConsoleUtil.println("Текущая информация о закупке:");
            ConsoleUtil.println(purchase.toString());
            ConsoleUtil.printDivider();

            int newQuantity = InputHandler.getIntInput("Введите новое количество: ");
            purchaseService.updatePurchase(purchaseId, newQuantity);

            showSuccess("Закупка успешно обновлена.");
        }, "Ошибка при обновлении закупки");
    }

    private void deletePurchase() {
        ExceptionHandler.execute(() -> {
            viewPurchases();
            Long purchaseId = InputHandler.getLongInput("Введите ID закупки для удаления: ");
            Purchase purchase = purchaseService.getPurchaseById(purchaseId);

            ConsoleUtil.println("Вы собираетесь удалить следующую закупку:");
            ConsoleUtil.println(purchase.toString());

            showConfirmationMenu("Вы уверены, что хотите удалить эту закупку?", () -> {
                purchaseService.deletePurchase(purchaseId);
                showSuccess("Закупка успешно удалена.");
            });
        }, "Ошибка при удалении закупки");
    }


    private void displayStockAndProducts() {
        List<Stock> stocks = stockService.getAllStock();
        ConsoleUtil.printHeader("Текущие остатки товаров");
        ConsoleUtil.println(TableFormatter.formatTable(stocks));

        List<Product> products = productService.getAllProducts();
        ConsoleUtil.printHeader("Список всех продуктов");
        ConsoleUtil.println(TableFormatter.formatTable(products));
    }

    private void handlePriceUpdate(Product product, int quantity, BigDecimal totalCost) {
        BigDecimal unitCost = totalCost.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
        BigDecimal previousUnitCost = product.getBuyPrice();

        ConsoleUtil.println("Цена за единицу: " + unitCost + " (предыдущая: " + previousUnitCost + ")");

        if (unitCost.compareTo(previousUnitCost) != 0) {
            String updatePrice = InputHandler.getStringInput("Цена за единицу изменилась. Обновить цену закупки в продукте? (да/нет): ");
            if (updatePrice.equalsIgnoreCase("да")) {
                product.setBuyPrice(unitCost);
                productService.updateProduct(product);
                showSuccess("Цена закупки продукта обновлена.");
            }
        }
    }

    private void viewPurchases() {
        showDateRangeMenu((startDate, endDate) -> {
            List<Purchase> purchases = purchaseService.getPurchasesByDateRange(
                    DateTimeUtils.startOfDay(startDate),
                    DateTimeUtils.endOfDay(endDate)
            );

            ConsoleUtil.printHeader("Список закупок за период " + startDate + " - " + endDate);
            if (purchases.isEmpty()) {
                ConsoleUtil.println("За выбранный период закупок не найдено.");
                return;
            }

            ConsoleUtil.println(TableFormatter.formatTable(purchases));
        });
    }
}