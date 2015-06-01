/*
 * Copyright (c) 2015-2015 Savoir Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
