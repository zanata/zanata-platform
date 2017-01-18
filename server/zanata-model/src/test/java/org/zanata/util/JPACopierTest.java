package org.zanata.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Test;

import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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

        @java.beans.ConstructorProperties({ "string", "wrapper_boolean",
                "primitive_boolean", "wrapper_byte", "primitive_byte",
                "wrapper_char", "primitive_char", "wrapper_double",
                "primitive_double", "wrapper_float", "primitive_float",
                "wrapper_int", "primitive_int", "wrapper_long",
                "primitive_long",
                "wrapper_short", "primitive_short", "customIgnoreField",
                "child1",
                "child2", "child3" })
        public ParentClass(String string, Boolean wrapper_boolean,
                boolean primitive_boolean, Byte wrapper_byte,
                byte primitive_byte,
                Character wrapper_char, char primitive_char,
                Double wrapper_double,
                double primitive_double, Float wrapper_float,
                float primitive_float,
                Integer wrapper_int, int primitive_int, Long wrapper_long,
                long primitive_long, Short wrapper_short, short primitive_short,
                int customIgnoreField, ChildClass child1,
                ChildClass child2, ChildClass child3) {
            this.string = string;
            this.wrapper_boolean = wrapper_boolean;
            this.primitive_boolean = primitive_boolean;
            this.wrapper_byte = wrapper_byte;
            this.primitive_byte = primitive_byte;
            this.wrapper_char = wrapper_char;
            this.primitive_char = primitive_char;
            this.wrapper_double = wrapper_double;
            this.primitive_double = primitive_double;
            this.wrapper_float = wrapper_float;
            this.primitive_float = primitive_float;
            this.wrapper_int = wrapper_int;
            this.primitive_int = primitive_int;
            this.wrapper_long = wrapper_long;
            this.primitive_long = primitive_long;
            this.wrapper_short = wrapper_short;
            this.primitive_short = primitive_short;
            this.customIgnoreField = customIgnoreField;
            this.child1 = child1;
            this.child2 = child2;
            this.child3 = child3;
        }

        public ParentClass() {
        }

        public String getString() {
            return this.string;
        }

        public Boolean getWrapper_boolean() {
            return this.wrapper_boolean;
        }

        public boolean isPrimitive_boolean() {
            return this.primitive_boolean;
        }

        public Byte getWrapper_byte() {
            return this.wrapper_byte;
        }

        public byte getPrimitive_byte() {
            return this.primitive_byte;
        }

        public Character getWrapper_char() {
            return this.wrapper_char;
        }

        public char getPrimitive_char() {
            return this.primitive_char;
        }

        public Double getWrapper_double() {
            return this.wrapper_double;
        }

        public double getPrimitive_double() {
            return this.primitive_double;
        }

        public Float getWrapper_float() {
            return this.wrapper_float;
        }

        public float getPrimitive_float() {
            return this.primitive_float;
        }

        public Integer getWrapper_int() {
            return this.wrapper_int;
        }

        public int getPrimitive_int() {
            return this.primitive_int;
        }

        public Long getWrapper_long() {
            return this.wrapper_long;
        }

        public long getPrimitive_long() {
            return this.primitive_long;
        }

        public Short getWrapper_short() {
            return this.wrapper_short;
        }

        public short getPrimitive_short() {
            return this.primitive_short;
        }

        public int getCustomIgnoreField() {
            return this.customIgnoreField;
        }

        public ChildClass getChild1() {
            return this.child1;
        }

        public ChildClass getChild2() {
            return this.child2;
        }

        public ChildClass getChild3() {
            return this.child3;
        }

        public void setString(String string) {
            this.string = string;
        }

        public void setWrapper_boolean(Boolean wrapper_boolean) {
            this.wrapper_boolean = wrapper_boolean;
        }

        public void setPrimitive_boolean(boolean primitive_boolean) {
            this.primitive_boolean = primitive_boolean;
        }

        public void setWrapper_byte(Byte wrapper_byte) {
            this.wrapper_byte = wrapper_byte;
        }

        public void setPrimitive_byte(byte primitive_byte) {
            this.primitive_byte = primitive_byte;
        }

        public void setWrapper_char(Character wrapper_char) {
            this.wrapper_char = wrapper_char;
        }

        public void setPrimitive_char(char primitive_char) {
            this.primitive_char = primitive_char;
        }

        public void setWrapper_double(Double wrapper_double) {
            this.wrapper_double = wrapper_double;
        }

        public void setPrimitive_double(double primitive_double) {
            this.primitive_double = primitive_double;
        }

        public void setWrapper_float(Float wrapper_float) {
            this.wrapper_float = wrapper_float;
        }

        public void setPrimitive_float(float primitive_float) {
            this.primitive_float = primitive_float;
        }

        public void setWrapper_int(Integer wrapper_int) {
            this.wrapper_int = wrapper_int;
        }

        public void setPrimitive_int(int primitive_int) {
            this.primitive_int = primitive_int;
        }

        public void setWrapper_long(Long wrapper_long) {
            this.wrapper_long = wrapper_long;
        }

        public void setPrimitive_long(long primitive_long) {
            this.primitive_long = primitive_long;
        }

        public void setWrapper_short(Short wrapper_short) {
            this.wrapper_short = wrapper_short;
        }

        public void setPrimitive_short(short primitive_short) {
            this.primitive_short = primitive_short;
        }

        public void setCustomIgnoreField(int customIgnoreField) {
            this.customIgnoreField = customIgnoreField;
        }

        public void setChild1(ChildClass child1) {
            this.child1 = child1;
        }

        public void setChild2(ChildClass child2) {
            this.child2 = child2;
        }

        public void setChild3(ChildClass child3) {
            this.child3 = child3;
        }
    }

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

        @java.beans.ConstructorProperties({ "id", "testString", "testList",
                "testSet", "testMap" })
        public ChildClass(Long id, String testString, List<String> testList,
                Set<String> testSet, Map<String, String> testMap) {
            this.id = id;
            this.testString = testString;
            this.testList = testList;
            this.testSet = testSet;
            this.testMap = testMap;
        }

        public ChildClass() {
        }

        public Long getId() {
            return this.id;
        }

        public String getTestString() {
            return this.testString;
        }

        public List<String> getTestList() {
            return this.testList;
        }

        public Set<String> getTestSet() {
            return this.testSet;
        }

        public Map<String, String> getTestMap() {
            return this.testMap;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setTestString(String testString) {
            this.testString = testString;
        }

        public void setTestList(List<String> testList) {
            this.testList = testList;
        }

        public void setTestSet(Set<String> testSet) {
            this.testSet = testSet;
        }

        public void setTestMap(Map<String, String> testMap) {
            this.testMap = testMap;
        }
    }

}
