package se.inera.certificate.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

/**
 * Definition of the common domain model for a 'utlåtande'. This model allows reading of all common fields. Setting the
 * model must be done through subclasses. All modules share this common base which is extendible such that:
 * <ul>
 * <li>New fields can be added to the model.
 * <li>The entities {@link Patient}, {@link HosPersonal}, {@link Aktivitet}, {@link Observation}, {@link Vardkontakt},
 * {@link Rekommendation} and {@link Referens} can be extended with sub classes of themself.
 * </ul>
 * <p>
 * Observe that the methods {@link #getAktiviteter()}, {@link #getObservationer()}, {@link #getVardkontakter()},
 * {@link #getRekommendationer()} and {@link #getReferenser()} are stubbed to return an empty immutable list of
 * entities. If any of these entities are to be used in a module, these methods have to be overridden.
 * <p>
 * The {@link #getPatient()} and {@link #getSkapadAv()} are abstract since they need an implementation (should never
 * return null). These must be implemented to return the correct subclass by the modules.
 */
public abstract class Utlatande {

    private Id id;

    private Kod typ;

    private List<String> kommentarer;

    private LocalDateTime signeringsdatum;

    private LocalDateTime skickatdatum;

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

    /**
     * Returns the patient of this utlatande.
     * <p>
     * The implementing class is free to chose a sub type of {@link Patient} as return value. A <code>setPatient</code>
     * method should also be created by the implementing class.
     * 
     * @return The patient of this utlatande.
     */
    public abstract Patient getPatient();

    /**
     * Returns the hos-personal of this utlatande.
     * <p>
     * The implementing class is free to chose a sub type of {@link HosPersonal} as return value. A
     * <code>setSkapadAv</code> method should also be created by the implementing class.
     * 
     * @return The hos-personal of this utlatande.
     */
    public abstract HosPersonal getSkapadAv();

    /**
     * Returns the list of aktiviteter for this utlatande.
     * <p>
     * Note that this implementation only returns an immutable empty list of {@link Aktivitet}er. Subclasses which
     * override this method should do this by concretise the return type like:
     * <p>
     * <code>List&lt;Aktivitet></code><br>
     * or<br>
     * <code>List&lt;SubclassOfAktivitet></code>
     * 
     * @return A list of {@link Aktivitet}er.
     */
    public List<? extends Aktivitet> getAktiviteter() {
        return Collections.emptyList();
    }

    /**
     * Returns the list of observationer for this utlatande.
     * <p>
     * Note that this implementation only returns an immutable empty list of {@link Observation}er. Subclasses which
     * override this method should do this by concretise the return type like:
     * <p>
     * <code>List&lt;Observation></code><br>
     * or<br>
     * <code>List&lt;SubclassOfObservation></code>
     * 
     * @return A list of {@link Observation}er.
     */
    public List<? extends Observation> getObservationer() {
        return Collections.emptyList();
    }

    /**
     * Returns the list of vardkontakter for this utlatande.
     * <p>
     * Note that this implementation only returns an immutable empty list of {@link Vardkontakt}er. Subclasses which
     * override this method should do this by concretise the return type like:
     * <p>
     * <code>List&lt;Vardkontakt></code><br>
     * or<br>
     * <code>List&lt;SubclassOfVardkontakt></code>
     * 
     * @return A list of {@link Vardkontakt}er.
     */
    public List<? extends Vardkontakt> getVardkontakter() {
        return Collections.emptyList();
    }

    /**
     * Returns the list of rekommendationer for this utlatande.
     * <p>
     * Note that this implementation only returns an immutable empty list of {@link Rekommendation}er. Subclasses which
     * override this method should do this by concretise the return type like:
     * <p>
     * <code>List&lt;Rekommendation></code><br>
     * or<br>
     * <code>List&lt;SubclassOfRekommendation></code>
     * 
     * @return A list of {@link Rekommendation}er.
     */
    public List<? extends Rekommendation> getRekommendationer() {
        return Collections.emptyList();
    }

    /**
     * Returns the list of referenser for this utlatande.
     * <p>
     * Note that this implementation only returns an immutable empty list of {@link Referens}er. Subclasses which
     * override this method should do this by concretise the return type like:
     * <p>
     * <code>List&lt;Referens></code><br>
     * or<br>
     * <code>List&lt;SubclassOfReferens></code>
     * 
     * @return A list of {@link Referens}er.
     */
    public List<? extends Referens> getReferenser() {
        return Collections.emptyList();
    }

    /**
     * Subclasses need to decide their respective validToDate, should return null if not applicable.
     * 
     * @return {@link LocalDate}, or null if not applicable
     */
    public abstract LocalDate getValidToDate();

    /**
     * Subclasses need to decide their respective validFromDate, should return null if not applicable.
     * 
     * @return {@link LocalDate}, or null if not applicable
     */
    public abstract LocalDate getValidFromDate();
}
