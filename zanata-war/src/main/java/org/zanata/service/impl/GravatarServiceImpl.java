package org.zanata.service.impl;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.model.HAccount;
import org.zanata.service.GravatarService;
import org.zanata.util.HashUtil;

@Name("gravatarServiceImpl")
@Scope(ScopeType.STATELESS)
public class GravatarServiceImpl implements GravatarService {
    private static String GRAVATAR_URL = "http://www.gravatar.com/avatar/";

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
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
        url.append(HashUtil.md5Hex(email.toLowerCase().trim()));
        url.append("?d=mm&r=g&s="); // d = default image, r = rating, s = size
        url.append(size);
        return url.toString();
    }

}
