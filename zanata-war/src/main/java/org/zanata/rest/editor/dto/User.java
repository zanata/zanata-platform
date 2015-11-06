package org.zanata.rest.editor.dto;

import java.io.IOException;
import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "username", "email", "name", "gravatarHash", "imageUrl", "languageTeams", "loggedIn" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User implements Serializable {

    @NotEmpty
    private String username;

    @Email
    @NotNull
    private String email;

    @NotEmpty
    private String name;

    /**
     * Use image url for user image path
     */
    @Deprecated
    private String gravatarHash;

    @NotEmpty
    private String imageUrl;

    private String languageTeams;

    private boolean loggedIn;

    @JsonIgnore
    public String getJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException e) {
            return this.getClass().getName() + "@"
                + Integer.toHexString(this.hashCode());
        }
    }
}
