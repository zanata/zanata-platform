package org.zanata.rest.oauth;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.zanata.security.annotations.NoSecurityCheck;

public class TokenServiceTest {

    @Test
    public void hasAnnotationToByPassRESTSecurityCheck() {

        NoSecurityCheck noSecurityCheck =
                TokenService.class.getDeclaredAnnotation(NoSecurityCheck.class);

        Assertions.assertThat(noSecurityCheck).isNotNull();
    }

}
