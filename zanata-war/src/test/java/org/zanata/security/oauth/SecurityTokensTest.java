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

package org.zanata.security.oauth;

import org.assertj.core.api.Assertions;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.zanata.model.HAccount;

public class SecurityTokensTest {

    private SecurityTokens securityTokens;

    @Before
    public void setUp() throws Exception {
        securityTokens = new SecurityTokens(1L);
        securityTokens.setUp();
    }

    @Test
    public void canConvertToJson() throws Exception {
        securityTokens.getAuthorizationCode("admin", "client_id");
        HAccount account = new HAccount();
        account.setUsername("admin");
        securityTokens.generateAccessAndRefreshTokens(account);
        String fieldValuesAsJSON = securityTokens.getFieldValuesAsJSON();

        SecurityTokens.Tokens tokens = new ObjectMapper().reader(SecurityTokens.Tokens.class)
                .readValue(fieldValuesAsJSON);
        Assertions.assertThat(tokens.getAccessTokenToUsername()).containsValue("admin");
        Assertions.assertThat(tokens.getUsernameToClientIdAuthCodeMap()).containsKey("admin");
    }

}
