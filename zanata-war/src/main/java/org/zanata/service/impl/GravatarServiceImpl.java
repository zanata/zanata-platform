package org.zanata.service.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
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

    @Override
    public String getUserImageUrl(int size, String email) {
        StringBuilder url = new StringBuilder(GRAVATAR_URL);
        url.append(getGravatarHash(email));
        url.append("?d=mm&r=g&s="); // d = default image, r = rating, s = size
        url.append(size);
        return url.toString();
    }

    @Override
    public String getGravatarHash(String email) {
       return HashUtil.md5Hex(email.toLowerCase().trim());
    }

}
