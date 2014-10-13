package org.zanata.rest.editor.dto;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TransUnits implements Map<String, TransUnit>, Serializable {
    private Map<String, TransUnit> map =
            new HashMap<String, TransUnit>();

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public TransUnit get(Object key) {
        return map.get(key);
    }

    @Override
    public TransUnit put(String key, TransUnit value) {
        return map.put(key, value);
    }

    @Override
    public TransUnit remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends TransUnit> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<TransUnit> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, TransUnit>> entrySet() {
        return map.entrySet();
    }
}
