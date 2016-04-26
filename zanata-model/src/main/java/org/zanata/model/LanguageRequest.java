package org.zanata.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
 * Entity for request to join language team.
 * See also {@link org.zanata.model.Request}
 *
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@Entity
@Access(AccessType.FIELD)
@NoArgsConstructor
public class LanguageRequest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @OneToOne
    @JoinColumn(name = "requestId", nullable = false)
    @Setter
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
}
