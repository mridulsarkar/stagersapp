package com.poc.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CollectionsUtil
{
    private CollectionsUtil() {
    }
    
    public static <X> Set<X> set() {
        return new HashSet<X>(1);
    }
    
    public static <T> List<T> list() {
        return new ArrayList<T>(1);
    }
    
    public static <K, V> Map<K, V> map() {
        return new HashMap<K, V>(2);
    }
    
    public static String serializeMap(final Map<?, ?> map) {
        final StringWriter writer = new StringWriter();
        final Serializer serializer = new Serializer((Writer)writer);
        try {
            serializer.writeObject((Object)map);
            serializer.flush();
            serializer.close();
        }
        catch (IOException exp) {
            return null;
        }
        final String result = writer.toString();
        try {
            serializer.close();
            writer.close();
        }
        catch (IOException e) {
            return null;
        }
        return result;
    }
    
    public static Map groupBy(final Collection<?> items, final ValueMapper accessor) {
        final Map result = map();
        for (final Object o : items) {
            final Object key = accessor.valueForObject(o);
            if (key != null) {
                List keyList = (List)result.get(key);
                if (keyList == null) {
                    keyList = list();
                    result.put(key, keyList);
                }
                keyList.add(o);
            }
        }
        return result;
    }
    
    public static Collection collect(final List<?> items, final Collection result, final ValueMapper accessor) {
        for (int i = 0, count = items.size(); i < count; ++i) {
            final Object o = items.get(i);
            final Object value = accessor.valueForObject(o);
            result.add(value);
        }
        return result;
    }
    
    public static List<?> collect(final List<?> items, final ValueMapper accessor) {
        return (List<?>)collect((List)items, (Collection)list(), accessor);
    }
    
    public interface ValueMapper {
        Object valueForObject(final Object object);
    }
}