/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.foxtrot.core.querystore.impl;

import io.dropwizard.lifecycle.Managed;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com)
 * Date: 14/03/14
 * Time: 12:38 AM
 */
public class ElasticsearchConnection implements Managed {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConnection.class.getSimpleName());
    private final ElasticsearchConfig config;
    private Client client;

    public ElasticsearchConnection(ElasticsearchConfig config) {
        this.config = config;
    }

    @Override
    public void start() throws Exception {
        logger.info("Starting Elasticsearch Client");
        Settings settings = Settings.builder()
                .put("cluster.name", config.getCluster())
                .put("transport.type", "netty3")
                .put("http.type", "netty3")
                .build();

        TransportClient esClient = new PreBuiltTransportClient(settings);
        for (String host : config.getHosts()) {
            String tokenizedHosts[] = host.split(",");
            for (String tokenizedHost : tokenizedHosts) {
                esClient.addTransportAddress(
                        new InetSocketTransportAddress(InetAddress.getByName(tokenizedHost), 9300));
                logger.info(String.format("Added Elasticsearch Node : %s", host));
            }
        }
        client = esClient;
        logger.info("Started Elasticsearch Client");
    }

    @Override
    public void stop() throws Exception {
        if (client != null) {
            client.close();
        }
        client = null;
    }

    public Client getClient() {
        return client;
    }

    public ElasticsearchConfig getConfig() {
        return config;
    }
}
