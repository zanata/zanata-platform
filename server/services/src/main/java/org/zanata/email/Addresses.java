/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.email;

import static com.google.common.base.Charsets.UTF_8;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.InternetAddress;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;

/**
 * Helper methods for working with JavaMail addresses.
 *
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class Addresses {
    private static final String UTF8 = UTF_8.name();

    public static InternetAddress getAddress(String email, String name) {
        try {
            return new InternetAddress(email, name, UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static InternetAddress getAddress(HPerson person) {
        return getAddress(person.getEmail(), person.getName());
    }

    public static InternetAddress[] getAddresses(List<HPerson> personList) {
        List<InternetAddress> toAddresses = new ArrayList<InternetAddress>();
        for (HPerson coord : personList) {
            toAddresses.add(getAddress(coord.getEmail(), coord.getName()));
        }
        return toAddresses.toArray(new InternetAddress[toAddresses.size()]);
    }

    public static InternetAddress[]
            getLocaleMemberAddresses(List<HLocaleMember> members) {
        List<InternetAddress> toAddresses = new ArrayList<InternetAddress>();
        for (HLocaleMember member : members) {
            toAddresses.add(getAddress(member.getPerson().getEmail(),
                    member.getPerson().getName()));
        }
        return toAddresses.toArray(new InternetAddress[toAddresses.size()]);
    }

    public static InternetAddress[] getAddresses(List<String> emailList,
            String name) {
        List<InternetAddress> toAddresses = new ArrayList<InternetAddress>();
        for (String email : emailList) {
            toAddresses.add(getAddress(email, name));
        }
        return toAddresses.toArray(new InternetAddress[toAddresses.size()]);
    }

    public static InternetAddress[] getReplyTo(String email, String name) {
        return new InternetAddress[] { getAddress(email, name) };
    }

    private Addresses() {
    }
}
