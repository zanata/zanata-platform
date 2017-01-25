package org.zanata.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.zanata.client.commands.PutUserCommand;
import org.zanata.client.commands.PutUserOptions;

/**
 * Creates or updates a Zanata user.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Mojo(name = "put-user", requiresOnline = true, requiresProject = false)
public class PutUserMojo extends ConfigurableMojo<PutUserOptions> implements
        PutUserOptions {

    /**
     * Full name of the user
     */
    @Parameter(property = "zanata.userName")
    private String userName;

    /**
     * Email address of the user
     */
    @Parameter(property = "zanata.userEmail")
    private String userEmail;

    /**
     * Login/username of the user
     */
    @Parameter(property = "zanata.userUsername", required = true)
    private String userUsername;

    /**
     * User password hash
     */
    @Parameter(property = "zanata.userPasswordHash")
    private String userPasswordHash;

    /**
     * User's api key (empty for none)
     */
    @Parameter(property = "zanata.userKey")
    private String userKey;

    /**
     * Security roles for the user
     */
    @Parameter(property = "zanata.userRoles")
    private String userRoles;

    /**
     * Language teams for the user
     */
    @Parameter(property = "zanata.userLangs")
    private String userLangs;

    /**
     * Whether the account should be enabled
     */
    @Parameter(property = "zanata.userEnabled")
    private String userEnabled;

    public PutUserMojo() throws Exception {
        super();
    }

    public PutUserCommand initCommand() {
        return new PutUserCommand(this);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getUserPasswordHash() {
        return userPasswordHash;
    }

    public void setUserPasswordHash(String userPasswordHash) {
        this.userPasswordHash = userPasswordHash;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(String userRoles) {
        this.userRoles = userRoles;
    }

    public String getUserLangs() {
        return userLangs;
    }

    public void setUserLangs(String userLangs) {
        this.userLangs = userLangs;
    }

    public String isUserEnabled() {
        return userEnabled;
    }

    public void setUserEnabled(String userEnabled) {
        this.userEnabled = userEnabled.toLowerCase();
    }

    @Override
    public String getCommandName() {
        return "put-user";
    }
}
