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
package org.neo4j.values.virtual;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.neo4j.helpers.collection.Iterators.iteratorsEqual;
import static org.neo4j.values.storable.Values.longValue;
import static org.neo4j.values.virtual.VirtualValues.EMPTY_LIST;
import static org.neo4j.values.virtual.VirtualValues.list;

class ListSliceTest
{
    @Test
    void shouldSliceList()
    {
        // Given
        ListValue inner = list( longValue( 5L ), longValue( 6L ), longValue( 7L ),
                longValue( 8L ), longValue( 9L ), longValue( 10L ), longValue( 11L ) );

        // When
        ListValue slice = inner.slice(2, 4 );

        // Then
        ListValue expected = list( longValue( 7L ), longValue( 8L ) );
        assertEquals( expected, slice );
        assertEquals( expected.hashCode(), slice.hashCode() );
        assertArrayEquals( expected.asArray(), slice.asArray() );
        assertTrue( iteratorsEqual(expected.iterator(), slice.iterator()) );
    }

    @Test
    void shouldReturnEmptyListIfEmptyRange()
    {
        // Given
        ListValue inner = list( longValue( 5L ), longValue( 6L ), longValue( 7L ),
                longValue( 8L ), longValue( 9L ), longValue( 10L ), longValue( 11L ) );

        // When
        ListValue slice = inner.slice( 4, 2 );

        // Then
        assertEquals( slice, EMPTY_LIST );
        assertTrue( iteratorsEqual(slice.iterator(), EMPTY_LIST.iterator()) );
    }

    @Test
    void shouldHandleExceedingRange()
    {
        // Given
        ListValue inner = list( longValue( 5L ), longValue( 6L ), longValue( 7L ),
                longValue( 8L ), longValue( 9L ), longValue( 10L ), longValue( 11L ) );

        // When
        ListValue slice = inner.slice( 2, 400000 );

        // Then
        ListValue expected =
                list( longValue( 7L ), longValue( 8L ), longValue( 9L ), longValue( 10L ), longValue( 11L ) );
        assertEquals( expected, slice );
        assertEquals( expected.hashCode(), slice.hashCode() );
        assertArrayEquals( expected.asArray(), slice.asArray() );
        assertTrue( iteratorsEqual(expected.iterator(), slice.iterator()) );
    }

    @Test
    void shouldHandleNegativeStart()
    {
        // Given
        ListValue inner = list( longValue( 5L ), longValue( 6L ), longValue( 7L ),
                longValue( 8L ), longValue( 9L ), longValue( 10L ), longValue( 11L ) );

        // When
        ListValue slice = inner.slice( -2, 400000 );

        // Then
        assertEquals( inner, slice );
        assertEquals( inner.hashCode(), slice.hashCode() );
        assertArrayEquals( inner.asArray(), slice.asArray() );
        assertTrue( iteratorsEqual(inner.iterator(), slice.iterator()) );
    }

    @Test
    void shouldBeAbleToDropFromList()
    {
        // Given
        ListValue inner = list( longValue( 5L ), longValue( 6L ), longValue( 7L ),
                longValue( 8L ), longValue( 9L ), longValue( 10L ), longValue( 11L ) );

        // When
        ListValue drop = inner.drop( 4 );

        // Then
        ListValue expected = list( longValue( 9L ), longValue( 10L ), longValue( 11L ) );
        assertEquals( expected, drop );
        assertEquals( expected.hashCode(), drop.hashCode() );
        assertArrayEquals( expected.asArray(), drop.asArray() );
        assertTrue( iteratorsEqual(expected.iterator(), drop.iterator()) );
    }

    @Test
    void shouldBeAbleToTakeFromList()
    {
        // Given
        ListValue inner = list( longValue( 5L ), longValue( 6L ), longValue( 7L ),
                longValue( 8L ), longValue( 9L ), longValue( 10L ), longValue( 11L ) );

        // When
        ListValue take = inner.take( 3 );

        // Then
        ListValue expected = list( longValue( 5L ), longValue( 6L ), longValue( 7L ) );
        assertEquals( expected, take );
        assertEquals( expected.hashCode(), take.hashCode() );
        assertArrayEquals( expected.asArray(), take.asArray() );
        assertTrue( iteratorsEqual(expected.iterator(), take.iterator()) );
    }
}
