package com.flipkart.foxtrot.core.querystore.impl;

import com.flipkart.foxtrot.core.MockElasticsearchServer;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class DistributedIndexAliasManagerTest {
    private HazelcastInstance hazelcastInstance;
    private DistributedIndexAliasManager distributedIndexAliasManager;
    private MockElasticsearchServer elasticsearchServer;
    private IMap<String, String> indexAliasStore;

    @Before
    public void setup() throws Exception {
        HazelcastConnection hazelcastConnection = Mockito.mock(HazelcastConnection.class);
        hazelcastInstance = new TestHazelcastInstanceFactory(1).newHazelcastInstance();

        when(hazelcastConnection.getHazelcast()).thenReturn(hazelcastInstance);

        Config config = new Config();
        when(hazelcastConnection.getHazelcastConfig()).thenReturn(config);


        elasticsearchServer = new MockElasticsearchServer(UUID.randomUUID().toString());

        ElasticsearchConnection elasticsearchConnection = Mockito.mock(ElasticsearchConnection.class);
        when(elasticsearchConnection.getClient()).thenReturn(elasticsearchServer.getClient());

        String ALIAS_MAP = "indexaliasmap";
        indexAliasStore = hazelcastInstance.getMap(ALIAS_MAP);
        distributedIndexAliasManager = new DistributedIndexAliasManager(hazelcastConnection, elasticsearchConnection);
        distributedIndexAliasManager.start();
    }

    @After
    public void tearDown() throws Exception {
        hazelcastInstance.shutdown();
        elasticsearchServer.shutdown();
        distributedIndexAliasManager.stop();
    }

    @Test
    public void testSave() throws Exception {
        String alias = "test-alias";
        distributedIndexAliasManager.save(alias);
        assertTrue(distributedIndexAliasManager.exists(alias));
    }

    @Test
    public void testExists() throws Exception {
        String alias = "test-alias";
        String missingAlias = "missing-alias";
        distributedIndexAliasManager.save(alias);
        assertTrue(distributedIndexAliasManager.exists(alias));
        assertFalse(distributedIndexAliasManager.exists(missingAlias));
    }

    @Test
    public void testGet() throws Exception {
        distributedIndexAliasManager.save("test-alias1");
        distributedIndexAliasManager.save("test-alias2");
        List<String> aliases = distributedIndexAliasManager.get();
        assertTrue(aliases.contains("test-alias1"));
        assertTrue(aliases.contains("test-alias2"));
        assertEquals(aliases.size(), 2);
    }
}
