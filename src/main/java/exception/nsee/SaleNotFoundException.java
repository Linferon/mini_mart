package exception.nsee;
import java.util.NoSuchElementException;

public class  SaleNotFoundException extends NoSuchElementException {
    public SaleNotFoundException(String message) {
        super(message);
    }
}
