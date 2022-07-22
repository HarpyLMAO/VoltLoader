package net.kerfu.volt.access;

import lombok.Getter;

import java.lang.reflect.Field;

@Getter
public class FieldAccess {

    private Field field;

    public FieldAccess(Class<?> target, String name) {
        try {
            this.field = target.getDeclaredField(name);
        } catch (NoSuchFieldException exception) {
            exception.printStackTrace();
        }

        this.field.setAccessible(true);
    }

    public <T> T read(Object instance) {
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public <T> void set(Object instance, T value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }
}
