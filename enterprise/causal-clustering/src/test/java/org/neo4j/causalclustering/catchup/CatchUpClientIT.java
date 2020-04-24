/*
 * Copyright (c) 2002-2018 "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
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
package org.neo4j.causalclustering.catchup;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.channels.ClosedChannelException;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.neo4j.causalclustering.catchup.storecopy.GetStoreIdRequest;
import org.neo4j.causalclustering.net.Server;
import org.neo4j.helpers.AdvertisedSocketAddress;
import org.neo4j.helpers.Exceptions;
import org.neo4j.helpers.ListenSocketAddress;
import org.neo4j.kernel.lifecycle.LifeSupport;
import org.neo4j.kernel.lifecycle.LifecycleException;
import org.neo4j.logging.NullLogProvider;
import org.neo4j.ports.allocation.PortAuthority;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatchUpClientIT
{

    private LifeSupport lifeSupport;
    private int inactivityTimeoutMillis = 10000;

    @BeforeEach
    void initLifeCycles()
    {
        lifeSupport = new LifeSupport();
    }

    @AfterEach
    void shutdownLifeSupport()
    {
        lifeSupport.stop();
        lifeSupport.shutdown();
    }

    @Test
    void shouldCloseHandlerIfChannelIsClosedInClient() throws LifecycleException
    {
        // given
        String hostname = "localhost";
        int port = PortAuthority.allocatePort();
        ListenSocketAddress listenSocketAddress = new ListenSocketAddress( hostname, port );
        AtomicBoolean wasClosedByClient = new AtomicBoolean( false );

        Server emptyServer = catchupServer( listenSocketAddress );
        CatchUpClient closingClient = closingChannelCatchupClient( wasClosedByClient );

        lifeSupport.add( emptyServer );
        lifeSupport.add( closingClient );

        // when
        lifeSupport.init();
        lifeSupport.start();

        // then
        assertClosedChannelException( hostname, port, closingClient );
        assertTrue( wasClosedByClient.get() );
    }

    @Test
    void shouldCloseHandlerIfChannelIsClosedOnServer()
    {
        // given
        String hostname = "localhost";
        int port = PortAuthority.allocatePort();
        ListenSocketAddress listenSocketAddress = new ListenSocketAddress( hostname, port );
        AtomicBoolean wasClosedByServer = new AtomicBoolean( false );

        Server closingChannelServer = closingChannelCatchupServer( listenSocketAddress, wasClosedByServer );
        CatchUpClient emptyClient = emptyClient();

        lifeSupport.add( closingChannelServer );
        lifeSupport.add( emptyClient );

        // when
        lifeSupport.init();
        lifeSupport.start();

        // then
        CatchUpClientException catchUpClientException = assertThrows( CatchUpClientException.class,
                () -> emptyClient.makeBlockingRequest( new AdvertisedSocketAddress( hostname, port ), new GetStoreIdRequest(), neverCompletingAdaptor() ) );
        assertEquals( ClosedChannelException.class, Exceptions.rootCause( catchUpClientException ).getClass() );
        assertTrue( wasClosedByServer.get() );
    }

    @Test
    void shouldTimeoutDueToInactivity()
    {
        // given
        String hostname = "localhost";
        int port = PortAuthority.allocatePort();
        ListenSocketAddress listenSocketAddress = new ListenSocketAddress( hostname, port );
        inactivityTimeoutMillis = 0;

        Server closingChannelServer = catchupServer( listenSocketAddress );
        CatchUpClient emptyClient = emptyClient();

        lifeSupport.add( closingChannelServer );
        lifeSupport.add( emptyClient );

        // when
        lifeSupport.init();
        lifeSupport.start();

        // then
        CatchUpClientException catchUpClientException = assertThrows( CatchUpClientException.class,
                () -> emptyClient.makeBlockingRequest( new AdvertisedSocketAddress( hostname, port ), new GetStoreIdRequest(), neverCompletingAdaptor() ) );
        assertEquals( TimeoutException.class, Exceptions.rootCause( catchUpClientException ).getClass() );
    }

    private CatchUpClient emptyClient()
    {
        return catchupClient( new MessageToByteEncoder<GetStoreIdRequest>()
        {
            @Override
            protected void encode( ChannelHandlerContext channelHandlerContext, GetStoreIdRequest getStoreIdRequest, ByteBuf byteBuf )
            {
                byteBuf.writeByte( (byte) 1 );
            }
        } );
    }

    private void assertClosedChannelException( String hostname, int port, CatchUpClient closingClient )
    {
        try
        {
            closingClient.makeBlockingRequest( new AdvertisedSocketAddress( hostname, port ), new GetStoreIdRequest(), neverCompletingAdaptor() );
            fail();
        }
        catch ( CatchUpClientException e )
        {
            Throwable cause = e.getCause();
            assertEquals( cause.getClass(), ExecutionException.class );
            Throwable actualCause = cause.getCause();
            assertEquals( actualCause.getClass(), ClosedChannelException.class );
        }
    }

    private CatchUpResponseAdaptor<Object> neverCompletingAdaptor()
    {
        return new CatchUpResponseAdaptor<>();
    }

    private CatchUpClient closingChannelCatchupClient( AtomicBoolean wasClosedByClient )
    {
        return catchupClient( new MessageToByteEncoder()
        {
            @Override
            protected void encode( ChannelHandlerContext ctx, Object msg, ByteBuf out )
            {
                wasClosedByClient.set( true );
                ctx.channel().close();
            }
        } );
    }

    private Server closingChannelCatchupServer( ListenSocketAddress listenSocketAddress, AtomicBoolean wasClosedByServer )
    {
        return catchupServer( listenSocketAddress, new ByteToMessageDecoder()
        {
            @Override
            protected void decode( ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list )
            {
                wasClosedByServer.set( true );
                ctx.channel().close();
            }
        } );
    }

    private CatchUpClient catchupClient( ChannelHandler... channelHandlers )
    {
        return new CatchUpClient( NullLogProvider.getInstance(), Clock.systemUTC(), inactivityTimeoutMillis,
                catchUpResponseHandler -> new ChannelInitializer<SocketChannel>()
        {
            @Override
            protected void initChannel( SocketChannel ch )
            {
                ch.pipeline().addLast( channelHandlers );
            }
        } );
    }

    private Server catchupServer( ListenSocketAddress listenSocketAddress, ChannelHandler... channelHandlers )
    {
        return new Server( channel -> channel.pipeline().addLast( channelHandlers ), listenSocketAddress, "empty-test-server" );
    }
}
