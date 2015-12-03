package se.inera.intyg.intygstjanst.web.exception;

public class SubsystemCallException extends ServerException {

    private static final long serialVersionUID = -2881966259299346838L;

    private final String subsystemId;

    public SubsystemCallException(String subsystemId) {
        super("Call to subsystem '" + subsystemId + "' caused an exception");
        this.subsystemId = subsystemId;
    }

    public SubsystemCallException(String subsystemId, String message) {
        super(message);
        this.subsystemId = subsystemId;
    }

    public SubsystemCallException(String subsystemId, Throwable cause) {
        super(cause);
        this.subsystemId = subsystemId;
    }

    public SubsystemCallException(String subsystemId, String message, Throwable cause) {
        super(message, cause);
        this.subsystemId = subsystemId;
    }

    public String getSubsystemId() {
        return subsystemId;
    }
}
