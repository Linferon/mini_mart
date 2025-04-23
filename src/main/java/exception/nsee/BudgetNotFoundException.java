package exception.nsee;
import java.util.NoSuchElementException;

public class  BudgetNotFoundException extends NoSuchElementException {
    public BudgetNotFoundException(String message) {
        super(message);
    }
}
