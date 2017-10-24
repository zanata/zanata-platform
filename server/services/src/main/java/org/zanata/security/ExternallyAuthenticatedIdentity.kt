package org.zanata.security

interface ExternallyAuthenticatedIdentity {
    /**
     * This will change the state of ZanataIdentity to be in pre-authenticated
     * state. Client code should then check if the user is active and enabled.
     * If yes, calling the login method will complete the authentication process.
     */
    fun authenticate()

    /**
     * Accept the externally authenticated principal/subject and complete the
     * authentication process.
     */
    fun login()
}
