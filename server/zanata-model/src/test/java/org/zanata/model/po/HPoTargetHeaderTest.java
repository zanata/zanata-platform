package org.zanata.model.po;

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.ModelEntityBase;

import java.lang.reflect.Method;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class HPoTargetHeaderTest {

    @Test
    public void testEquals() {
        Date now = new Date();
        HPoTargetHeader header = new HPoTargetHeader();
        header.setCreationDate(now);
        header.setLastChanged(now);
        HPoTargetHeader other = new HPoTargetHeader();
        other.setCreationDate(now);
        other.setLastChanged(now);
        assertThat(header.equals(other)).isTrue();

        HDocument docA = new HDocument();
        docA.setName("DocA");
        setId(docA, 123456789L);
        docA.setCreationDate(now);
        docA.setLastChanged(now);
        HDocument docB = new HDocument();
        docB.setName("DocB");
        setId(docB, 987654321L);
        docB.setCreationDate(now);
        docB.setLastChanged(now);
        assertThat(docA.equals(docB)).isFalse();

        HPoTargetHeader alpha = new HPoTargetHeader();
        alpha.setTargetLanguage(new HLocale(new LocaleId("fr")));
        alpha.setCreationDate(now);
        alpha.setLastChanged(now);
        HPoTargetHeader bravo = new HPoTargetHeader();
        bravo.setCreationDate(now);
        bravo.setLastChanged(now);
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
        Date now = new Date();
        HPoTargetHeader alpha = new HPoTargetHeader();
        alpha.setCreationDate(now);
        alpha.setLastChanged(now);
        HPoTargetHeader bravo = new HPoTargetHeader();
        bravo.setCreationDate(now);
        bravo.setLastChanged(now);
        setId(bravo, 987654321L);
        assertThat(alpha.hashCode()).isNotEqualTo(bravo.hashCode());
        setId(alpha, 987654321L);
        assertThat(alpha.hashCode()).isEqualTo(bravo.hashCode());
    }

    // Better to duplicate this method (EntityTestData.setId) than to add
    // another module (or add this to zanata-model's public API!)
    @SuppressWarnings("all")
    private static void setId(ModelEntityBase entity, Long id) {
        try {
            Method setIdMethod = ModelEntityBase.class
                    .getDeclaredMethod("setId", Long.class);
            setIdMethod.setAccessible(true);
            setIdMethod.invoke(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
