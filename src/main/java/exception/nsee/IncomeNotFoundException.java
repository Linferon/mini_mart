package exception.nsee;
import java.util.NoSuchElementException;

public class  IncomeNotFoundException extends NoSuchElementException {
    public IncomeNotFoundException(String message) {
        super(message);
    }
}
