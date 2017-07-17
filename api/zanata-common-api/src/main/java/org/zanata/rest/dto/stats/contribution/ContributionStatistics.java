package org.zanata.rest.dto.stats.contribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Contains information about translations contributed by a single user.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({ "username", "contributions" })
@Label("Contribution Statistics")
public class ContributionStatistics implements Serializable {

    private static final long serialVersionUID = -6328249224235406066L;
    private String username;

    private List<LocaleStatistics> contributions;

    public ContributionStatistics() {
    }

    public ContributionStatistics(String username,
            List<LocaleStatistics> contributions) {
        this.username = username;
        this.contributions = contributions;
    }

    /**
     * User name responsible for the contributions.
     */
    @JsonProperty("username")
    @DocumentationExample("bart")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("contributions")
    public List<LocaleStatistics> getContributions() {
        if (contributions == null) {
            contributions = new ArrayList<LocaleStatistics>();
        }
        return contributions;
    }

    public void setContributions(List<LocaleStatistics> contributions) {
        this.contributions = contributions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ContributionStatistics))
            return false;

        ContributionStatistics that = (ContributionStatistics) o;

        if (username != null ? !username.equals(that.username)
                : that.username != null)
            return false;
        return !(contributions != null
                ? !contributions.equals(that.contributions)
                : that.contributions != null);

    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result =
                31 * result +
                        (contributions != null ? contributions.hashCode() : 0);
        return result;
    }
}
