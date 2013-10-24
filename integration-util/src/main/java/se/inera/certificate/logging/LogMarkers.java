package se.inera.certificate.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class LogMarkers {

    private LogMarkers() {}
    
    public static Marker VALIDATION = MarkerFactory.getMarker("Validation");
}
