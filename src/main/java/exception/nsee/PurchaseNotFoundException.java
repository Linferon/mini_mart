package exception.nsee;
import java.util.NoSuchElementException;

public class  PurchaseNotFoundException extends NoSuchElementException {
    public PurchaseNotFoundException(String message) {
        super(message);
    }
}
