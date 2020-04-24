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
package org.neo4j.test.limited;

import java.io.File;
import java.util.Map;

import org.neo4j.graphdb.facade.GraphDatabaseFacadeFactory;
import org.neo4j.graphdb.factory.module.PlatformModule;
import org.neo4j.graphdb.factory.module.edition.CommunityEditionModule;
import org.neo4j.graphdb.mockfs.UncloseableDelegatingFileSystemAbstraction;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.DatabaseInfo;
import org.neo4j.test.ImpermanentGraphDatabase;

public class LimitedFileSystemGraphDatabase extends ImpermanentGraphDatabase
{
    private FileSystemAbstraction fs;
    private LimitedFilesystemAbstraction limitedFs;

    public LimitedFileSystemGraphDatabase( File storeDir )
    {
        super( storeDir );
    }

    @Override
    protected void create( File storeDir, Map<String, String> params, GraphDatabaseFacadeFactory.Dependencies dependencies )
    {
        new GraphDatabaseFacadeFactory( DatabaseInfo.COMMUNITY, CommunityEditionModule::new )
        {
            @Override
            protected PlatformModule createPlatform( File storeDir, Config config, Dependencies dependencies )
            {
                return new ImpermanentPlatformModule( storeDir, config, databaseInfo, dependencies )
                {
                    @Override
                    protected FileSystemAbstraction createFileSystemAbstraction()
                    {
                        fs = super.createFileSystemAbstraction();
                        limitedFs = new LimitedFilesystemAbstraction( new UncloseableDelegatingFileSystemAbstraction( fs ) );
                        return limitedFs;
                    }
                };
            }
        }.initFacade( storeDir, params, dependencies, this );
    }

    public void runOutOfDiskSpaceNao()
    {
        this.limitedFs.runOutOfDiskSpace( true );
    }

    public void somehowGainMoreDiskSpace()
    {
        this.limitedFs.runOutOfDiskSpace( false );
    }

    public FileSystemAbstraction getFileSystem()
    {
        return fs;
    }
}
