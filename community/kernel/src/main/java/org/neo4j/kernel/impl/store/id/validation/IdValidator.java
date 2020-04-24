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
package org.neo4j.kernel.impl.store.id.validation;

import org.neo4j.kernel.impl.store.id.IdGenerator;
import org.neo4j.kernel.impl.store.id.IdGeneratorImpl;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;

/**
 * Utility containing methods to validate record id used in {@link AbstractBaseRecord} and possibly generated by
 * {@link IdGenerator}. Takes into account special reserved value {@link IdGeneratorImpl#INTEGER_MINUS_ONE}.
 */
public final class IdValidator
{
    private IdValidator()
    {
    }

    /**
     * Checks if the given id is reserved, i.e. {@link IdGeneratorImpl#INTEGER_MINUS_ONE}.
     *
     * @param id the id to check.
     * @return <code>true</code> if the given id is {@link IdGeneratorImpl#INTEGER_MINUS_ONE}, <code>false</code>
     * otherwise.
     * @see IdGeneratorImpl#INTEGER_MINUS_ONE
     */
    public static boolean isReservedId( long id )
    {
        return id == IdGeneratorImpl.INTEGER_MINUS_ONE;
    }

    /**
     * Asserts that the given id is valid:
     * <ul>
     * <li>non-negative
     * <li>less than the given max id
     * <li>not equal to {@link IdGeneratorImpl#INTEGER_MINUS_ONE}
     * </ul>
     *
     * @param id the id to check.
     * @param maxId the max allowed id.
     * @see IdGeneratorImpl#INTEGER_MINUS_ONE
     */
    public static void assertValidId( IdType idType, long id, long maxId )
    {
        if ( isReservedId( id ) )
        {
            throw new ReservedIdException( id );
        }
        assertIdWithinCapacity( idType, id, maxId );
    }

    /**
     * Asserts that the given id is valid with respect to given max id:
     * <ul>
     * <li>non-negative
     * <li>less than the given max id
     * </ul>
     *
     * @param idType
     * @param id the id to check.
     * @param maxId the max allowed id.
     */
    public static void assertIdWithinCapacity( IdType idType, long id, long maxId )
    {
        if ( id < 0 )
        {
            throw new NegativeIdException( id );
        }
        if ( id > maxId )
        {
            throw new IdCapacityExceededException( idType, id, maxId );
        }
    }

    public static boolean hasReservedIdInRange( long startIdInclusive, long endIdExclusive )
    {
        return startIdInclusive <= IdGeneratorImpl.INTEGER_MINUS_ONE &&
                endIdExclusive > IdGeneratorImpl.INTEGER_MINUS_ONE;
    }
}
