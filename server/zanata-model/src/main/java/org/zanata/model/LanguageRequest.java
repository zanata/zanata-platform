package org.zanata.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 * Entity for request to join language team. See also
 * {@link org.zanata.model.Request}
 *
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
public class LanguageRequest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    @OneToOne
    @JoinColumn(name = "requestId", nullable = false)
    private Request request;
    @ManyToOne
    @JoinColumn(name = "localeId", nullable = false)
    private HLocale locale;

    /**
     * Request as coordinator in team
     */
    private boolean coordinator;

    /**
     * Request as reviewer in team
     */
    private boolean reviewer;

    /**
     * Request as translator in team
     */
    private boolean translator;

    public LanguageRequest(Request request, HLocale locale, boolean coordinator,
            boolean reviewer, boolean translator) {
        this.request = request;
        this.locale = locale;
        this.coordinator = coordinator;
        this.reviewer = reviewer;
        this.translator = translator;
    }

    public Long getId() {
        return this.id;
    }

    public Request getRequest() {
        return this.request;
    }

    public HLocale getLocale() {
        return this.locale;
    }

    /**
     * Request as coordinator in team
     */
    public boolean isCoordinator() {
        return this.coordinator;
    }

    /**
     * Request as reviewer in team
     */
    public boolean isReviewer() {
        return this.reviewer;
    }

    /**
     * Request as translator in team
     */
    public boolean isTranslator() {
        return this.translator;
    }

    public LanguageRequest() {
    }

    public void setRequest(final Request request) {
        this.request = request;
    }
}
