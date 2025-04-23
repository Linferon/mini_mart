package exception.nsee;
import java.util.NoSuchElementException;

public class  SourceNotFoundException extends NoSuchElementException {
    public SourceNotFoundException(String message) {
        super(message);
    }
}
