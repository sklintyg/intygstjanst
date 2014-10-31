package se.inera.certificate.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

public class MarkerFilter extends AbstractMatcherFilter<ILoggingEvent> {
    private List<Marker> markersToMatch = new ArrayList<>();
    @Override
    public void start() {
        if (!markersToMatch.isEmpty())
            super.start();
         else
            addError("!!! no marker yet !!!");
    }
    @Override
    public FilterReply decide(ILoggingEvent event) {
        Marker marker = event.getMarker();
        if (!isStarted())
            return FilterReply.NEUTRAL;
        if (null == marker)
            return onMismatch;
        for (Marker markerToMatch : markersToMatch) {
            if (markerToMatch.contains(marker))
                return onMatch;
        }
        return onMismatch;
    }
    
    public void setMarker(String markerStr) {
        if(null != markerStr && !markerStr.isEmpty()) {
            markersToMatch.clear();
            markersToMatch.add(MarkerFactory.getMarker(markerStr));
        }
    }

    public void setMarkers(String markersStr) {
        if(null != markersStr && !markersStr.isEmpty()) {
            markersToMatch.clear();
            StringTokenizer tokenizer = new StringTokenizer(markersStr, ",");
            while (tokenizer.hasMoreElements()) {
                String markerStr = tokenizer.nextToken().trim();
                markersToMatch.add(MarkerFactory.getMarker(markerStr));
            }
        }
    }
}
