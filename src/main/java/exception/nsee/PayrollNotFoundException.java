package exception.nsee;
import java.util.NoSuchElementException;

public class  PayrollNotFoundException extends NoSuchElementException {
    public PayrollNotFoundException(String message) {
        super(message);
    }
}
