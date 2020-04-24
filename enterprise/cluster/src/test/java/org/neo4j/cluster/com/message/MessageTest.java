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
package org.neo4j.cluster.com.message;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MessageTest
{
    @Test
    public void respondingToInternalMessageShouldProduceCorrectMessage()
    {
        // Given
        final Object payload = new Object();
        final MessageType type = mock(MessageType.class);
        Message message = Message.internal( type, payload );

        // When
        Message response = Message.respond( type, message, payload );

        // Then
        assertTrue( response.isInternal() );
        assertEquals( payload, response.getPayload() );
        assertEquals( type, response.getMessageType() );
    }

    @Test
    public void respondingToExternalMessageShouldProperlySetToHeaders()
    {
        // Given
        final Object payload = new Object();
        final MessageType type = mock(MessageType.class);
        URI to = URI.create( "cluster://to" );
        URI from = URI.create( "cluster://from" );
        Message incoming = Message.to( type, to, payload );
        incoming.setHeader( Message.HEADER_FROM, from.toString() );

        // When
        Message response = Message.respond( type, incoming, payload );

        // Then
        assertFalse( response.isInternal() );
        assertEquals( from.toString(), response.getHeader( Message.HEADER_TO ) );
        assertEquals( payload, response.getPayload() );
        assertEquals( type, response.getMessageType() );
    }
}
