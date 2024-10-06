package se.inera.intyg.intygstjanst.logging;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.CharBuffer;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class MdcHelper {

    protected static final String LOG_TRACE_ID_HEADER = "x-trace-id";
    protected static final String LOG_SESSION_ID_HEADER = "x-session-id";
    private static final int LENGTH_LIMIT = 8;
    private static final char[] BASE62CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public String sessionId(HttpServletRequest http) {
        return Optional.ofNullable(
                http.getHeader(LOG_SESSION_ID_HEADER)
            )
            .orElse("-");
    }

    public String traceId(HttpServletRequest http) {
        return Optional.ofNullable(
                http.getHeader(LOG_TRACE_ID_HEADER)
            )
            .orElse(generateId());
    }

    public String spanId() {
        return generateId();
    }

    private String generateId() {
        final CharBuffer charBuffer = CharBuffer.allocate(LENGTH_LIMIT);
        IntStream.generate(() -> ThreadLocalRandom.current().nextInt(BASE62CHARS.length))
            .limit(LENGTH_LIMIT)
            .forEach(value -> charBuffer.append(BASE62CHARS[value]));
        return charBuffer.rewind().toString();
    }
}
