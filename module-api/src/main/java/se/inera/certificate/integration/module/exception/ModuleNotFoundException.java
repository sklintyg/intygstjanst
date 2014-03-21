package se.inera.certificate.integration.module.exception;

public class ModuleNotFoundException extends Exception {

    private static final long serialVersionUID = 2806302183790886656L;

    public ModuleNotFoundException() {
        super();
    }

    public ModuleNotFoundException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ModuleNotFoundException(String arg0) {
        super(arg0);
    }

    public ModuleNotFoundException(Throwable arg0) {
        super(arg0);
    }
}
