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
package org.neo4j.server.rest.transactional;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.neo4j.internal.kernel.api.Transaction.Type;
import org.neo4j.internal.kernel.api.security.LoginContext;
import org.neo4j.kernel.GraphDatabaseQueryService;
import org.neo4j.kernel.impl.core.ThreadToStatementContextBridge;
import org.neo4j.kernel.impl.coreapi.InternalTransaction;
import org.neo4j.kernel.impl.coreapi.PropertyContainerLocker;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.query.Neo4jTransactionalContextFactory;
import org.neo4j.kernel.impl.query.TransactionalContext;
import org.neo4j.kernel.impl.query.TransactionalContextFactory;
import org.neo4j.kernel.impl.query.clientconnection.ClientConnectionInfo;
import org.neo4j.kernel.impl.util.ValueUtils;
import org.neo4j.server.rest.web.HttpConnectionInfoFactory;

public class TransitionalPeriodTransactionMessContainer
{
    private static final PropertyContainerLocker locker = new PropertyContainerLocker();

    private final GraphDatabaseFacade db;
    private final ThreadToStatementContextBridge txBridge;

    public TransitionalPeriodTransactionMessContainer( GraphDatabaseFacade db )
    {
        this.db = db;
        this.txBridge = db.getDependencyResolver().resolveDependency( ThreadToStatementContextBridge.class );
    }

    public TransitionalTxManagementKernelTransaction newTransaction( Type type, LoginContext loginContext,
            long customTransactionTimeout )
    {
        return new TransitionalTxManagementKernelTransaction( db, type, loginContext, customTransactionTimeout, txBridge );
    }

    ThreadToStatementContextBridge getBridge()
    {
        return txBridge;
    }

    public TransactionalContext create(
            HttpServletRequest request,
            GraphDatabaseQueryService service,
            Type type,
            LoginContext loginContext,
            String query,
            Map<String, Object> queryParameters )
    {
        TransactionalContextFactory contextFactory = Neo4jTransactionalContextFactory.create( service, locker );
        ClientConnectionInfo clientConnection = HttpConnectionInfoFactory.create( request );
        InternalTransaction transaction = service.beginTransaction( type, loginContext );
        return contextFactory.newContext( clientConnection, transaction, query, ValueUtils.asMapValue( queryParameters ) );
    }
}
