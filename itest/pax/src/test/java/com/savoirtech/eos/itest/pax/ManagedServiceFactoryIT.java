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

package com.savoirtech.eos.itest.pax;

import java.io.File;

import javax.inject.Inject;

import com.savoirtech.eos.itest.bundle.service.Greeter;
import com.savoirtech.eos.itest.bundle.service.HelloService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.service.cm.ConfigurationAdmin;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ManagedServiceFactoryIT extends Assert {
//----------------------------------------------------------------------------------------------------------------------
// Fields
//----------------------------------------------------------------------------------------------------------------------

    private static final String DEFAULT_KARAF_VERSION = "3.0.3";

    @Inject
    private HelloService helloService;

    @Inject
    private ConfigurationAdmin configurationAdmin;

    @Inject
    @Filter("(language=english)")
    private Greeter englishGreeter;

    @Inject
    @Filter("(language=spanish)")
    private Greeter spanishGreeter;

//----------------------------------------------------------------------------------------------------------------------
// Other Methods
//----------------------------------------------------------------------------------------------------------------------

    @Configuration
    public Option[] configure() {
        final String jvmOptions = StringUtils.defaultIfBlank(System.getProperty("jvm.options"), "-Dnoop");
        final String karafVersion = System.getProperty("karaf.version", DEFAULT_KARAF_VERSION);
        final String projectVersion = System.getProperty("project.version");
        return options(
                karafDistributionConfiguration()
                        .frameworkUrl(maven("org.apache.karaf", "apache-karaf", karafVersion).type("tar.gz"))
                        .unpackDirectory(new File("target/karaf")),
                vmOption(jvmOptions),
                configureConsole()
                        .startRemoteShell()
                        .ignoreLocalConsole(),
                editConfigurationFileExtend("etc/com.savoirtech.eos.itest.bundle.greeter-1.cfg", "language", "english"),
                editConfigurationFileExtend("etc/com.savoirtech.eos.itest.bundle.greeter-1.cfg", "pattern", "Hello, %s!"),
                editConfigurationFileExtend("etc/com.savoirtech.eos.itest.bundle.greeter-2.cfg", "language", "spanish"),
                editConfigurationFileExtend("etc/com.savoirtech.eos.itest.bundle.greeter-2.cfg", "pattern", "Hola, %s!"),

                features(maven("com.savoirtech.eos", "eos-itest-features", projectVersion).type("xml").classifier("features"), "eos-itest-bundle"),
                junitBundles(),
                keepRuntimeFolder(),
                logLevel(LogLevelOption.LogLevel.WARN));
    }
    
    @Test
    public void testManagedServiceFactoryCreation() throws Exception {
        assertEquals("Hello, OSGi!", helloService.sayHello("english", "OSGi"));
        assertEquals("Hola, OSGi!", helloService.sayHello("spanish", "OSGi"));
    }
}
