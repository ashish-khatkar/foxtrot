package com.flipkart.foxtrot.core.querystore.impl;

import com.flipkart.foxtrot.core.util.MetricUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.codahale.metrics.Timer;

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

    public IndexAliasMapStore(ElasticsearchConnection elasticsearchConnection) {
        this.elasticsearchConnection = elasticsearchConnection;
    }

    @Override
    public void store(String key, String value) {

    }

    @Override
    public void storeAll(Map<String, String> map) {

    }

    @Override
    public void delete(String key) {

    }

    @Override
    public void deleteAll(Collection<String> keys) {

    }

    /**
     *
     * Load function is called when hazelcast can't find value corresponding to key and it tries to load it by calling load.
     * This also gets called while we check containsKey on hazelcast map.
     * Here we don't need to do anything as we are creating index and aliases by ourselves. We don't want hazelcast to do that for us.
     * So just pass null from here.
     *
     */
    @Override
    public String load(String key) {
        logger.info("Load called for alias : {}", key);
        return null;
    }

    /**
     *
     * LoadAll function is called at the init for hazelcast. It calls loadKeys which fetches all the existing aliases from the ES.
     * Then hazelcast calls loadAll on those keys. Since for the corresponding keys aliases already exists we just convert the key array to map.
     *
     */
    @Override
    public Map<String, String> loadAll(Collection<String> keys) {
        logger.info("Load all called for alias map");
        Map<String, String> aliases = Maps.newHashMap();
        for (String key : keys) {
            aliases.put(key, key);
        }
        logger.info("Loaded value count {}", aliases.size());
        return aliases;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        logger.info("Load all keys called for aliases");
        Set<String> aliases = null;
        try {
            aliases = getAllAliases();
            logger.info("Loaded aliases. No of aliases : {}", aliases.size());
        } catch (Exception e) {
            MetricUtil.getInstance().markMeter(IndexAliasMapStore.class, "hazelcastInitFail");
            logger.error("Hazelcast map initialization failed. Exiting");
            System.exit(1);
        }
        return aliases;
    }

    /**
     * This function fetches all the existing aliases in th ES and returns them
     *
     * @return Set of aliases
     */
    private Set<String> getAllAliases() throws Exception {
        Set<String> aliases = Sets.newHashSet();
        Timer.Context timer = MetricUtil.getInstance().startTimer(IndexAliasMapStore.class, "getAllAliases");
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
            throw new Exception("Error getting aliases from ES.", e.getCause());
        } finally {
            timer.stop();
        }
        return aliases;
    }

}
