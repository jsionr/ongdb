/*
 * Copyright (c) 2002-2018 "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * This file is part of ONgDB Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) as found
 * in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 */
package org.neo4j.causalclustering.core.state;

import java.io.IOException;

import org.neo4j.causalclustering.SessionTracker;
import org.neo4j.causalclustering.core.state.machines.CoreStateMachines;
import org.neo4j.causalclustering.core.state.snapshot.CoreSnapshot;
import org.neo4j.causalclustering.core.state.snapshot.CoreStateType;
import org.neo4j.causalclustering.core.state.storage.StateStorage;

import static java.lang.Long.max;

public class CoreState
{
    private CoreStateMachines coreStateMachines;
    private final SessionTracker sessionTracker;
    private final StateStorage<Long> lastFlushedStorage;

    public CoreState( CoreStateMachines coreStateMachines, SessionTracker sessionTracker,
            StateStorage<Long> lastFlushedStorage )
    {
        this.coreStateMachines = coreStateMachines;
        this.sessionTracker = sessionTracker;
        this.lastFlushedStorage = lastFlushedStorage;
    }

    synchronized void augmentSnapshot( CoreSnapshot coreSnapshot )
    {
        coreStateMachines.addSnapshots( coreSnapshot );
        coreSnapshot.add( CoreStateType.SESSION_TRACKER, sessionTracker.snapshot() );
    }

    synchronized void installSnapshot( CoreSnapshot coreSnapshot )
    {
        coreStateMachines.installSnapshots( coreSnapshot );
        sessionTracker.installSnapshot( coreSnapshot.get( CoreStateType.SESSION_TRACKER ) );
    }

    synchronized void flush( long lastApplied ) throws IOException
    {
        coreStateMachines.flush();
        sessionTracker.flush();
        lastFlushedStorage.persistStoreData( lastApplied );
    }

    public CommandDispatcher commandDispatcher()
    {
        return coreStateMachines.commandDispatcher();
    }

    long getLastAppliedIndex()
    {
        return max( coreStateMachines.getLastAppliedIndex(), sessionTracker.getLastAppliedIndex() );
    }

    long getLastFlushed()
    {
        return lastFlushedStorage.getInitialState();
    }
}
