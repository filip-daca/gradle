/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.internal.migration;

import org.gradle.tooling.model.migration.TestResult;

import java.io.File;
import java.io.Serializable;

public class DefaultTestResult implements TestResult, Serializable {
    private final File xmlReportDir;

    public DefaultTestResult(File xmlReportDir) {
        this.xmlReportDir = xmlReportDir;
    }

    public File getXmlReportDir() {
        return xmlReportDir;
    }
}
