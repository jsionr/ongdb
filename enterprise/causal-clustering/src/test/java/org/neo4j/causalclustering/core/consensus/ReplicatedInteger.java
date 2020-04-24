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
package org.neo4j.causalclustering.core.consensus;

import java.util.Objects;

import org.neo4j.causalclustering.core.replication.ReplicatedContent;
import org.neo4j.causalclustering.messaging.marshalling.ReplicatedContentHandler;

import static java.lang.String.format;

public class ReplicatedInteger implements ReplicatedContent
{
    private final Integer value;

    private ReplicatedInteger( Integer data )
    {
        Objects.requireNonNull( data );
        this.value = data;
    }

    public static ReplicatedInteger valueOf( Integer value )
    {
        return new ReplicatedInteger( value );
    }

    public int get()
    {
        return value;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        ReplicatedInteger that = (ReplicatedInteger) o;
        return value.equals( that.value );
    }

    @Override
    public int hashCode()
    {
        return value.hashCode();
    }

    @Override
    public String toString()
    {
        return format( "Integer(%d)", value );
    }

    @Override
    public void handle( ReplicatedContentHandler contentHandler )
    {
        throw new UnsupportedOperationException( "No handler for this " + this.getClass() );
    }
}
