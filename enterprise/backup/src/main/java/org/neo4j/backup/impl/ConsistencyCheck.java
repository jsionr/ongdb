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
package org.neo4j.backup.impl;

import org.neo4j.consistency.ConsistencyCheckService;
import org.neo4j.consistency.checking.full.ConsistencyCheckIncompleteException;
import org.neo4j.consistency.checking.full.ConsistencyFlags;
import org.neo4j.helpers.progress.ProgressMonitorFactory;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.logging.LogProvider;

public interface ConsistencyCheck
{
    ConsistencyCheck NONE = new ConsistencyCheck()
            {
                @Override
                public String name()
                {
                    return "none";
                }

                @Override
                public boolean runFull( DatabaseLayout databaseLayout, Config tuningConfiguration,
                        ProgressMonitorFactory progressFactory, LogProvider logProvider,
                        FileSystemAbstraction fileSystem, PageCache pageCache, boolean verbose,
                        ConsistencyFlags consistencyFlags )
                {
                    return true;
                }
            };

    ConsistencyCheck FULL = new ConsistencyCheck()
            {
                @Override
                public String name()
                {
                    return "full";
                }

                @Override
                public boolean runFull( DatabaseLayout databaseLayout, Config tuningConfiguration,
                        ProgressMonitorFactory progressFactory, LogProvider logProvider,
                        FileSystemAbstraction fileSystem, PageCache pageCache, boolean verbose,
                        ConsistencyFlags consistencyFlags ) throws ConsistencyCheckFailedException
                {
                    try
                    {
                        return new ConsistencyCheckService().runFullConsistencyCheck(
                                databaseLayout, tuningConfiguration, progressFactory, logProvider, fileSystem,
                                pageCache, verbose, consistencyFlags ).isSuccessful();
                    }
                    catch ( ConsistencyCheckIncompleteException e )
                    {
                        throw new ConsistencyCheckFailedException( e );
                    }
                }
            };

    String name();

    boolean runFull( DatabaseLayout databaseLayout, Config tuningConfiguration, ProgressMonitorFactory progressFactory,
                     LogProvider logProvider, FileSystemAbstraction fileSystem, PageCache pageCache, boolean verbose,
                     ConsistencyFlags consistencyFlags ) throws ConsistencyCheckFailedException;

    String toString();

    static ConsistencyCheck fromString( String name )
    {
        for ( ConsistencyCheck consistencyCheck : new ConsistencyCheck[]{NONE, FULL} )
        {
            if ( consistencyCheck.name().equalsIgnoreCase( name ) )
            {
                return consistencyCheck;
            }
        }
        throw new IllegalArgumentException( "Unknown consistency check name: " + name +
                ". Supported values: NONE, FULL" );
    }
}
