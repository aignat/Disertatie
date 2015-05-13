package exception;

/**
 * Created by aignat on 5/12/2015.
 */
public class CustomException extends Exception {

    String originatingMethodName;

    public CustomException(String message, String originatingMethodName) {
        super(message);
        this.originatingMethodName = originatingMethodName;
    }

    public String getOriginatingMethodName() {
        return originatingMethodName;
    }
}
