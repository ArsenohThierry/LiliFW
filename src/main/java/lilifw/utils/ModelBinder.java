package lilifw.utils;

import java.lang.reflect.Field;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lilifw.dto.MapFile;

public class ModelBinder {

    public Object bind(Class<?> type, HttpServletRequest request) throws Exception {
        Object target = type.getDeclaredConstructor().newInstance();

        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            if (MapFile.class.equals(fieldType)) {
                MapFile mf = bindFichier(field.getName(), request);
                if (mf != null) {
                    field.set(target, mf);
                }
            } else if (isObjet(fieldType)) {
                field.set(target, bind(fieldType, request));
            } else {
                String value = request.getParameter(field.getName());
                if (value != null) {
                    field.set(target, convert(value, fieldType, field.getName()));
                }
            }
        }

        return target;
    }

    public MapFile bindFichier(String nomAttribut, HttpServletRequest request) throws Exception {
        String ct = request.getContentType();
        if (ct == null || !ct.toLowerCase().startsWith("multipart/")) {
            return null;
        }
        Part part = request.getPart(nomAttribut);
        if (part == null) {
            return null;
        }
        String nom = part.getSubmittedFileName();
        if (nom == null || nom.isEmpty()) {
            return null;
        }
        String ext = nom.contains(".") ? nom.substring(nom.lastIndexOf('.') + 1) : "";
        return new MapFile(nom, ext, part.getInputStream().readAllBytes());
    }


    
    private boolean isObjet(Class<?> type) {
        if (type.isPrimitive()) return false;
        if (type.isEnum()) return false;
        if (type.isArray()) return false;
        if (type.equals(String.class)) return false;
        if (type.equals(Integer.class) || type.equals(int.class)) return false;
        if (type.equals(Long.class) || type.equals(long.class)) return false;
        if (type.equals(Double.class) || type.equals(double.class)) return false;
        if (type.equals(Boolean.class) || type.equals(boolean.class)) return false;
        if (type.equals(Float.class) || type.equals(float.class)) return false;
        if (type.equals(Short.class) || type.equals(short.class)) return false;
        if (type.equals(Byte.class) || type.equals(byte.class)) return false;
        if (type.equals(Character.class) || type.equals(char.class)) return false;
        if (MapFile.class.equals(type)) return false;
        return true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object convert(String value, Class<?> type, String name) throws Exception {
        if (value == null) {return type.isPrimitive() ? defaultValue(type) : null;}
        if (type.equals(String.class)) {return value;}
        if (type.equals(int.class) || type.equals(Integer.class)) {return Integer.valueOf(value);}
        if (type.equals(long.class) || type.equals(Long.class)) {return Long.valueOf(value);}
        if (type.equals(double.class) || type.equals(Double.class)) {return Double.valueOf(value);}
        if (type.equals(boolean.class) || type.equals(Boolean.class)) {return Boolean.valueOf(value);}
        if (type.equals(float.class) || type.equals(Float.class)) {return Float.valueOf(value);}
        if (type.equals(short.class) || type.equals(Short.class)) {return Short.valueOf(value);}
        if (type.equals(byte.class) || type.equals(Byte.class)) {return Byte.valueOf(value);}
        if (type.isEnum()) {return Enum.valueOf((Class) type, value);}
        throw new Exception("Type de parametre non supporte : '" + name + "' de type " + type.getName());
    }

    private Object defaultValue(Class<?> primitive) {
        if (primitive.equals(boolean.class)) {return false;}
        if (primitive.equals(char.class)) {return '\u0000';}
        if (primitive.equals(byte.class)) {return (byte) 0;}
        if (primitive.equals(short.class)) {return (short) 0;}
        if (primitive.equals(int.class)) {return 0;}
        if (primitive.equals(long.class)) {return 0L;}
        if (primitive.equals(float.class)) {return 0f;}
        return 0d;
    }
}
