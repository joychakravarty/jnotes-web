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
package com.jc.jnotesweb.service.cassandra;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.shaded.guava.common.net.HostAndPort;
import com.jc.jnotesweb.util.EncryptionUtil;

import lombok.Setter;

@Configuration
@ConfigurationProperties("cassandra")
public class CassandraSessionManager {

    @Value("classpath:cassandra/secure-connect-jnotes.zip")
    private Resource securityBundle;

    @Setter
    private String keyspace;
    
    @Setter
    private String clientUserName;

    @Setter
    private String clientPassword;

    @Setter
    private String hostname;

    @Setter
    private int port;

    @Setter
    private String datacenter;

    private CqlSession clientCqlSession;

    @Bean(name = "dbProperties")
    public String getDBProperties() {
        String dbProperties = String.format("securityBundle:[%s] keyspace:[%s] clientUserName:[%s] hostname:[%s] port:[%d] datacenter:[%s]",
                securityBundle.getFilename(), keyspace, clientUserName, hostname, port, datacenter);
        return dbProperties;
    }
    
    @Bean(name = "keyspace")
    public String getKeyspace() {
        return keyspace;
    }

    protected void closeClientSession() {
        if (clientCqlSession != null) {
            clientCqlSession.close();
            clientCqlSession = null;
        }
    }

    @Profile("local")
    @Bean
    public CqlSession getLocalClientSession() {
        if (clientCqlSession == null) {
            HostAndPort parsed = HostAndPort.fromString(hostname);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(parsed.getHost(), parsed.getPortOrDefault(port));
            clientCqlSession = CqlSession.builder().addContactPoint(inetSocketAddress).withLocalDatacenter(datacenter)
                    .withAuthCredentials(clientUserName, EncryptionUtil.locallyDecrypt(clientPassword)).build();

        }
        return clientCqlSession;
    }

    @Profile("production")
    @Bean
    public CqlSession getClientSession() {
        if (clientCqlSession == null) {

            try {
                clientCqlSession = CqlSession.builder().withCloudSecureConnectBundle(securityBundle.getInputStream()).withKeyspace(keyspace)
                        .withAuthCredentials(clientUserName, EncryptionUtil.locallyDecrypt(clientPassword)).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return clientCqlSession;
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("CassandraSessionManager: Session cleanup");
        closeClientSession();
    }

}
