package org.zanata.client.commands;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
public class PutUserOptionsImpl extends ConfigurableOptionsImpl implements
        PutUserOptions {
    private String userName;
    private String userEmail;
    private String userUsername;
    private String userPasswordHash;
    private String userKey;
    private String userRoles;
    private String userLangs;
    private String userEnabled = "auto";

    private static final Logger log = LoggerFactory
            .getLogger(PutUserCommand.class);

    @Override
    public String getCommandName() {
        return "put-user";
    }

    @Override
    public String getCommandDescription() {
        return "Creates or updates a user. Unspecified options will not be updated.";
    }

    @Override
    public PutUserCommand initCommand() {
        return new PutUserCommand(this);
    }

    @Override
    @Option(name = "--user-name",
            usage = "Full name of the user (required for new user)")
    public void setUserName(String name) {
        this.userName = name;
    }

    @Override
    @Option(name = "--user-email",
            usage = "Email address of the user (required for new user)")
    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @Override
    @Option(name = "--user-username", required = true,
            usage = "Login/username of the user (required)")
    public void setUserUsername(String username) {
        this.userUsername = username;
    }

    @Override
    @Option(name = "--user-passwordhash",
            usage = "User password hash")
    public void setUserPasswordHash(String passwordHash) {
        this.userPasswordHash = passwordHash;
    }

    @Override
    @Option(name = "--user-key",
            usage = "User's api key (empty for none)")
    public void setUserKey(String userKey) {
        if (userKey == null || userKey.length() == 0)
            this.userKey = null;
        else
            this.userKey = userKey;
    }

    @Option(name = "--user-langs",
            usage = "Language teams for the user")
    @Override
    public void setUserLangs(String userLangs) {
        this.userLangs = userLangs;
    }

    @Override
    @Option(name = "--user-roles",
            usage = "Security roles for the user")
    public void setUserRoles(String roles) {
        this.userRoles = roles;
    }

    @Override
    @Option(name = "--user-enabled",
            usage = "Whether the account should be enabled or disabled (true/false)")
    public void setUserEnabled(String enabled) {
        String[] options = { "auto", "true", "false" };
        if (!Arrays.asList(options).contains(enabled.toLowerCase())) {
            throw new RuntimeException("--user-enabled requires true or false (or auto)");
        }
        this.userEnabled = enabled.toLowerCase();
    }

    @Override
    public String getUserUsername() {
        return userUsername;
    }

    @Override
    public String isUserEnabled() {
        return userEnabled;
    }

    @Override
    public String getUserLangs() {
        return userLangs;
    }

    @Override
    public String getUserKey() {
        return userKey;
    }

    @Override
    public String getUserPasswordHash() {
        return userPasswordHash;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public String getUserRoles() {
        return userRoles;
    }

}
