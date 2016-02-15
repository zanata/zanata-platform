package org.zanata.servlet;

import org.ocpsoft.rewrite.annotation.RewriteConfiguration;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Direction;
import org.ocpsoft.rewrite.servlet.config.Forward;
import org.ocpsoft.rewrite.servlet.config.HttpConfigurationProvider;
import org.ocpsoft.rewrite.servlet.config.Path;
import org.ocpsoft.rewrite.servlet.config.Redirect;
import org.ocpsoft.rewrite.servlet.config.rule.Join;

import javax.servlet.ServletContext;

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

                // strip trailling slash as recommended:
                // https://github.com/ocpsoft/rewrite/issues/158#issuecomment-154597796
                .addRule()
                .when(Path.matches("/{path}/"))
                .perform(Redirect.permanent(contextPath + "/{path}"))
                .where("path").matches(".*")

                // TODO test this
                .addRule()
                .when(Direction.isInbound().and(Path.matches("/seam/resource/restv1/{path}")))
                .perform(Forward.to("/rest/{path}"))
                .where("path").matches(".*")

                .addRule()
                .when(Direction.isInbound().and(Path.matches("/iteration/files/{projectSlug}/{iterationSlug}")))
                .perform(Redirect.permanent(contextPath + "/iteration/view/{projectSlug}/{iterationSlug}/documents"))

                .addRule()
                .when(Direction.isInbound().and(Path.matches("/iteration/source_files/{projectSlug}/{iterationSlug}")))
                .perform(Redirect.permanent(contextPath + "/iteration/view/{projectSlug}/{iterationSlug}/documents"))


                .addRule(Join.path("/").to("/home.seam"))
                .addRule(Join.path("/account/activate/{key}").to("/account/activate.seam"))
                //.addRule(Join.path("/account/changepassword").to("/account/changepassword.seam"))
                .addRule(Join.path("/account/google_password_reset_request").to("/account/google_password_reset_request.seam"))
                .addRule(Join.path("/account/password_reset/{key}").to("/account/password_reset.seam"))
                .addRule(Join.path("/account/password_reset_request").to("/account/password_reset_request.seam"))
                .addRule(Join.path("/account/inactive").to("/account/inactive_account.seam"))
                .addRule(Join.path("/account/klogin").to("/account/klogin.seam"))
                .addRule(Join.path("/account/sign_in").to("/account/login.seam"))
                .addRule(Join.path("/account/register").to("/account/register.seam"))
                .addRule(Join.path("/account/sign_out").to("/account/logout.seam"))
                .addRule(Join.path("/account/validate_email/{key}").to("/account/email_validation.seam"))
                .addRule(Join.path("/admin").to("/admin/home.seam"))
                .addRule(Join.pathNonBinding("/admin/{page}").to("/admin/{page}.seam"))
                .addRule(Join.path("/dashboard").to("/dashboard/home.seam"))
                .addRule(Join.path("/error").to("/error.seam"))
                .addRule(Join.pathNonBinding("/error/{path}").to("/error/{path}.seam"))
                .addRule(Join.path("/glossary").to("/glossary/view.seam"))
                //.addRule(Join.path("/help/view").to("/help/view.seam"))
                .addRule(Join.path("/iteration/view/{projectSlug}/{iterationSlug}").to("/iteration/view.seam"))
                .addRule(Join.path("/iteration/view/{projectSlug}/{iterationSlug}/{section}").to("/iteration/view.seam"))
                .when(Direction.isInbound())
                .where("section").matches(".*")

                /* JSF serves zanata-assets with suffix of .seam only.
                   This is to make sure any reference to zanata-assets
                   without .seam can access the resource.
                   e.g. jars/assets/style.css forwards to
                   jars/assets/style.css.seam
                */
                .addRule(Join.path("/javax.faces.resource/jars/assets/{path}")
                        .to("/javax.faces.resource/jars/assets/{path}.seam"))
                .when(Direction.isInbound())
                .where("path").matches(".*(?<!.seam)")

                .addRule(Join.path("/language/list").to("/language/home.seam"))
                .addRule(Join.path("/language/view/{id}").to("/language/language.seam"))
                .addRule(Join.path("/profile").to("/profile/home.seam"))
                .addRule(Join.path("/profile/add_identity").to("/profile/add_identity.seam"))
                .addRule(Join.path("/profile/create").to("/profile/create_user.seam"))
                .addRule(Join.path("/profile/edit").to("/profile/edit.seam"))
                .addRule(Join.path("/profile/merge_account").to("/profile/merge_account.seam"))
                .addRule(Join.path("/profile/view/{username}").to("/profile/home.seam"))
                .addRule(Join.path("/project/add_iteration/{projectSlug}").to("/project/add_iteration.seam"))
                .addRule(Join.path("/project/add_iteration/{projectSlug}/{copyFromVersionSlug}").to("/project/add_iteration.seam"))
                .addRule(Join.path("/project/create").to("/project/create_project.seam"))
                .addRule(Join.path("/project/list").to("/project/home.seam"))
                .addRule(Join.path("/project/view/{slug}").to("/project/project.seam"))
                .addRule(Join.path("/project/view/{slug}/{section}").to("/project/project.seam"))
                .when(Direction.isInbound())
                .where("section").matches(".*")

                // generate zanata.xml config
                .addRule(Join.path("/project/view/{projectSlug}/iter/{iterationSlug}/config").
                        to("/project/project.seam?actionMethod=project%2Fproject.xhtml%3AconfigurationAction.getData"))
                // TODO fix this
                .addRule(Join.path("/rest").to("/rest/index.xrd"))
                .addRule(Join.path("/search/{query}").to("/search.seam"))
                // Translation Memory
                .addRule(Join.path("/tm").to("/tm/home.seam"))
                .addRule(Join.path("/tm/create").to("/tm/create.seam"))
                .addRule(Join.path("/version-group/create").to("/version-group/create_version_group.seam"))
                .addRule(Join.path("/version-group/list").to("/version-group/home.seam"))
                .addRule(Join.path("/version-group/view/{versionGroupSlug}").to("/version-group/version_group.seam"))
                .addRule(Join.path("/webtrans/Application.html").to("/webtrans/Application.seam")).when(Direction.isInbound())
                .addRule(Join.path("/webtrans/translate").to("/webtrans/Application.seam"))
                ;
    }

    @Override
    public int priority() {
        return 0;
    }
}
