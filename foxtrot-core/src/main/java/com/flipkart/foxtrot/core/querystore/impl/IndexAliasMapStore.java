package com.flipkart.foxtrot.core.querystore.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.foxtrot.core.exception.FoxtrotExceptions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.exists.AliasesExistResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class IndexAliasMapStore implements MapStore<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(IndexAliasMapStore.class.getSimpleName());

    public static class Factory implements MapStoreFactory<String, String> {
        private final ElasticsearchConnection elasticsearchConnection;

        public Factory(ElasticsearchConnection elasticsearchConnection) {
            this.elasticsearchConnection = elasticsearchConnection;
        }

        @Override
        public IndexAliasMapStore newMapStore(String mapName, Properties properties) {
            return new IndexAliasMapStore(elasticsearchConnection);
        }
    }

    public static Factory factory(ElasticsearchConnection elasticsearchConnection) {
        return new Factory(elasticsearchConnection);
    }

    private final ElasticsearchConnection elasticsearchConnection;
    private final ObjectMapper objectMapper;

    public IndexAliasMapStore(ElasticsearchConnection elasticsearchConnection) {
        this.elasticsearchConnection = elasticsearchConnection;
        this.objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Override
    public void store(String key, String value) {
        logger.info("Store called for alias {}", key);
        Set<String> existingAliases = getAllAliases();
        if (!existingAliases.contains(key)) {
            createIndexAndAlias(key);
        }
    }

    @Override
    public void storeAll(Map<String, String> map) {
        logger.info("Store all called for multiple aliases");
        Set<String> existingAliases = getAllAliases();
        for (String alias : map.keySet()) {
            if (!existingAliases.contains(alias)) {
                createIndexAndAlias(alias);
            }
        }
    }

    @Override
    public void delete(String key) {

    }

    @Override
    public void deleteAll(Collection<String> keys) {

    }

    @Override
    public String load(String key) {
        logger.info("Load called for alias : {}", key);
        try {
            AliasesExistResponse response = elasticsearchConnection.getClient()
                    .admin()
                    .indices()
                    .aliasesExist(new GetAliasesRequest(key))
                    .actionGet();
            if (response.exists()) {
                return key;
            }
        } catch (Exception e) {
            logger.info("Error in alias exists response {}", e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public Map<String, String> loadAll(Collection<String> keys) {
        logger.info("Load all called for alias map");
        Map<String, String> aliases = Maps.newHashMap();
        Set<String> existingAliases = getAllAliases();
        for (String key : keys) {
            if (existingAliases.contains(key)) {
                aliases.put(key, key);
            }
        }
        logger.info("Loaded value count {}", aliases.size());
        return aliases;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        logger.info("Load all keys called for aliases");
        Set<String> aliases = getAllAliases();
        logger.info("Loaded aliases. No of aliases : {}", aliases.size());
        return aliases;
    }

    private Set<String> getAllAliases() {
        Set<String> aliases = Sets.newHashSet();
        try {
            GetAliasesResponse response = elasticsearchConnection.getClient()
                    .admin()
                    .indices()
                    .getAliases(new GetAliasesRequest("_all"))
                    .get();
            Iterator<List<AliasMetaData>> aliasIterator = response.getAliases().valuesIt();
            while (aliasIterator.hasNext()) {
                List<AliasMetaData> aliasList = aliasIterator.next();
                for (AliasMetaData alias : aliasList) {
                    aliases.add(alias.getAlias());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting aliases from ES.", e.getCause());
        }
        return aliases;
    }

    private void createIndexAndAlias(String aliasName) {
        logger.info("Creating index and alias for aliasName {}", aliasName);
        String indexName = ElasticsearchUtils.getIndexFromAlias(aliasName);
        try {
            CreateIndexResponse response = elasticsearchConnection.getClient()
                    .admin()
                    .indices()
                    .create(new CreateIndexRequest(indexName).alias(new Alias(aliasName)))
                    .actionGet();
            if (!response.isAcknowledged()) {
                logger.error("Error creating index {} with alias {}", indexName, aliasName);
                //TODO: add create index exception here
            }
            logger.info("Index {} created with alias {}", indexName, aliasName);
        } catch (ResourceAlreadyExistsException e) {
            // should never come here
            logger.error("Error creating index {} with alias {}", indexName, aliasName);
            //TODO : add create index exception here
        }
    }
}
