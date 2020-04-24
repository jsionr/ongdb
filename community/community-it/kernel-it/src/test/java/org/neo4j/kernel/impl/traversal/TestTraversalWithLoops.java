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
package org.neo4j.kernel.impl.traversal;

import org.junit.Test;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

public class TestTraversalWithLoops extends TraversalTestBase
{
    @Test
    public void traverseThroughNodeWithLoop()
    {
        /*
         * (a)-->(b)-->(c)-->(d)-->(e)
         *             /  \ /  \
         *             \__/ \__/
         */

        createGraph( "a TO b", "b TO c", "c TO c", "c TO d", "d TO d", "d TO e" );

        try ( Transaction tx = beginTx() )
        {
            Node a = getNodeWithName( "a" );
            final Node e = getNodeWithName( "e" );
            Evaluator onlyEndNode = path -> Evaluation.ofIncludes( path.endNode().equals( e ) );
            TraversalDescription basicTraverser = getGraphDb().traversalDescription().evaluator( onlyEndNode );
            expectPaths( basicTraverser.traverse( a ), "a,b,c,d,e" );
            expectPaths( basicTraverser.uniqueness( Uniqueness.RELATIONSHIP_PATH ).traverse( a ),
                    "a,b,c,d,e", "a,b,c,c,d,e", "a,b,c,d,d,e", "a,b,c,c,d,d,e" );
            tx.success();
        }
    }
}
