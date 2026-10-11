package lilifw.utils;

import java.lang.reflect.Field;

import jakarta.servlet.http.HttpServletRequest;

public class ModelBinder {

    public Object bind(Class<?> type, HttpServletRequest request) throws Exception {
        Object target = type.getDeclaredConstructor().newInstance();

        for (Field field : type.getDeclaredFields()) {
            String value = request.getParameter(field.getName());
            if (value == null) {
                continue;
            }
            field.setAccessible(true);
            field.set(target, convert(value, field.getType(), field.getName()));
        }

        return target;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object convert(String value, Class<?> type, String name) throws Exception {
        if (value == null) {
            return type.isPrimitive() ? defaultValue(type) : null;
        }
        if (type.equals(String.class)) {
            return value;
        }
        if (type.equals(int.class) || type.equals(Integer.class)) {
            return Integer.valueOf(value);
        }
        if (type.equals(long.class) || type.equals(Long.class)) {
            return Long.valueOf(value);
        }
        if (type.equals(double.class) || type.equals(Double.class)) {
            return Double.valueOf(value);
        }
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return Boolean.valueOf(value);
        }
        if (type.equals(float.class) || type.equals(Float.class)) {
            return Float.valueOf(value);
        }
        if (type.equals(short.class) || type.equals(Short.class)) {
            return Short.valueOf(value);
        }
        if (type.equals(byte.class) || type.equals(Byte.class)) {
            return Byte.valueOf(value);
        }
        if (type.isEnum()) {
            return Enum.valueOf((Class) type, value);







        }
        throw new Exception("Type de parametre non supporte : '" + name + "' de type " + type.getName());
    }

    private Object defaultValue(Class<?> primitive) {
        if (primitive.equals(boolean.class)) {
            return false;
        }
        if (primitive.equals(char.class)) {
            return '\u0000';
        }
        if (primitive.equals(byte.class)) {
            return (byte) 0;
        }
        if (primitive.equals(short.class)) {
            return (short) 0;
        }
        if (primitive.equals(int.class)) {
            return 0;
        }
        if (primitive.equals(long.class)) {
            return 0L;
        }
        if (primitive.equals(float.class)) {
            return 0f;
        }
        return 0d;
    }
}