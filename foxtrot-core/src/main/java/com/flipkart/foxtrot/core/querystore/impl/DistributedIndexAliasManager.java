package com.flipkart.foxtrot.core.querystore.impl;

import com.codahale.metrics.Timer;
import com.flipkart.foxtrot.core.cache.impl.DistributedCache;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.exception.FoxtrotExceptions;
import com.flipkart.foxtrot.core.exception.IndexCreationException;
import com.flipkart.foxtrot.core.querystore.IndexAliasManager;
import com.flipkart.foxtrot.core.util.MetricUtil;
import com.google.common.collect.Lists;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class DistributedIndexAliasManager implements IndexAliasManager {
    private static final Logger logger = LoggerFactory.getLogger(DistributedIndexAliasManager.class);
    private static final String ALIAS_MAP = "indexaliasmap";
    private final HazelcastConnection hazelcastConnection;
    private final ElasticsearchConnection elasticsearchConnection;
    private HazelcastInstance hazelcastInstance;
    private IMap<String, String> indexAliasStore;

    public DistributedIndexAliasManager(HazelcastConnection hazelcastConnection,
                                        ElasticsearchConnection elasticsearchConnection) {
        this.hazelcastConnection = hazelcastConnection;
        this.elasticsearchConnection = elasticsearchConnection;
        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setFactoryImplementation(IndexAliasMapStore.factory(elasticsearchConnection));
        mapStoreConfig.setEnabled(true);
        mapStoreConfig.setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER);
        MapConfig mapConfig = new MapConfig(ALIAS_MAP);
        mapConfig.setMapStoreConfig(mapStoreConfig);
        mapConfig.setReadBackupData(true);
        hazelcastConnection.getHazelcastConfig().addMapConfig(mapConfig);
        DistributedCache.setupConfig(hazelcastConnection);
    }

    @Override
    public void saveAlias(String aliasName) throws FoxtrotException {
        ILock lock = hazelcastInstance.getLock(aliasName);
        lock.lock();
        try {
            if (!aliasExists(aliasName)) {
                logger.info("Saving alias {}", aliasName);
                createIndexAndAlias(aliasName);
                indexAliasStore.put(aliasName, aliasName);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean aliasExists(String aliasName) throws FoxtrotException {
        return indexAliasStore.containsKey(aliasName);
    }

    @Override
    public List<String> getAllAliases() throws FoxtrotException {
        if (0 == indexAliasStore.size()) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(indexAliasStore.values());
    }

    @Override
    public void start() throws Exception {
        hazelcastInstance = hazelcastConnection.getHazelcast();
        indexAliasStore = hazelcastInstance.getMap(ALIAS_MAP);
    }

    @Override
    public void stop() throws Exception {

    }

    /**
     * This function creates the index and alias corresponding to it
     * @param aliasName Alias name for the index
     */
    private void createIndexAndAlias(String aliasName) throws IndexCreationException {
        logger.info("Creating index and alias for aliasName {}", aliasName);
        String indexName = ElasticsearchUtils.getIndexFromAlias(aliasName);
        Timer.Context timer = MetricUtil.getInstance().startTimer(DistributedIndexAliasManager.class, "indexAliasCreation");
        try {
            CreateIndexResponse response = elasticsearchConnection.getClient()
                    .admin()
                    .indices()
                    .create(new CreateIndexRequest(indexName).alias(new Alias(aliasName)))
                    .actionGet();
            if (!response.isAcknowledged()) {
                logger.error("Error creating index {} with alias {}", indexName, aliasName);
                throw FoxtrotExceptions.createIndexCreationException(indexName, response.toString());
            }
            logger.info("Index {} created with alias {}", indexName, aliasName);
        } catch (FoxtrotException e) {
            throw e;
        } catch (ResourceAlreadyExistsException e) {
            // should never come here
            MetricUtil.getInstance().markMeter(DistributedIndexAliasManager.class, "indexExistsException");
            logger.error("Index {} already exists with alias {}", indexName, aliasName);
        } finally {
            timer.stop();
        }
    }
}
