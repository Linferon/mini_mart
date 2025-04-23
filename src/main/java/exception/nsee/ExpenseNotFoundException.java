package exception.nsee;
import java.util.NoSuchElementException;

public class  ExpenseNotFoundException extends NoSuchElementException {
    public ExpenseNotFoundException(String message) {
        super(message);
    }
}
