/*
 * Copyright 2010 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.projectodd.stilts.stomp.protocol.websocket.ietf00;

import org.jboss.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.projectodd.stilts.stomp.protocol.websocket.WebSocketFrame;
import org.projectodd.stilts.stomp.protocol.websocket.WebSocketFrame.FrameType;

/**
 * Encodes a {@link WebSocketFrame} into a {@link ChannelBuffer}.
 * <p>
 * For the detailed instruction on adding add Web Socket support to your HTTP
 * server, take a look into the <tt>WebSocketServer</tt> example located in the
 * {@code org.jboss.netty.example.http.websocket} package.
 * 
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Mike Heath (mheath@apache.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2243 $, $Date: 2010-04-16 14:01:55 +0900 (Fri, 16 Apr 2010) $
 * 
 * @apiviz.landmark
 * @apiviz.uses org.jboss.netty.handler.codec.http.websocket.WebSocketFrame
 */
@Sharable
public class Ietf00WebSocketFrameEncoder extends OneToOneEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) msg;

            FrameType frameType = frame.getType();

            switch (frameType) {
            case CLOSE: {
                log.trace(  "encode close"  );
                ChannelBuffer encoded = channel.getConfig().getBufferFactory().getBuffer( 2 );
                encoded.writeByte( 0x80 );
                encoded.writeByte( 0xFF );
                return encoded;
            }
            case TEXT: {
                log.trace(  "encode text"  );
                ChannelBuffer data = frame.getBinaryData();
                ChannelBuffer encoded = channel.getConfig().getBufferFactory().getBuffer( data.order(), data.readableBytes() + 2 );
                encoded.writeByte( 0x00 );
                encoded.writeBytes( data, data.readableBytes() );
                encoded.writeByte( (byte) 0xFF );
                return encoded;
            }
            case BINARY: {
                log.trace(  "encode binary"  );
                ChannelBuffer data = frame.getBinaryData();
                int dataLen = data.readableBytes();
                ChannelBuffer encoded = channel.getConfig().getBufferFactory().getBuffer( data.order(), dataLen + 5 );
                encoded.writeByte( (byte) 0x80 );
                encoded.writeByte( (byte) (dataLen >>> 28 & 0x7F | 0x80) );
                encoded.writeByte( (byte) (dataLen >>> 14 & 0x7F | 0x80) );
                encoded.writeByte( (byte) (dataLen >>> 7 & 0x7F | 0x80) );
                encoded.writeByte( (byte) (dataLen & 0x7F) );
                encoded.writeBytes( data, dataLen );
                return encoded;
            }
            default:
                break;
            }
        }
        return msg;
    }
            
    private static final Logger log = Logger.getLogger( "stilts.websockets.ietf00.encoder" );
}
