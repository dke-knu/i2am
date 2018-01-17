/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.scheduler;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface SchedulerAssignment {
    /**
     * Does this slot occupied by this assignment?
     * @param slot
     * @return
     */
    boolean isSlotOccupied(WorkerSlot slot);

    /**
     * is the executor assigned?
     * 
     * @param executor
     * @return
     */
    boolean isExecutorAssigned(ExecutorDetails executor);
    
    /**
     * get the topology-id this assignment is for.
     * @return
     */
    String getTopologyId();

    /**
     * get the executor -> slot map.
     * @return
     */
    Map<ExecutorDetails, WorkerSlot> getExecutorToSlot();

    /**
     * Return the executors covered by this assignments
     * @return
     */
    Set<ExecutorDetails> getExecutors();
    
    Set<WorkerSlot> getSlots();

    Map<WorkerSlot, Collection<ExecutorDetails>> getSlotToExecutors();
}
