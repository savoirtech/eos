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

package com.savoirtech.eos.itest.bundle.service.impl;

import com.savoirtech.eos.itest.bundle.service.Greeter;
import com.savoirtech.eos.itest.bundle.service.HelloService;
import com.savoirtech.eos.pattern.whiteboard.KeyedWhiteboard;
import org.apache.commons.lang3.Validate;
import org.osgi.framework.BundleContext;

public class HelloServiceImpl implements HelloService {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private final KeyedWhiteboard<String, Greeter> greeters;

//----------------------------------------------------------------------------------------------------------------------
// Constructors
//----------------------------------------------------------------------------------------------------------------------

    public HelloServiceImpl(BundleContext bundleContext) {
        this.greeters = new KeyedWhiteboard<>(bundleContext, Greeter.class, (svc, props) -> svc.getLanguage());
        greeters.start();
    }

//----------------------------------------------------------------------------------------------------------------------
// HelloService Implementation
//----------------------------------------------------------------------------------------------------------------------

    @Override
    public String sayHello(String language, String name) {
        Greeter greeter = Validate.notNull(greeters.getService(language), "Language %s not supported!", language);
        return greeter.sayHello(name);
    }

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    public void shutdown() {
        greeters.stop();
    }
}
