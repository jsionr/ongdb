/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2018 "Neo4j,"
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
package org.neo4j.metrics.output;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.EnterpriseGraphDatabaseFactory;
import org.neo4j.kernel.configuration.Settings;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.test.rule.TestDirectory;

import static org.junit.Assert.assertTrue;
import static org.neo4j.metrics.MetricsSettings.prometheusEnabled;
import static org.neo4j.metrics.MetricsSettings.prometheusEndpoint;
import static org.neo4j.metrics.source.db.EntityCountMetrics.COUNTS_NODE;
import static org.neo4j.metrics.source.db.EntityCountMetrics.COUNTS_RELATIONSHIP_TYPE;
import static org.neo4j.test.PortUtils.getConnectorAddress;

public class PrometheusOutputIT
{
    @Rule
    public TestDirectory testDirectory = TestDirectory.testDirectory();

    private GraphDatabaseService database;

    @Before
    public void setUp()
    {
        database = new EnterpriseGraphDatabaseFactory().newEmbeddedDatabaseBuilder( testDirectory.storeDir() )
                .setConfig( prometheusEnabled, Settings.TRUE )
                .setConfig( prometheusEndpoint, "localhost:0" )
                .newGraphDatabase();
    }

    @After
    public void tearDown()
    {
        database.shutdown();
    }

    @Test
    public void httpEndpointShouldBeAvailableAndResponsive() throws IOException
    {
        String url = "http://" + getConnectorAddress( (GraphDatabaseAPI) database, "prometheus" ) + "/metrics";
        URLConnection connection = new URL( url ).openConnection();
        connection.setDoOutput( true );
        connection.connect();
        Scanner s = new Scanner( connection.getInputStream(), "UTF-8" ).useDelimiter( "\\A" );

        assertTrue( s.hasNext() );
        String response = s.next();
        assertTrue( response.contains( COUNTS_NODE ) );
        assertTrue( response.contains( COUNTS_RELATIONSHIP_TYPE ) );
    }
}
