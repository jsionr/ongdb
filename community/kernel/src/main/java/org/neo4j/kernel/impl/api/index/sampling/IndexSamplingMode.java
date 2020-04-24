/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2020 "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of ONgDB.
 *
 * ONgDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.api.index.sampling;

public enum IndexSamplingMode
{
    TRIGGER_REBUILD_ALL( false, true )
            {
                @Override
                public String toString()
                {
                    return "FORCE REBUILD";
                }
            },
    TRIGGER_REBUILD_UPDATED( true, true )
            {
                @Override
                public String toString()
                {
                    return "REBUILD OUTDATED";
                }
            },
    BACKGROUND_REBUILD_UPDATED( true, false )
            {
                @Override
                public String toString()
                {
                    return "BACKGROUND-REBUILD OF OUTDATED";
                }
            };

    public final boolean sampleOnlyIfUpdated;
    public final boolean blockUntilAllScheduled;

    IndexSamplingMode( boolean sampleOnlyIfUpdated, boolean blockUntilAllScheduled )
    {
        this.sampleOnlyIfUpdated = sampleOnlyIfUpdated;
        this.blockUntilAllScheduled = blockUntilAllScheduled;
    }
}
