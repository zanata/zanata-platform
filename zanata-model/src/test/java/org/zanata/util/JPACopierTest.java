package org.zanata.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class JPACopierTest {
    @Test
    public void testCopy() throws Exception {
        ParentClass original = constructTestData();
        ParentClass clone =
                JPACopier.<ParentClass> copyBean(original, "customIgnoreField");
        assertClone(original, clone);
    }

    private void assertClone(ParentClass original, ParentClass clone) {
        assertThat(clone.getString()).isEqualTo(original.getString());

        assertThat(clone.getCustomIgnoreField()).isNotEqualTo(
                original.getCustomIgnoreField());

        assertChildClass(original.getChild1(), clone.getChild1(), true);
        assertChildClass(original.getChild2(), clone.getChild2(), false);
        assertChildClass(original.getChild3(), clone.getChild3(), true);
    }

    private void assertChildClass(ChildClass original, ChildClass clone,
            boolean expectNewRef) {
        if (expectNewRef) {
            assertThat(clone).isNotSameAs(original);
            assertThat(clone.getId()).isNotEqualTo(original.getId());
            assertThat(clone.getTestList()).isNotSameAs(original.getTestList());
            assertThat(clone.getTestSet()).isNotSameAs(original.getTestSet());
            assertThat(clone.getTestMap()).isNotSameAs(original.getTestMap());
        } else {
            assertThat(clone.getId()).isEqualTo(original.getId());
            assertThat(clone.getTestList()).isSameAs(original.getTestList());
            assertThat(clone.getTestSet()).isSameAs(original.getTestSet());
            assertThat(clone.getTestMap()).isSameAs(original.getTestMap());
        }

        assertThat(clone.getTestString()).isEqualTo(original.getTestString());
        assertThat(clone.getTestList()).isEqualTo(original.getTestList());
        assertThat(clone.getTestSet()).isEqualTo(original.getTestSet());
        assertThat(clone.getTestMap()).isEqualTo(original.getTestMap());
    }

    private ParentClass constructTestData() {
        List<String> testList = Lists.newArrayList("list1", "list2", "list3");
        Set<String> testSet = Sets.newHashSet("set1", "set2", "set3");
        Map<String, String> testMap = Maps.newHashMap();
        testMap.put("One", "one");
        testMap.put("Two", "two");
        testMap.put("Three", "three");

        ChildClass child1 =
                new ChildClass(1L, "String1", testList, testSet, testMap);

        ChildClass child2 =
                new ChildClass(2L, "String2", testList, testSet, testMap);

        ChildClass child3 =
                new ChildClass(3L, "String3", testList, testSet, testMap);

        return new ParentClass("string", Boolean.TRUE, true, Byte.MIN_VALUE,
                Byte.MAX_VALUE, Character.MIN_VALUE,
                Character.MAX_VALUE, Double.MIN_VALUE,
                Double.MAX_VALUE, Float.MIN_VALUE, Float.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Long.MIN_VALUE,
                Long.MAX_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, 100,
                child1, child2, child3);
    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentClass {
        private String string;

        private Boolean wrapper_boolean;
        private boolean primitive_boolean;

        private Byte wrapper_byte;
        private byte primitive_byte;

        private Character wrapper_char;
        private char primitive_char;

        private Double wrapper_double;
        private double primitive_double;

        private Float wrapper_float;
        private float primitive_float;

        private Integer wrapper_int;
        private int primitive_int;

        private Long wrapper_long;
        private long primitive_long;

        private Short wrapper_short;
        private short primitive_short;

        // custom ignore field
        private int customIgnoreField;

        // expect to copy with new reference
        @OneToOne
        private ChildClass child1;

        // expect to copy with same reference
        @OneToMany
        private ChildClass child2;

        // expect to copy with new reference
        @OneToMany(mappedBy = "id")
        private ChildClass child3;

    }

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildClass {
        // common ignored field
        private Long id;

        private String testString;

        // expect to create new instance when copy
        private List<String> testList;

        // expect to create new instance when copy
        private Set<String> testSet;

        // expect to create new instance when copy
        private Map<String, String> testMap;
    }

}
