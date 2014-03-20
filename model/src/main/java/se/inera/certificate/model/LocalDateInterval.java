package se.inera.certificate.model;

import org.joda.time.LocalDate;

/**
 * @author andreaskaltenbach
 */
public final class LocalDateInterval {

    private LocalDate from;
    private LocalDate tom;

    public LocalDateInterval() {
    }

    public LocalDateInterval(LocalDate from, LocalDate tom) {
        this.from = from;
        this.tom = tom;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTom() {
        return tom;
    }

    public void setTom(LocalDate tom) {
        this.tom = tom;
    }
}
