/*
 * Copyright 2011 Red Hat, Inc, and individual contributors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectodd.stilts.stomp.server;

import org.jboss.netty.buffer.ChannelBuffer;
import org.projectodd.stilts.stomp.DefaultStompMessage;
import org.projectodd.stilts.stomp.Headers;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.StompMessageFactory;

public class ServerStompMessageFactory implements StompMessageFactory {
    
    public final static ServerStompMessageFactory INSTANCE = new ServerStompMessageFactory();

    @Override
    public StompMessage createMessage(Headers headers, ChannelBuffer content, boolean isError) {
        return new DefaultStompMessage( headers, content, isError); 
    }

}
