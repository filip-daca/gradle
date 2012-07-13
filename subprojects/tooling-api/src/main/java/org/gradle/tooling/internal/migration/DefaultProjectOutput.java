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

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.migration.ProjectOutput;
import org.gradle.tooling.model.migration.TaskOutput;

import java.io.Serializable;

public class DefaultProjectOutput implements ProjectOutput, Serializable {
    private final String name;
    private final ProjectOutput parent;
    private final DomainObjectSet<ProjectOutput> children;
    private final DomainObjectSet<TaskOutput> taskOutputs;

    public DefaultProjectOutput(String name, ProjectOutput parent, DomainObjectSet<ProjectOutput> children, DomainObjectSet<TaskOutput> taskOutputs) {
        this.name = name;
        this.taskOutputs = taskOutputs;
        this.parent = parent;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return null;
    }

    public ProjectOutput getParent() {
        return parent;
    }

    public DomainObjectSet<ProjectOutput> getChildren() {
        return children;
    }

    public DomainObjectSet<TaskOutput> getTaskOutputs() {
        return taskOutputs;
    }
}
