package hu.ujfalusis.obm;

public class OBMException extends RuntimeException {

    public OBMException() {
        super();
    }

    public OBMException(String message) {
        super(message);
    }

    public OBMException(String message, Throwable cause) {
        super(message, cause);
    }

    public OBMException(Throwable cause) {
        super(cause);
    }

}
