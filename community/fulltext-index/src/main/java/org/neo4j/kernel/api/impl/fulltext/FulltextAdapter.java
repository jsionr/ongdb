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
package org.neo4j.kernel.api.impl.fulltext;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.neo4j.graphdb.index.fulltext.AnalyzerProvider;
import org.neo4j.internal.kernel.api.exceptions.schema.IndexNotFoundKernelException;
import org.neo4j.internal.kernel.api.schema.SchemaDescriptor;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.storageengine.api.EntityType;

public interface FulltextAdapter
{
    SchemaDescriptor schemaFor( EntityType type, String[] entityTokens, Properties indexConfiguration, String... properties );

    SchemaDescriptor schemaSortFor( EntityType type, String[] entityTokens, Properties indexConfiguration, String[] properties, String[] sortProperties,
                                    Map<String,String> sortTypes );

    ScoreEntityIterator query( KernelTransaction tx, String indexName, String queryString ) throws IOException, IndexNotFoundKernelException, ParseException;

    ScoreEntityIterator query( KernelTransaction tx, String indexName, String queryString, FulltextQueryConfig queryConfig )
            throws IOException, IndexNotFoundKernelException, ParseException;

    CountResult queryForCount( KernelTransaction ktx, String indexName, String queryString )
            throws IndexNotFoundKernelException, ParseException;

    void awaitRefresh();

    Stream<AnalyzerProvider> listAvailableAnalyzers();
}
