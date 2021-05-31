/*
 * This file is part of JNotes. Copyright (C) 2020  Joy Chakravarty
 *
 * JNotes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNotes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */
package com.jc.jnotesweb.repository;

import java.security.NoSuchAlgorithmException;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.JdkSSLOptions;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties("cassandra")
public class CassandraSessionManager {
    private static final Logger log = LoggerFactory.getLogger(CassandraSessionManager.class);
    private String keyspace;

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String username;

    private String password;

    private String hostname;

    private int port;

    private Session session;
    private Cluster cluster;

    @Bean(name = "dbProperties")
    public String getDBProperties() {
        String dbProperties = String.format("keyspace:[%s] username:[%s] hostname:[%s] port:[%d]", keyspace, username, hostname, port);
        return dbProperties;
    }

    @Bean(name = "keyspace")
    public String getKeyspace() {
        return keyspace;
    }

    protected void closeSession() {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    @Profile("local")
    @Bean
    public Session getLocalClientSession() {
        if (session == null) {
            try {
                this.createSession();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

        }
        return session;
    }

    @Profile("production")
    @Bean
    public Session getClientSession() {
        if (session == null) {

            try {
//                clientCqlSession = CqlSession.builder().
//                        withCloudSecureConnectBundle(securityBundle.getInputStream()).withKeyspace(keyspace)
//                        .withAuthCredentials(clientUserName, EncryptionUtil.locallyDecrypt(clientPassword)).build();

                this.createSession();

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return session;
    }

    protected void createSession() throws NoSuchAlgorithmException {
        JdkSSLOptions sslOptions = RemoteEndpointAwareJdkSSLOptions.builder()
                .withSSLContext(SSLContext.getDefault())
                .build();
        log.info("keyspace:[{}] username:[{}] hostname:[{}] port:[{}]", keyspace, username, hostname, port);
        Cluster cluster = Cluster.builder()
                .addContactPoint(hostname)
                .withPort(port)
                .withCredentials(username, password)
                .withSSL(sslOptions)
                .build();
        this.cluster = cluster;
        this.session = cluster.connect();
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("CassandraSessionManager: Session cleanup");
        if(cluster!=null) {
            cluster.close();
        }
    }

}
