/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2018 "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
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
package org.neo4j.tools.txlog.checktypes;

import org.junit.Test;

import org.neo4j.kernel.impl.store.record.RelationshipGroupRecord;

import static org.junit.Assert.assertTrue;

public class RelationshipGroupCheckTypeTest
{
    @Test
    public void inUseRecordEquality()
    {
        RelationshipGroupRecord record1 = new RelationshipGroupRecord( 1 );
        record1.initialize( true, 1, 2, 3, 4, 5, 6 );
        record1.setSecondaryUnitId( 42 );

        RelationshipGroupRecord record2 = record1.clone();

        RelationshipGroupCheckType check = new RelationshipGroupCheckType();

        assertTrue( check.equal( record1, record2 ) );
    }

    @Test
    public void notInUseRecordEquality()
    {
        RelationshipGroupRecord record1 = new RelationshipGroupRecord( 1 );
        record1.initialize( false, 1, 2, 3, 4, 5, 6 );
        record1.setSecondaryUnitId( 42 );

        RelationshipGroupRecord record2 = new RelationshipGroupRecord( 1 );
        record1.initialize( false, 11, 22, 33, 44, 55, 66 );
        record2.setSecondaryUnitId( 24 );

        RelationshipGroupCheckType check = new RelationshipGroupCheckType();

        assertTrue( check.equal( record1, record2 ) );
    }
}
