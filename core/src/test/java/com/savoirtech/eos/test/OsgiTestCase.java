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

package com.savoirtech.eos.test;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.felix.connect.PojoServiceRegistryFactoryImpl;
import org.apache.felix.connect.launch.BundleDescriptor;
import org.apache.felix.connect.launch.ClasspathScanner;
import org.apache.felix.connect.launch.PojoServiceRegistry;
import org.apache.felix.connect.launch.PojoServiceRegistryFactory;
import org.junit.Before;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public abstract class OsgiTestCase extends MockObjectTestCase {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    public static final String SYMBOLIC_NAME_HEADER = "Bundle-SymbolicName";
    protected BundleContext bundleContext;
    protected PojoServiceRegistry registry;

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Before
    public void initBundleContext() throws Exception {
        Map<String, Object> config = new HashMap<>();
        List<BundleDescriptor> bundles = new ClasspathScanner().scanForBundles().stream().filter(bundle -> bundle.getHeaders().get(SYMBOLIC_NAME_HEADER) != null).collect(Collectors.toList());
        config.put(PojoServiceRegistryFactory.BUNDLE_DESCRIPTORS, bundles);
        registry = new PojoServiceRegistryFactoryImpl().newPojoServiceRegistry(config);
        bundleContext = registry.getBundleContext();
    }

    @SuppressWarnings("unchecked")
    protected <T> ServiceRegistration<T> registerService(Class<? super T> serviceInterface, T serviceObject, ServicePropsBuilder builder) {
        return (ServiceRegistration<T>)registry.registerService(serviceInterface.getName(), serviceObject, builder.build());
    }

    protected ServicePropsBuilder serviceProps() {
        return new ServicePropsBuilder();
    }

//----------------------------------------------------------------------------------------------------------------------
// Inner Classes
//----------------------------------------------------------------------------------------------------------------------

    protected static class ServicePropsBuilder {
        private final Dictionary<String, Object> props = new Hashtable<>();

        public ServicePropsBuilder with(String name, Object value) {
            props.put(name, value);
            return this;
        }

        public Dictionary<String, Object> build() {
            return props;
        }
    }
}
