package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.common.LocaleId;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "username", "email", "name", "imageUrl", "languageTeams"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class User implements Serializable {

    private String username;
    private String email;
    private String name;
    private String imageUrl;
    private List<LocaleId> languageTeams;

    public User() {
        this(null, null, null, null, null);
    }

    public User(String username, String email, String name,
        String imageUrl, List<LocaleId> languageTeams) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
        this.languageTeams = languageTeams;
    }

    @NotEmpty
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Email
    @NotNull
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotEmpty
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty
    @JsonProperty("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JsonProperty("languageTeams")
    public List<LocaleId> getLanguageTeams() {
        return languageTeams;
    }

    public void setLanguageTeams(List<LocaleId> languageTeams) {
        this.languageTeams = languageTeams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (username != null ? !username.equals(user.username) :
            user.username != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null)
            return false;
        if (name != null ? !name.equals(user.name) : user.name != null)
            return false;
        if (imageUrl != null ? !imageUrl.equals(user.imageUrl) :
            user.imageUrl != null) return false;
        return !(languageTeams != null ?
            !languageTeams.equals(user.languageTeams) :
            user.languageTeams != null);

    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result =
            31 * result +
                (languageTeams != null ? languageTeams.hashCode() : 0);
        return result;
    }
}
