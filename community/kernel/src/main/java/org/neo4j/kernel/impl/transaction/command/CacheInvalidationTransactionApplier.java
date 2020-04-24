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
package org.neo4j.kernel.impl.transaction.command;

import org.neo4j.internal.kernel.api.NamedToken;
import org.neo4j.kernel.impl.api.TransactionApplier;
import org.neo4j.kernel.impl.core.CacheAccessBackDoor;
import org.neo4j.kernel.impl.store.LabelTokenStore;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.PropertyKeyTokenStore;
import org.neo4j.kernel.impl.store.RelationshipTypeTokenStore;
import org.neo4j.kernel.impl.transaction.command.Command.LabelTokenCommand;
import org.neo4j.kernel.impl.transaction.command.Command.PropertyKeyTokenCommand;
import org.neo4j.kernel.impl.transaction.command.Command.RelationshipTypeTokenCommand;

public class CacheInvalidationTransactionApplier extends TransactionApplier.Adapter
{
    private final CacheAccessBackDoor cacheAccess;
    private final RelationshipTypeTokenStore relationshipTypeTokenStore;
    private final LabelTokenStore labelTokenStore;
    private final PropertyKeyTokenStore propertyKeyTokenStore;

    public CacheInvalidationTransactionApplier( NeoStores neoStores,
                                                CacheAccessBackDoor cacheAccess )
    {
        this.cacheAccess = cacheAccess;
        this.relationshipTypeTokenStore = neoStores.getRelationshipTypeTokenStore();
        this.labelTokenStore = neoStores.getLabelTokenStore();
        this.propertyKeyTokenStore = neoStores.getPropertyKeyTokenStore();
    }

    @Override
    public boolean visitRelationshipTypeTokenCommand( RelationshipTypeTokenCommand command )
    {
        NamedToken type = relationshipTypeTokenStore.getToken( (int) command.getKey() );
        cacheAccess.addRelationshipTypeToken( type );

        return false;
    }

    @Override
    public boolean visitLabelTokenCommand( LabelTokenCommand command )
    {
        NamedToken labelId = labelTokenStore.getToken( (int) command.getKey() );
        cacheAccess.addLabelToken( labelId );

        return false;
    }

    @Override
    public boolean visitPropertyKeyTokenCommand( PropertyKeyTokenCommand command )
    {
        NamedToken index = propertyKeyTokenStore.getToken( (int) command.getKey() );
        cacheAccess.addPropertyKeyToken( index );

        return false;
    }

    @Override
    public void close()
    {
        // Nothing to close
    }
}
