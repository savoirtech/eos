package com.savoirtech.eos.util;

import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public class TypeVariableUtils {

    @SuppressWarnings("unchecked")
    public static <T extends P, P, C extends Type> C getTypeVariableBinding(Class<T> concreteClass, Class<P> definingClass, int varIndex) {
        final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(concreteClass, definingClass);
        final TypeVariable<Class<P>> entityTypeVar = definingClass.getTypeParameters()[varIndex];
        return (C) typeArguments.get(entityTypeVar);
    }
}
