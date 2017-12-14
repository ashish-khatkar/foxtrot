package com.flipkart.foxtrot.core.querystore.impl;

import com.flipkart.foxtrot.core.cache.impl.DistributedCache;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.querystore.IndexAliasManager;
import com.google.common.collect.Lists;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.IMap;
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
    private IMap<String, String> indexAliasStore;

    public DistributedIndexAliasManager(HazelcastConnection hazelcastConnection,
                                        ElasticsearchConnection elasticsearchConnection) {
        this.hazelcastConnection = hazelcastConnection;
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
    public void save(String aliasName) throws FoxtrotException {
        logger.info("Saving alias {}", aliasName);
        indexAliasStore.put(aliasName, aliasName);
        indexAliasStore.flush();
    }

    @Override
    public boolean exists(String aliasName) throws FoxtrotException {
        return indexAliasStore.containsKey(aliasName);
    }

    @Override
    public List<String> get() throws FoxtrotException {
        if (0 == indexAliasStore.size()) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(indexAliasStore.values());
    }

    @Override
    public void start() throws Exception {
        indexAliasStore = hazelcastConnection.getHazelcast().getMap(ALIAS_MAP);
    }

    @Override
    public void stop() throws Exception {

    }
}
