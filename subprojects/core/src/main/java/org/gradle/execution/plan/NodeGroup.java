/*
 * Copyright 2022 the original author or authors.
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

package org.gradle.execution.plan;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * <p>Represents the reason why a node is included in the work graph. In general, nodes are included in the graph because they are either a "root node" or they are reachable from one or more "root nodes"
 * or both. There are two basic kind of "root nodes":</p>
 * <ul>
 *     <li>
 *         A "requested task", which is a task requested on the command-line. For example given `gradle a b:` then all the tasks that match `a` are treated as a requested task group (with ordinal 0)
 *         and all the tasks that match `b` are treated as another requested task group (with ordinal 1).
 *     </li>
 *     <li>A finalizer node. Each finalizer is treated as its own node group.</li>
 * </ul>
 *
 * <p>A node may be included in more than one group, for example when it is reachable from both a requested task and a finalizer.</p>
 *
 * <p>The the groups that the node belongs to can affect how the node is scheduled relative to other nodes in the graph.</p>
 */
public abstract class NodeGroup {
    public static final NodeGroup DEFAULT_GROUP = new NodeGroup() {
        @Override
        public String toString() {
            return "default group";
        }

        @Nullable
        @Override
        public OrdinalGroup asOrdinal() {
            return null;
        }

        @Override
        public boolean isReachableFromEntryPoint() {
            return false;
        }
    };

    @Nullable
    public abstract OrdinalGroup asOrdinal();

    public abstract boolean isReachableFromEntryPoint();

    @Nullable
    public FinalizerGroup asFinalizer() {
        return null;
    }

    /**
     * Returns the set of nodes which must complete before any node in this group can start.
     */
    public Iterable<? extends Node> getSuccessors() {
        return Collections.emptyList();
    }

    public Iterable<? extends Node> getSuccessorsInReverseOrder() {
        return Collections.emptyList();
    }

    public boolean isSuccessorsCompleteFor(Node node) {
        return true;
    }

    public boolean isSuccessorsSuccessfulFor(Node node) {
        return true;
    }

    public void addMember(Node node) {
    }

    public void removeMember(Node node) {
    }
}
