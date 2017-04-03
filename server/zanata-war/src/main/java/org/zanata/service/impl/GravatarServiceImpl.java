package org.zanata.service.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.zanata.ApplicationConfiguration;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.model.HAccount;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.GravatarService;
import org.zanata.util.HashUtil;

@Named("gravatarServiceImpl")
@RequestScoped
public class GravatarServiceImpl implements GravatarService {
    private static String GRAVATAR_URL = "//www.gravatar.com/avatar/";

    @Inject
    @Authenticated
    HAccount authenticatedAccount;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Override
    public String getUserImageUrl(int size) {
        String email = "";
        if (authenticatedAccount != null) {
            email =
                    this.authenticatedAccount.getPerson().getEmail()
                            .toLowerCase().trim();
        }
        return this.getUserImageUrl(size, email);
    }

    /**
     * Return a url representing the user's Gravatar image.
     * Query parameters:
     * d = default image
     * r = image rating
     * s = size
     * @param size pixel size of image
     * @param email identifier of user image
     * @return String url for user image
     */
    @Override
    public String getUserImageUrl(int size, String email) {
        return new StringBuilder(GRAVATAR_URL)
            .append(getGravatarHash(email))
            .append("?d=mm&r=").append(applicationConfiguration.getGravatarRating())
            .append("&s=")
            .append(size).toString();
    }

    @Override
    public String getGravatarHash(String email) {
       return HashUtil.md5Hex(email.toLowerCase().trim());
    }

}
