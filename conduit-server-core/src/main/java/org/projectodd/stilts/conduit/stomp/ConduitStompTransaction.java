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

package org.projectodd.stilts.conduit.stomp;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.projectodd.stilts.stomp.Acknowledger;
import org.projectodd.stilts.stomp.StompException;
import org.projectodd.stilts.stomp.StompMessage;
import org.projectodd.stilts.stomp.protocol.StompFrame.Header;
import org.projectodd.stilts.stomp.spi.StompTransaction;

public class ConduitStompTransaction implements StompTransaction {

    public ConduitStompTransaction(ConduitStompConnection clientAgent, Transaction transaction, String id) {
        this.stompConnection = clientAgent;
        this.transaction = transaction;
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public ConduitStompConnection getStompConnection() {
        return this.stompConnection;
    }

    public Transaction getJTATransaction() {
        return this.transaction;
    }

    @Override
    public void commit() throws StompException {
        try {
            log.debugf( "Committing transaction" );
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            tm.commit();
        } catch (SecurityException e) {
            throw new StompException( e );
        } catch (IllegalStateException e) {
            throw new StompException( e );
        } catch (RollbackException e) {
            throw new StompException( e );
        } catch (HeuristicMixedException e) {
            throw new StompException( e );
        } catch (HeuristicRollbackException e) {
            throw new StompException( e );
        } catch (SystemException e) {
            throw new StompException( e );
        } catch (InvalidTransactionException e) {
            throw new StompException( e );
        }
    }

    @Override
    public void abort() throws StompException {
        try {
            log.debugf( "Aborting transaction" );
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            tm.rollback();
        } catch (IllegalStateException e) {
            throw new StompException( e );
        } catch (SystemException e) {
            throw new StompException( e );
        } catch (InvalidTransactionException e) {
            throw new StompException( e );
        }
    }

    @Override
    public void send(StompMessage message) throws StompException {
        try {
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            message.getHeaders().remove( Header.TRANSACTION );
            this.stompConnection.send( message );
            tm.suspend();
        } catch (StompException e) {
            throw e;
        } catch (Exception e) {
            throw new StompException( e );
        }
    }

    public void ack(Acknowledger acknowledger) throws StompException {
        try {
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            acknowledger.ack();
            tm.suspend();
        } catch (Exception e) {
            throw new StompException( e );
        }
    }

    public void nack(Acknowledger acknowledger) throws StompException {
        try {
            TransactionManager tm = this.stompConnection.getStompProvider().getTransactionManager();
            tm.resume( this.transaction );
            acknowledger.nack();
            tm.suspend();
        } catch (Exception e) {
            throw new StompException( e );
        }
    }

    public String toString() {
        return "[" + getClass().getSimpleName() + ": " + id + "]";
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger( ConduitStompTransaction.class );

    private Transaction transaction;
    private ConduitStompConnection stompConnection;
    private String id;
}
