/**
 * This is the documentation for the Zanata REST API. It includes detailed
 * information about the resources, content, response codes, etc available
 * to external users.
 *
 * <h3>Authentication</h3>
 *
 * Some of the available resources require authentication and will respond with
 * an UNAUTHORIZED (401) code if not available. To prevent this, you must supply
 * credentials in the form of the "X-Auth-User" header, which should contain a
 * valid Zanata user name, and "X-Auth-Token", which should contain the
 * corresponding API key for the user. Additionally, the service might continue
 * to respond with the same code if the authenticated user does not have
 * the right permissions to access the resource.
 *
 */
package org.zanata.rest.service;
