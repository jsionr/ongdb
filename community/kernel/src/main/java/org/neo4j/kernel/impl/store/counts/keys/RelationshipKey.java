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
package org.neo4j.kernel.impl.store.counts.keys;

import org.neo4j.kernel.impl.api.CountsVisitor;

import static org.neo4j.kernel.impl.util.IdPrettyPrinter.label;
import static org.neo4j.kernel.impl.util.IdPrettyPrinter.relationshipType;

public final class RelationshipKey implements CountsKey
{
    private final int startLabelId;
    private final int typeId;
    private final int endLabelId;

    RelationshipKey( int startLabelId, int typeId, int endLabelId )
    {
        this.startLabelId = startLabelId;
        this.typeId = typeId;
        this.endLabelId = endLabelId;
    }

    public int getStartLabelId()
    {
        return startLabelId;
    }

    public int getTypeId()
    {
        return typeId;
    }

    public int getEndLabelId()
    {
        return endLabelId;
    }

    @Override
    public String toString()
    {
        return String.format( "RelationshipKey[(%s)-%s->(%s)]",
                              label( startLabelId ), relationshipType( typeId ), label( endLabelId ) );
    }

    @Override
    public void accept( CountsVisitor visitor, long ignored, long count )
    {
        visitor.visitRelationshipCount( startLabelId, typeId, endLabelId, count );
    }

    @Override
    public CountsKeyType recordType()
    {
        return CountsKeyType.ENTITY_RELATIONSHIP;
    }

    @Override
    public int hashCode()
    {
        int result = startLabelId;
        result = 31 * result + typeId;
        result = 31 * result + endLabelId;
        result = 31 * result + recordType().hashCode();
        return result;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o instanceof RelationshipKey )
        {
            RelationshipKey that = (RelationshipKey) o;
            return endLabelId == that.endLabelId && startLabelId == that.startLabelId && typeId == that.typeId;
        }
        return false;
    }

    @Override
    public int compareTo( CountsKey other )
    {
        if ( other instanceof RelationshipKey )
        {
            RelationshipKey that = (RelationshipKey) other;
            if ( this.typeId != that.typeId )
            {
                return Integer.compare( this.typeId, that.typeId );
            }
            if ( this.startLabelId != that.startLabelId )
            {
                return Integer.compare( this.startLabelId, that.startLabelId );
            }
            return Integer.compare( this.endLabelId, that.endLabelId );
        }
        return recordType().compareTo( other.recordType() );
    }
}
