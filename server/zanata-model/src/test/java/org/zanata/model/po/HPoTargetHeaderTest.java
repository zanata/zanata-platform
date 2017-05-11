package org.zanata.model.po;

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import static org.assertj.core.api.Assertions.assertThat;

public class HPoTargetHeaderTest {

    @Test
    public void testEquals() {
        assertThat(new HPoTargetHeader().equals(new HPoTargetHeader())).isTrue();

        HDocument docA = new HDocument();
        docA.setName("DocA");
        docA.setId(123456789L);
        HDocument docB = new HDocument();
        docB.setName("DocB");
        docA.setId(987654321L);
        assertThat(docA.equals(docB)).isFalse();

        HPoTargetHeader alpha = new HPoTargetHeader();
        alpha.setTargetLanguage(new HLocale(new LocaleId("fr")));
        HPoTargetHeader bravo = new HPoTargetHeader();
        assertThat(alpha.equals(bravo)).isFalse();

        bravo.setTargetLanguage(new HLocale(new LocaleId("fr")));
        assertThat(alpha.equals(bravo)).isTrue();

        alpha.setDocument(docA);
        bravo.setDocument(docB);
        assertThat(alpha.equals(bravo)).isFalse();
        assertThat(alpha.getDocument()).isNotEqualTo(bravo.getDocument());
    }

    @Test
    public void testHashcode() {
        HPoTargetHeader alpha = new HPoTargetHeader();
        HPoTargetHeader bravo = new HPoTargetHeader();
        bravo.setId(987654321L);
        assertThat(alpha.hashCode()).isNotEqualTo(bravo.hashCode());
        alpha.setId(987654321L);
        assertThat(alpha.hashCode()).isEqualTo(bravo.hashCode());
    }
}
