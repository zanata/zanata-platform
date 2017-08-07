/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.action;

import org.apache.commons.lang.StringUtils;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleMemberDAO;
import org.zanata.email.ContactLanguageTeamMembersEmailStrategy;
import org.zanata.email.EmailStrategy;
import org.zanata.i18n.Messages;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.security.annotations.Authenticated;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.service.EmailService;
import org.zanata.service.LocaleService;
import org.zanata.ui.faces.FacesMessages;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("contactLanguageTeamMembersAction")
@javax.faces.bean.ViewScoped
public class ContactLanguageTeamMembersAction implements Serializable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(ContactLanguageTeamMembersAction.class);

    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    private EmailService emailServiceImpl;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    private Messages msgs;
    @Inject
    private ApplicationConfiguration applicationConfiguration;
    @Inject
    private LocaleMemberDAO localeMemberDAO;
    private String message;
    private String subject;
    private String localeId;
    private HLocale locale;

    public List<HLocaleMember> getMembers() {
        if (StringUtils.isBlank(localeId)) {
            return Lists.newArrayList();
        }
        if (authenticatedAccount == null) {
            return localeMemberDAO.findAllActiveMembers(new LocaleId(localeId));
        }
        return localeMemberDAO.findActiveMembers(new LocaleId(localeId),
                authenticatedAccount.getPerson());
    }

    @CheckLoggedIn
    public void send() {
        List<HLocaleMember> members = getMembers();
        if (!members.isEmpty()) {
            String fromLoginName = authenticatedAccount.getUsername();
            String contactCoordinatorLink =
                    applicationConfiguration.getServerPath() + "/language/view/"
                            + localeId;
            String localeNativeName = getLocale().retrieveNativeName();
            LocaleId localeId = getLocale().getLocaleId();
            EmailStrategy strategy =
                    new ContactLanguageTeamMembersEmailStrategy(fromLoginName,
                            getSubject(), localeId.getId(), localeNativeName,
                            message, contactCoordinatorLink);
            try {
                String msg = emailServiceImpl
                        .sendToLanguageTeamMembers(localeId, strategy, members);
                facesMessages.addGlobal(msg);
            } catch (Exception e) {
                String subject = strategy.getSubject(msgs);
                StringBuilder sb = new StringBuilder()
                        .append("Failed to send email with subject \'")
                        .append(strategy.getSubject(msgs))
                        .append("\' , message \'").append(message).append("\'");
                log.error(
                        "Failed to send email: fromLoginName \'{}\', subject \'{}\', message \'{}\'. {}",
                        fromLoginName, subject, message, e);
                facesMessages.addGlobal(sb.toString());
            } finally {
                message = null;
                subject = null;
            }
        }
    }

    public HLocale getLocale() {
        if (locale == null) {
            locale = localeServiceImpl.getByLocaleId(new LocaleId(localeId));
        }
        return locale;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getLocaleId() {
        return this.localeId;
    }

    public void setLocaleId(final String localeId) {
        this.localeId = localeId;
    }
}
