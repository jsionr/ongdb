/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.consistency.checking.cache;

public interface CacheSlots
{
    int LABELS_SLOT_SIZE = 40;
    int ID_SLOT_SIZE = 40;

    interface NodeLabel
    {
        int SLOT_LABEL_FIELD = 0;
        int SLOT_IN_USE = 1;
    }

    interface NextRelationship
    {
        int SLOT_RELATIONSHIP_ID = 0;
        int SLOT_FIRST_IN_SOURCE = 1;
        int SLOT_FIRST_IN_TARGET = 2;
    }

    interface RelationshipLink
    {
        int SLOT_RELATIONSHIP_ID = 0;
        int SLOT_REFERENCE = 1;
        int SLOT_SOURCE_OR_TARGET = 2;
        int SLOT_PREV_OR_NEXT = 3;
        int SLOT_IN_USE = 4;
        long SOURCE = 0;
        long TARGET = -1;
        long PREV = 0;
        long NEXT = -1;
    }
}
