/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.kernel.impl.api.index;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.index.schema.BlockBasedIndexPopulator;
import org.neo4j.kernel.impl.index.schema.GenericNativeIndexPopulator;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.monitoring.Monitors;
import org.neo4j.test.extension.DbmsExtension;
import org.neo4j.test.extension.Inject;
import org.neo4j.util.FeatureToggles;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.neo4j.graphdb.Label.label;
import static org.neo4j.io.ByteUnit.kibiBytes;
import static org.neo4j.kernel.impl.index.schema.BlockBasedIndexPopulator.BLOCK_SIZE_NAME;
import static org.neo4j.kernel.impl.index.schema.GenericNativeIndexProvider.BLOCK_BASED_POPULATION_NAME;

@DbmsExtension
class BlockBasedIndexPopulationMemoryUsageIT
{
    private static final long TEST_BLOCK_SIZE = kibiBytes( 64 );
    private static final String[] KEYS = {"key1", "key2", "key3", "key4"};
    private static final Label[] LABELS = {label( "Label1" ), label( "Label2" ), label( "Label3" ), label( "Label4" )};

    @Inject
    private GraphDatabaseAPI db;

    @BeforeAll
    static void setUpFeatureToggles()
    {
        // Configure populator so that it will use block-based population and reduce batch size and increase number of workers
        // so that population will very likely create more batches in more threads (affecting number of buffers used)
        FeatureToggles.set( GenericNativeIndexPopulator.class, BLOCK_BASED_POPULATION_NAME, true );
        FeatureToggles.set( BlockBasedIndexPopulator.class, BLOCK_SIZE_NAME, TEST_BLOCK_SIZE );
    }

    @AfterAll
    static void restoreFeatureToggles()
    {
        FeatureToggles.clear( GenericNativeIndexPopulator.class, BLOCK_BASED_POPULATION_NAME );
        FeatureToggles.clear( BlockBasedIndexPopulator.class, BLOCK_SIZE_NAME );
    }

    @Test
    void shouldKeepMemoryConsumptionLowDuringPopulation() throws InterruptedException
    {
        // given
        IndexPopulationMemoryUsageMonitor monitor = new IndexPopulationMemoryUsageMonitor();
        db.getDependencyResolver().resolveDependency( Monitors.class ).addMonitorListener( monitor );
        someData();

        // when
        createLotsOfIndexesInOneTransaction();
        monitor.called.await();

        // then all in all the peak memory usage with the introduction of the more sophisticated ByteBufferFactory
        // given all parameters of data size, number of workers and number of indexes will amount
        // to a maximum of 10 MiB. Previously this would easily be 10-fold of that for this scenario.
        long targetMemoryConsumption = TEST_BLOCK_SIZE * (8 /*mergeFactor*/ + 1 /*write buffer*/) * 8 /*numberOfWorkers*/;
        assertThat( monitor.peakDirectMemoryUsage, lessThan( targetMemoryConsumption * 2 + 1 ) );
    }

    private void createLotsOfIndexesInOneTransaction()
    {
        try ( Transaction tx = db.beginTx() )
        {
            var schema = tx.schema();
            for ( Label label : LABELS )
            {
                for ( String key : KEYS )
                {
                    schema.indexFor( label ).on( key ).create();
                }
            }
            tx.commit();
        }
        while ( true )
        {
            try ( Transaction tx = db.beginTx() )
            {
                tx.schema().awaitIndexesOnline( 1, SECONDS );
                break;
            }
            catch ( IllegalStateException e )
            {
                // Just wait longer
                try
                {
                    Thread.sleep( 100 );
                }
                catch ( InterruptedException e1 )
                {
                    // Not sure we can do anything about this, other than just break this loop
                    break;
                }
            }
        }
    }

    private void someData() throws InterruptedException
    {
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool( threads );
        for ( int i = 0; i < threads; i++ )
        {
            executor.submit( () ->
            {
                for ( int t = 0; t < 100; t++ )
                {
                    try ( Transaction tx = db.beginTx() )
                    {
                        for ( int n = 0; n < 100; n++ )
                        {
                            Node node = tx.createNode( LABELS );
                            for ( String key : KEYS )
                            {
                                node.setProperty( key, format( "some value %d", n ) );
                            }
                        }
                        tx.commit();
                    }
                }
            } );
        }
        executor.shutdown();
        while ( !executor.awaitTermination( 1, SECONDS ) )
        {
            // Just wait longer
        }
    }

    private static class IndexPopulationMemoryUsageMonitor extends IndexingService.MonitorAdapter
    {
        private volatile long peakDirectMemoryUsage;
        private final CountDownLatch called = new CountDownLatch( 1 );

        @Override
        public void populationJobCompleted( long peakDirectMemoryUsage )
        {
            this.peakDirectMemoryUsage = peakDirectMemoryUsage;
            // We need a count on this one because index will come online slightly before we get a call to this method
            called.countDown();
        }
    }
}
