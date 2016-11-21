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
    @Parameter(property = "zanata.userName", required = true)
    private String userName;

    /**
     * Email address of the user
     */
    @Parameter(property = "zanata.userEmail", required = true)
    private String userEmail;

    /**
     * Login/username of the user
     */
    @Parameter(property = "zanata.userUsername", required = true)
    private String userUsername;

    /**
     * User password hash
     */
    @Parameter(property = "zanata.userPasswordHash", required = true)
    private String userPasswordHash;

    /**
     * User's api key (empty for none)
     */
    @Parameter(property = "zanata.userKey", required = true)
    private String userKey;

    /**
     * Security roles for the user
     */
    @Parameter(property = "zanata.userRoles", required = true)
    private String userRoles;

    /**
     * Language teams for the user
     */
    @Parameter(property = "zanata.userLangs", required = true)
    private String userLangs;

    /**
     * Whether the account should be disabled
     */
    @Parameter(property = "zanata.userDisabled", required = true)
    private boolean userDisabled;

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

    public boolean isUserDisabled() {
        return userDisabled;
    }

    public void setUserDisabled(boolean userDisabled) {
        this.userDisabled = userDisabled;
    }

    @Override
    public String getCommandName() {
        return "put-user";
    }
}
