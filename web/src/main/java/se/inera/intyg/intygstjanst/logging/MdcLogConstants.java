package se.inera.intyg.intygstjanst.logging;

public class MdcLogConstants {

    private MdcLogConstants() {

    }

    public static final String EVENT_ACTION = "event.action";
    public static final String EVENT_CATEGORY = "event.category";
    public static final String EVENT_CATEGORY_PROCESS = "[process]";
    public static final String EVENT_TYPE = "event.type";
    public static final String EVENT_START = "event.start";
    public static final String EVENT_END = "event.end";
    public static final String EVENT_DURATION = "event.duration";
    public static final String EVENT_CERTIFICATE_ID = "event.certificate.id";
    public static final String EVENT_CERTIFICATE_TYPE = "event.certificate.type";
    public static final String EVENT_CERTIFICATE_CARE_UNIT_ID = "event.certificate.care_unit.id";
    public static final String EVENT_PART_ID = "event.part.id";
    public static final String EVENT_RECIPIENT = "event.recipient";
    public static final String SESSION_ID_KEY = "session.id";
    public static final String SPAN_ID_KEY = "span.id";
    public static final String TRACE_ID_KEY = "trace.id";
    public static final String USER_ID = "user.id";
}
