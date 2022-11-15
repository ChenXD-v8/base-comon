package com.cxd.basecommon.distributedlock;
import java.lang.reflect.Field;
/**
 * <p>
 * description
 * </p>
 *
 * @author xiaodong.chen01@hand-china.com 2022/11/15 15:38
 */
public class ReflectionUtils {

    public static Object getFieldValue(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        if (target == null) {
            return null;
        }
        Class clazz = target.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static <T> T getFieldValue(Object target, String fieldName, Class<T> returnType) throws NoSuchFieldException, IllegalAccessException {
        if (target == null) {
            return null;
        }
        Class clazz = target.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
