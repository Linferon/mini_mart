package util;

import exception.nsee.*;

import java.util.*;
import java.util.function.Supplier;

import static util.LoggerUtil.info;
import static util.LoggerUtil.warn;

public class EntityUtil {

    private EntityUtil() {
    }

    private record EntityMeta(
            Class<? extends RuntimeException> exceptionClass,
            String errorMessage,
            boolean logFlag,
            String logLabel
    ) {
    }

    private static final Map<String, EntityMeta> ENTITY_META_MAP = new HashMap<>();

    static {
        ENTITY_META_MAP.put("ExpenseCategory", new EntityMeta(CategoryNotFoundException.class, "Категории затрат не найдены", true, "Получено категорий затрат: "));
        ENTITY_META_MAP.put("Expense", new EntityMeta(ExpenseNotFoundException.class, "Расходы не найдены", false, ""));
        ENTITY_META_MAP.put("Income", new EntityMeta(IncomeNotFoundException.class, "Доходы не найдены", false, ""));
        ENTITY_META_MAP.put("IncomeSource", new EntityMeta(SourceNotFoundException.class, "Источники дохода не найдены", true, "Получено источников дохода: "));
        ENTITY_META_MAP.put("MonthlyBudget", new EntityMeta(BudgetNotFoundException.class, "Бюджеты не найдены", false, ""));
        ENTITY_META_MAP.put("Payroll", new EntityMeta(PayrollNotFoundException.class, "Зарплаты не найдены", false, ""));
        ENTITY_META_MAP.put("ProductCategory", new EntityMeta(CategoryNotFoundException.class, "Категории продуктов не были найдены!", true, "Получено категорий: "));
        ENTITY_META_MAP.put("Product", new EntityMeta(ProductNotFoundException.class, "Продукты не были найдены!", true, "Получено продуктов: "));
        ENTITY_META_MAP.put("Purchase", new EntityMeta(PurchaseNotFoundException.class, "Покупки не найдены", false, ""));
        ENTITY_META_MAP.put("Role", new EntityMeta(RoleNotFoundException.class, "Роли не найдены", true, "Получено ролей: "));
        ENTITY_META_MAP.put("Sale", new EntityMeta(SaleNotFoundException.class, "Продажи не найдены", false, ""));
        ENTITY_META_MAP.put("Stock", new EntityMeta(ProductNotFoundException.class, "Нет продуктов на складе!", true, "Количество продуктов на складе: "));
        ENTITY_META_MAP.put("User", new EntityMeta(UserNotFoundException.class, "Сотрудники не были найдены!", true, "Получено сотрудников: "));
    }

    public static <T> List<T> findAndValidate(Supplier<List<T>> supplier, String customErrorMessage) {
        Objects.requireNonNull(supplier, "Поставщик данных не может быть null");

        List<T> entities = supplier.get();

        if (!entities.isEmpty()) {
            String entityClassName = entities.get(0).getClass().getSimpleName();
            EntityMeta meta = ENTITY_META_MAP.get(entityClassName);

            if (meta != null && meta.logFlag()) {
                info(meta.logLabel() + entities.size());
            }

            return entities;
        }

        String entityClassName = determineEntityClassName(supplier);
        EntityMeta meta = ENTITY_META_MAP.get(entityClassName);

        String message = (customErrorMessage != null && !customErrorMessage.isEmpty())
                ? customErrorMessage
                : (meta != null ? meta.errorMessage() : "Записи не найдены");

        warn(message);
        throw createException(meta, message);
    }

    public static <T> List<T> findAndValidate(Supplier<List<T>> supplier) {
        return findAndValidate(supplier, null);
    }

    private static <T> String determineEntityClassName(Supplier<List<T>> supplier) {
        List<T> tempList = supplier.get();
        return tempList.isEmpty() ? "Unknown" : tempList.get(0).getClass().getSimpleName();
    }

    private static RuntimeException createException(EntityMeta meta, String message) {
        if (meta == null) return new RuntimeException(message);
        try {
            return meta.exceptionClass().getConstructor(String.class).newInstance(message);
        } catch (Exception e) {
            return new RuntimeException(message);
        }
    }
}
