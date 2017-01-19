package org.zanata.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.hibernate.proxy.HibernateProxyHelper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
// @formatter:off
// @formatter:on

/**
 * Utility class for creating copy of Hibernate entity in
 * {@link org.zanata.model}.
 *
 * This will copy all writable properties {@link PropertyUtilsBean#isWriteable}
 * in provided bean except for:
 *
 * Properties in {@link this#COMMON_IGNORED_FIELDS}, and Properties in
 * ignoreProperties field.
 *
 * @see this#shouldCopy(PropertyUtilsBean, Object, String, java.util.List) for
 *      condition check.
 *
 *
 *      Property which has {@link javax.persistence.OneToMany#mappedBy()} or
 *      {@link javax.persistence.OneToOne} in field or GetterMethod
 *      {@link PropertyUtilsBean#getReadMethod} will be copied using
 *      {@link this#copyBean(Object, String...)}, otherwise
 *      {@link BeanUtilsBean#copyProperty} will be used.
 *
 *      New collection will be created if bean type is: {@link java.util.List},
 *      {@link java.util.Set} and {@link java.util.Map}
 *
 *
 * @see this#copyBean(Object, String...)
 * @see this#copyBean(Object, Object, String...)
 *
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class JPACopier {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(JPACopier.class);

    /**
     * Common ignored fields when copying entity, {id, creationDate,
     * lastChanged}
     */
    private static final List<String> COMMON_IGNORED_FIELDS = ImmutableList
            .<String> builder().add("id").add("creationDate").build();

    /**
     * Fields to copy using {@link this#copyBean(Object, String...)} in class.
     */
    private static Map<Class, List<String>> FIELDS_TO_COPY =
            Maps.newConcurrentMap();

    /**
     * Create a clone of all writable properties from fromBean.
     *
     * @param fromBean
     *            original bean to be copy from. Needs to have no-arguments
     *            constructor.
     * @param ignoreProperties
     *            properties to be ignore when copy
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static <T> T copyBean(@Nonnull T fromBean,
            String... ignoreProperties)
            throws IllegalAccessException, InstantiationException,
            InvocationTargetException, NoSuchMethodException {
        Preconditions.checkNotNull(fromBean);
        // create a copy of the bean entity
        // TODO: replace HibernateProxyHelper as its being phased out
        Object copy = HibernateProxyHelper
                .getClassWithoutInitializingProxy(fromBean).newInstance();
        copy = copyBean(fromBean, copy, ignoreProperties);
        return (T) copy;
    }

    /**
     * Create a clone of all writable properties from fromBean.
     *
     * @param fromBean
     *            original bean to be copy from
     * @param toBean
     *            destination bean
     * @param ignoreProperties
     *            properties to be ignore when copy
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     *
     * @return fromBean if
     *         {@link org.zanata.util.JPACopier#isPrimitiveOrString(Object)},
     *         otherwise return toBean
     */
    public static <T> T copyBean(@Nonnull T fromBean, @Nonnull T toBean,
            String... ignoreProperties)
            throws IllegalAccessException, InstantiationException,
            InvocationTargetException, NoSuchMethodException {
        Preconditions.checkNotNull(fromBean);
        Preconditions.checkNotNull(toBean);
        if (isPrimitiveOrString(fromBean)) {
            return fromBean;
        }
        BeanUtilsBean beanUtilsBean = BeanUtilsBean.getInstance();
        if (isCollectionType(fromBean.getClass())) {
            toBean = (T) createNewCollection(fromBean.getClass(), fromBean);
            return toBean;
        }
        List<String> ignoreList = Lists.newArrayList(ignoreProperties);
        Map<String, Object> propertiesMap =
                beanUtilsBean.getPropertyUtils().describe(fromBean);
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
            String property = entry.getKey();
            Object value = entry.getValue();
            if (!shouldCopy(beanUtilsBean.getPropertyUtils(), toBean, property,
                    ignoreList)) {
                continue;
            }
            if (value != null && isJPACopyProperty(fromBean, property)) {
                value = copyBean(value);
            }
            copyProperty(beanUtilsBean, toBean, property, value);
        }
        return toBean;
    }

    /**
     * Check if property is writable and not in ignore list and common ignore
     * list.
     *
     * @param propertyUtilsBean
     * @param toBean
     * @param property
     * @param ignoreList
     */
    private static boolean shouldCopy(PropertyUtilsBean propertyUtilsBean,
            Object toBean, String property, List<String> ignoreList) {
        return propertyUtilsBean.isWriteable(toBean, property)
                && !ignoreList.contains(property)
                && !COMMON_IGNORED_FIELDS.contains(property);
    }

    /**
     * Check is property is a JPA copier field
     *
     * @param bean
     * @param property
     */
    private static boolean isJPACopyProperty(Object bean, String property)
            throws IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        if (!FIELDS_TO_COPY.containsKey(bean.getClass())) {
            FIELDS_TO_COPY.put(bean.getClass(), getJPACopierFields(bean));
        }
        return FIELDS_TO_COPY.get(bean.getClass()).contains(property);
    }

    /**
     * Check if obj is Primitive type or {@link java.lang.String}
     *
     * @see Class#isPrimitive()
     * @param obj
     */
    private static boolean isPrimitiveOrString(Object obj) {
        return obj instanceof String
                || Primitives.isWrapperType(Primitives.wrap(obj.getClass()));
    }

    /**
     * Copy value to property. New instance of value will be created if
     * propertyType is {@link java.util.List}, {@link java.util.Set} or
     * {@link java.util.Map}.
     *
     * Note: ArrayList, HashSet and HashMap are being used to create new
     * instance of Collection, which will potential be an issue if propertyType
     * is not one of those implementation but sharing the same interface.
     */
    private static void copyProperty(BeanUtilsBean beanUtilsBean, Object toBean,
            String property, Object value) throws InvocationTargetException,
            IllegalAccessException, NoSuchMethodException {
        Class propertyType = beanUtilsBean.getPropertyUtils()
                .getPropertyDescriptor(toBean, property).getPropertyType();
        if (isCollectionType(propertyType)) {
            value = createNewCollection(propertyType, value);
        }
        beanUtilsBean.copyProperty(toBean, property, value);
    }

    /**
     * Check if clazz is List, Set or Map
     *
     * @param clazz
     */
    private static boolean isCollectionType(Class clazz) {
        return clazz == List.class || clazz == Set.class || clazz == Map.class;
    }

    /**
     * Create new instance of collection is value.class is List, Set, or Map
     *
     * @param clazz
     * @param value
     */
    private static Object createNewCollection(Class clazz, Object value) {
        if (value != null) {
            if (clazz == List.class) {
                List<Object> list = Lists.<Object> newArrayList();
                list.addAll((List<Object>) value);
                return list;
            } else if (clazz == Set.class) {
                Set<Object> set = Sets.<Object> newHashSet();
                set.addAll((Set<Object>) value);
                return set;
            } else if (clazz == Map.class) {
                Map<Object, Object> map = Maps.<Object, Object> newHashMap();
                map.putAll((Map<Object, Object>) value);
                return map;
            }
        }
        return value;
    }

    /**
     * Return list of properties that should use JPACopier to copy.
     * {@link javax.persistence.OneToMany#mappedBy()}
     * {@link javax.persistence.OneToOne}
     *
     * This runs only once for each Class type.
     *
     * @param bean
     *            The actual bean of which fields will be extracted
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private static List<String> getJPACopierFields(Object bean)
            throws IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        PropertyUtilsBean propertyUtilsBean =
                BeanUtilsBean.getInstance().getPropertyUtils();
        List<String> properties = Lists.newCopyOnWriteArrayList();
        // TODO: replace HibernateProxyHelper as its being phased out
        Class noProxyBean =
                HibernateProxyHelper.getClassWithoutInitializingProxy(bean);
        Map<String, Object> propertiesMap = propertyUtilsBean.describe(bean);
        for (String property : propertiesMap.keySet()) {
            // Read annotate in Field or Getter method of property
            try {
                PropertyDescriptor descriptor =
                        propertyUtilsBean.getPropertyDescriptor(bean, property);
                String methodName =
                        propertyUtilsBean.getReadMethod(descriptor).getName();
                Method getterMethod = noProxyBean.getMethod(methodName);
                if (isUseJPACopier(getterMethod)) {
                    properties.add(property);
                    continue;
                }
            } catch (NoSuchMethodException e) {
                log.debug("Read method inaccessible for {0} in class-{1}",
                        property, noProxyBean.getName());
            }
            Field field = FieldUtils.getField(bean.getClass(), property, true);
            if (isUseJPACopier(field)) {
                properties.add(property);
            }
        }
        return properties;
    }

    /**
     * Check if accessibleObject contains
     *
     * {@link javax.persistence.OneToMany#mappedBy()}
     * {@link javax.persistence.OneToOne}
     *
     * @param accessibleObject
     *            method object
     */
    private static boolean isUseJPACopier(AccessibleObject accessibleObject) {
        if (accessibleObject == null) {
            return false;
        }
        if (accessibleObject.isAnnotationPresent(OneToOne.class)) {
            return true;
        } else if (accessibleObject.isAnnotationPresent(OneToMany.class)
                && StringUtils.isNotEmpty(accessibleObject
                        .getAnnotation(OneToMany.class).mappedBy())) {
            return true;
        }
        return false;
    }
}
