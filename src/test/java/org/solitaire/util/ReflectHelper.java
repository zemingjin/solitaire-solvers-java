package org.solitaire.util;

import static java.util.Objects.nonNull;

public class ReflectHelper {
    public static void setField(Object obj, String fieldName, Object fieldValue) {
        setField(obj, obj.getClass(), fieldName, fieldValue);
    }

    public static void setField(Class<?> clazz, String fieldName, Object fieldValue) {
        setField(null, clazz, fieldName, fieldValue);
    }

    private static void setField(Object obj, Class<?> clazz, String fieldName, Object fieldValue) {
        try {
            var field = clazz.getDeclaredField(fieldName);
            var access = nonNull(obj) && field.canAccess(obj);

            field.setAccessible(true);
            field.set(obj, fieldValue);
            if (nonNull(obj)) {
                field.setAccessible(access);
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
}
