package org.zanata.servlet;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.servlet.config.Forward;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.HttpOperation;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Query;
import org.ocpsoft.rewrite.servlet.config.Redirect;
import org.ocpsoft.rewrite.servlet.config.rule.Join;
import org.ocpsoft.rewrite.servlet.http.event.HttpInboundServletRewrite;
import org.ocpsoft.rewrite.servlet.http.event.HttpServletRewrite;
import org.ocpsoft.urlbuilder.Address;
import org.ocpsoft.urlbuilder.AddressBuilder;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/*
 * This class replaces urlrewrite.xml, with simpler bidirectional mappings for external/internal URLs.
 */
@RewriteConfiguration
public class UrlRewriteConfig extends HttpConfigurationProvider {

    @Override
    public Configuration getConfiguration(final ServletContext context) {
        String contextPath = context.getContextPath();
        // NB: inbound rules are processed in order, outbound rules in reverse order (as of Rewrite 3.0.0.Alpha1)
        return ConfigurationBuilder.begin()

                // strip cid params to avoid NonexistentConversationException
                .addRule()
                .when(Query.parameterExists("cid"))
                .perform(new RedirectWithoutParam("cid"))

                .addRule()
                .when(Direction.isInbound()
                    .and(Path.matches("/seam/resource/restv1/{path}")))
                .perform(Forward.to("/rest/{path}"))
                .where("path").matches(".*")

                // a redirect would be nicer (preferably to a pretty url, not
                // to .xhtml), but make sure you handle parameters, eg for
                // /search.seam?query=foo
                .addRule(Join.pathNonBinding("/{path}.seam").to("{path}.xhtml"))
                .when(Direction.isInbound())
                .where("path").matches(".*")

                .addRule()
                .when(Direction.isInbound().and(Path.matches(
                    "/iteration/files/{projectSlug}/{iterationSlug}")))
                .perform(Redirect.permanent(contextPath +
                    "/iteration/view/{projectSlug}/{iterationSlug}/documents"))

                .addRule()
                .when(Direction.isInbound().and(Path.matches(
                    "/iteration/source_files/{projectSlug}/{iterationSlug}")))
                .perform(Redirect.permanent(contextPath +
                    "/iteration/view/{projectSlug}/{iterationSlug}/documents"))

                .addRule(Join.path("/{path}").to("/a/index.xhtml"))
                .where("path").matches(anyOf(
                        "explore",
                        "glossary",
                        "glossary/project/[^/]*",
                        "languages",
                        // There is a 302 redirect from profile to profile/
                        // I don't know why it does it, but it causes 403 Forbidden
                        // unless there is also a rewrite for profile/ here
                        "profile",
                        "profile/",
                        "profile/view/[^/]*"))

                .addRule(Join.path("/project/translate/{project}/v/{version}/{document}").to("/editor/index.xhtml"))
                .where("document").matches(".*")

                .addRule(Join.path("/").to("/public/home.xhtml"))
                .addRule(Join.path("/info").to("/public/info.xhtml"))
                .addRule(Join.path("/account/activate/{key}").to("/account/activate.xhtml"))
                .addRule(Join.path("/account/google_password_reset_request").to("/account/google_password_reset_request.xhtml"))
                .addRule(Join.path("/account/password_reset/{key}").to("/account/password_reset.xhtml"))
                .addRule(Join.path("/account/password_reset_request").to("/account/password_reset_request.xhtml"))
                .addRule(Join.path("/account/inactive").to("/account/inactive_account.xhtml"))
                .addRule(Join.path("/account/sign_in").to("/account/login.xhtml"))
                .addRule(Join.path("/account/register").to("/account/register.xhtml"))
                .addRule(Join.path("/account/sign_out").to("/account/logout.xhtml"))
                // open id return url
                .addRule(Join.path("/openid").to("/account/openid.xhtml"))
                .addRule(Join.path("/account/validate_email/{key}").to("/account/email_validation.xhtml"))
                .addRule(Join.path("/admin/").to("/admin/home.xhtml"))
                .addRule(Join.pathNonBinding("/admin/{page}").to("/admin/{page}.xhtml")).where("page").matches(".+")
                .addRule(Join.path("/dashboard/").to("/dashboard/home.xhtml"))

                .addRule(Join.path("/dashboard/{section}").to("/dashboard/home.xhtml"))
                .where("section").matches(".*")

                .addRule(Join.path("/error/").to("/error.xhtml"))
                .addRule(Join.pathNonBinding("/error/{path}").to("/error/{path}.xhtml"))
                .addRule(Join.path("/iteration/view/{projectSlug}/{iterationSlug}").to("/iteration/view.xhtml"))

                .addRule(Join.path("/iteration/view/{projectSlug}/{iterationSlug}/{section}").to("/iteration/view.xhtml"))
                .where("section").matches(".*")

                /* JSF serves zanata-assets with suffix of .xhtml only.
                   This is to make sure any reference to zanata-assets
                   without .xhtml can access the resource.
                   e.g. jars/assets/style.css forwards to
                   jars/assets/style.css.xhtml
                */
                .addRule(Join.pathNonBinding("/javax.faces.resource/jars/assets/{path}")
                        .to("/javax.faces.resource/jars/assets/{path}.xhtml"))
                .when(Direction.isInbound())
                .where("path").matches(".*(?<!.xhtml)")

                .addRule(Join.path("/language/view/{id}").to("/language/language.xhtml"))

                .addRule(Join.path("/language/view/{id}/{section}").to("/language/language.xhtml"))
                .where("section").matches(".*")



                .addRule(Join.path("/account/create_user").to("/account/create_user.xhtml"))
                .addRule(Join.path("/profile/merge_account").to("/profile/merge_account.xhtml"))

                .addRule(Join.path("/project/add_iteration/{projectSlug}").to("/project/add_iteration.xhtml"))
                .addRule(Join.path("/project/add_iteration/{projectSlug}/{copyFromVersionSlug}").to("/project/add_iteration.xhtml"))
                .addRule(Join.path("/project/create").to("/project/create_project.xhtml"))
                .addRule(Join.path("/project/list").to("/project/home.xhtml"))
                .addRule(Join.path("/project/view/{slug}").to("/project/project.xhtml"))

                .addRule(Join.path("/project/view/{slug}/{section}").to("/project/project.xhtml"))
                .where("section").matches(".*")

                // generate zanata.xml config
                .addRule(Join.path("/project/view/{projectSlug}/iter/{iterationSlug}/config").
                        to("/project/project.xhtml?actionMethod=project%2Fproject.xhtml%3AconfigurationAction.getData"))
                // TODO fix this
                .addRule(Join.path("/rest").to("/rest/index.xrd"))
                .addRule(Join.path("/search/{query}").to("/search.xhtml"))
                // Translation Memory
                .addRule(Join.path("/tm/").to("/tm/home.xhtml"))
                .addRule(Join.path("/tm/create").to("/tm/create.xhtml"))
                .addRule(Join.path("/version-group/create").to("/version-group/create_version_group.xhtml"))
                .addRule(Join.path("/version-group/list").to("/version-group/home.xhtml"))
                .addRule(Join.path("/version-group/view/{slug}").to("/version-group/version_group.xhtml"))

                .addRule(Join.path("/version-group/view/{slug}/{section}").to("/version-group/version_group.xhtml"))
                .where("section").matches(".*")

                .addRule(Join.path("/webtrans/Application.html").to("/webtrans/Application.xhtml")).when(Direction.isInbound())
                .addRule(Join.path("/webtrans/translate").to("/webtrans/Application.xhtml"))

                .addRule(Join.path("/404").to("/404.xhtml"))
                // OAuth authorization
                .addRule(Join.path("/oauth/").to("/oauth/home.xhtml"))
                ;
    }

    @Override
    public int priority() {
        return 0;
    }

    static class RedirectWithoutParam extends HttpOperation {
        private final String paramName;

        RedirectWithoutParam(String paramName) {
            this.paramName = paramName;
        }

        @Override
        public void performHttp(HttpServletRewrite event,
                EvaluationContext context) {
            // Remove param from address query
            Address address = event.getAddress();
            String query = address.getQuery();
            List<NameValuePair> nameValuePairs =
                    URLEncodedUtils.parse(query, UTF_8);
            nameValuePairs.removeIf(nvp -> nvp.getName().equals(paramName));

            String newAddress;
            if (nameValuePairs.isEmpty()) {
                newAddress = address.getPath();
            } else {
                newAddress = address.getPath() + "?" + URLEncodedUtils.format(nameValuePairs, UTF_8);
            }
            ((HttpInboundServletRewrite) event).redirectTemporary(AddressBuilder.create(newAddress).toString());
        }
    }

    /**
     * Return a regex string that will match any of the given paths.
     *
     * Paths may include regular expression parts, or just be simple strings.
     * All paths are just joined with the pipe character ("|").
     *
     * @param paths one or more path regular expressions to match against
     * @return a regular expression that will match any of the given path expressions
     */
    private static String anyOf(@NotNull String... paths) {
        if (paths.length == 0) {
            throw new RuntimeException("anyOf() called with no paths. Specify at least one path.");
        }

        return StringUtils.join(paths, "|");
    }
}
