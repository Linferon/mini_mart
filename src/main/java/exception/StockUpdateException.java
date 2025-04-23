package exception;

public class  StockUpdateException extends IllegalArgumentException {
    public StockUpdateException(String message) {
        super(message);
    }
}
