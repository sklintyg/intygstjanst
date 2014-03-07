package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * @author andreaskaltenbach
 */
public class Utlatande {

    private Id id;

    private Kod typ;

    private List<String> kommentarer;

    private LocalDateTime signeringsdatum;

    private LocalDateTime skickatdatum;

    private Patient patient;

    private HosPersonal skapadAv;

    private List<Aktivitet> aktiviteter;

    private List<Observation> observationer;

    private List<Vardkontakt> vardkontakter;

    private List<Rekommendation> rekommendationer;

    private List<Referens> referenser;

    /**
     * To which point in time is this certificate considered valid. Modules implementing this model should use their own
     * getters calculating the date suitable for the certificate type and rules.
     */
    private LocalDate validToDate;

    /**
     * From which point in time is this certificate considered valid. Modules implementing this model should use their
     * own getters calculating the date suitable for the certificate type and rules.
     */
    private LocalDate validFromDate;

    public final Id getId() {
        return id;
    }

    public final void setId(Id id) {
        this.id = id;
    }

    public final Kod getTyp() {
        return typ;
    }

    public final void setTyp(Kod typ) {
        this.typ = typ;
    }

    public final List<String> getKommentarer() {
        if (kommentarer == null) {
            kommentarer = new ArrayList<>();
        }
        return kommentarer;
    }

    public final LocalDateTime getSigneringsdatum() {
        return signeringsdatum;
    }

    public final void setSigneringsdatum(LocalDateTime signeringsdatum) {
        this.signeringsdatum = signeringsdatum;
    }

    public final LocalDateTime getSkickatdatum() {
        return skickatdatum;
    }

    public final void setSkickatdatum(LocalDateTime skickatdatum) {
        this.skickatdatum = skickatdatum;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public HosPersonal getSkapadAv() {
        return skapadAv;
    }

    public void setSkapadAv(HosPersonal skapadAv) {
        this.skapadAv = skapadAv;
    }

    public List<? extends Aktivitet> getAktiviteter() {
        if (aktiviteter == null) {
            aktiviteter = new ArrayList<>();
        }
        return aktiviteter;
    }

    public List<? extends Observation> getObservationer() {
        if (observationer == null) {
            observationer = new ArrayList<>();
        }
        return observationer;
    }

    public List<? extends Vardkontakt> getVardkontakter() {
        if (vardkontakter == null) {
            vardkontakter = new ArrayList<>();
        }
        return vardkontakter;
    }

    public List<? extends Rekommendation> getRekommendationer() {
        if (rekommendationer == null) {
            rekommendationer = new ArrayList<>();
        }
        return rekommendationer;
    }

    public List<? extends Referens> getReferenser() {
        if (referenser == null) {
            referenser = new ArrayList<>();
        }
        return referenser;
    }

    public final LocalDate getValidToDate() {
        return validToDate;
    }

    public final void setValidToDate(LocalDate date) {
        validToDate = date;
    }

    public final LocalDate getValidFromDate() {
        return validFromDate;
    }

    public final void setValidFromDate(LocalDate date) {
        validFromDate = date;
    }
}
