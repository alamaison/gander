/*
 * Created on Nov 12, 2004
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/**
 * @author Fabio Zadrozny
 */
public class REF {
    
    /**
     * @return the field from a class that matches the passed attr name (or null if it couldn't be found)
     * @see #getAttrObj(Object, String) to get the actual value of the field.
     */
    public static Field getAttr(Object o, String attr){
        try {
            return o.getClass().getDeclaredField(attr);
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        }
        return null;
    }
    
    /**
     * @return the value of some attribute in the given object
     */
    public static Object getAttrObj(Object o, String attr, boolean raiseExceptionIfNotAvailable){
        try {
            Field field = REF.getAttr(o, attr);
            if(field != null){
                //get it even if it's not public!
                if((field.getModifiers() & Modifier.PUBLIC) == 0){
                    field.setAccessible(true);
                }
                Object obj = field.get(o);
                return obj;
            }
        }catch (Exception e) {
            //ignore
            if(raiseExceptionIfNotAvailable){
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}

