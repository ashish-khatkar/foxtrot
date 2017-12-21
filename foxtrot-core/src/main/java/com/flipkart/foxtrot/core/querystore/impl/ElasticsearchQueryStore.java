/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.foxtrot.core.querystore.impl;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.foxtrot.common.Document;
import com.flipkart.foxtrot.common.FieldTypeMapping;
import com.flipkart.foxtrot.common.Table;
import com.flipkart.foxtrot.common.TableFieldMapping;
import com.flipkart.foxtrot.core.common.AliasConditions;
import com.flipkart.foxtrot.core.datastore.DataStore;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.exception.FoxtrotExceptions;
import com.flipkart.foxtrot.core.parsers.ElasticsearchMappingParser;
import com.flipkart.foxtrot.core.querystore.IndexAliasManager;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import com.flipkart.foxtrot.core.table.TableMetadataManager;
import com.flipkart.foxtrot.core.util.MetricUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.rollover.RolloverRequestBuilder;
import org.elasticsearch.action.admin.indices.rollover.RolloverResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com)
 * Date: 14/03/14
 * Time: 12:27 AM
 */

public class ElasticsearchQueryStore implements QueryStore {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchQueryStore.class.getSimpleName());

    private final ElasticsearchConnection connection;
    private final DataStore dataStore;
    private final TableMetadataManager tableMetadataManager;
    private final ObjectMapper mapper;
    private final IndexAliasManager indexAliasManager;

    public ElasticsearchQueryStore(TableMetadataManager tableMetadataManager,
                                   IndexAliasManager indexAliasManager,
                                   ElasticsearchConnection connection,
                                   DataStore dataStore,
                                   ObjectMapper mapper) {
        this.connection = connection;
        this.dataStore = dataStore;
        this.tableMetadataManager = tableMetadataManager;
        this.mapper = mapper;
        this.indexAliasManager = indexAliasManager;
    }

    @Override
    @Timed
    public void initializeTable(String table) throws FoxtrotException {
        // Nothing needs to be done here since indexes are created at runtime in elasticsearch
    }

    @Override
    @Timed
    public void save(String table, Document document) throws FoxtrotException {
        table = ElasticsearchUtils.getValidTableName(table);
        Timer.Context timer = null;
        try {
            if (!tableMetadataManager.exists(table)) {
                throw FoxtrotExceptions.createBadRequestException(table,
                        String.format("unknown_table table:%s", table));
            }
            if (new DateTime().plusDays(1).minus(document.getTimestamp()).getMillis() < 0) {
                return;
            }
            final Table tableMeta = tableMetadataManager.get(table);
            final Document translatedDocument = dataStore.save(tableMeta, document);
            long timestamp = translatedDocument.getTimestamp();

            String alias = ElasticsearchUtils.getAliasFromTimestamp(table, timestamp);
            if (!indexAliasManager.aliasExists(alias)) {
                indexAliasManager.saveAlias(alias);
            }
            timer = MetricUtil.getInstance().startTimer(ElasticsearchQueryStore.class, "save");
            connection.getClient()
                    .prepareIndex()
                    .setIndex(alias)
                    .setType(ElasticsearchUtils.DOCUMENT_TYPE_NAME)
                    .setId(translatedDocument.getId())
                    .setSource(convert(translatedDocument), XContentType.JSON)
                    .setWaitForActiveShards(ActiveShardCount.DEFAULT)
                    .execute()
                    .get(2, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            MetricUtil.getInstance().markMeter(ElasticsearchQueryStore.class, "save.failure." + table + "." + e.getClass().getSimpleName());
            throw FoxtrotExceptions.createExecutionException(table, e);
        } finally {
            if (null != timer) {
                timer.stop();
            }
        }
    }

    @Override
    @Timed
    public void save(String table, List<Document> documents) throws FoxtrotException {
        table = ElasticsearchUtils.getValidTableName(table);
        Timer.Context timer = null;
        try {
            if (!tableMetadataManager.exists(table)) {
                throw FoxtrotExceptions.createBadRequestException(table,
                        String.format("unknown_table table:%s", table));
            }
            if (documents == null || documents.size() == 0) {
                throw FoxtrotExceptions.createBadRequestException(table, "Empty Document List Not Allowed");
            }
            final Table tableMeta = tableMetadataManager.get(table);
            final List<Document> translatedDocuments = dataStore.saveAll(tableMeta, documents);
            BulkRequestBuilder bulkRequestBuilder = connection.getClient().prepareBulk();

            DateTime dateTime = new DateTime().plusDays(1);

            for (Document document : translatedDocuments) {
                long timestamp = document.getTimestamp();
                if (dateTime.minus(timestamp).getMillis() < 0) {
                    continue;
                }
                String alias = ElasticsearchUtils.getAliasFromTimestamp(table, timestamp);
                if (!indexAliasManager.aliasExists(alias)) {
                    indexAliasManager.saveAlias(alias);
                }
                IndexRequest indexRequest = new IndexRequest()
                        .index(alias)
                        .type(ElasticsearchUtils.DOCUMENT_TYPE_NAME)
                        .id(document.getId())
                        .source(convert(document), XContentType.JSON);
                bulkRequestBuilder.add(indexRequest);
            }
            if (bulkRequestBuilder.numberOfActions() > 0) {
                timer = MetricUtil.getInstance().startTimer(ElasticsearchQueryStore.class, "bulkSave");
                BulkResponse responses = bulkRequestBuilder
                        .setWaitForActiveShards(ActiveShardCount.DEFAULT)
                        .execute()
                        .get(10, TimeUnit.SECONDS);
                for (int i = 0; i < responses.getItems().length; i++) {
                    BulkItemResponse itemResponse = responses.getItems()[i];
                    if (itemResponse.isFailed()) {
                        MetricUtil.getInstance().markMeter(ElasticsearchQueryStore.class, "saveAll.failure." + table);
                        logger.error(String.format("Table : %s Failure Message : %s Document : %s", table,
                                itemResponse.getFailureMessage(),
                                mapper.writeValueAsString(documents.get(i))));
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw FoxtrotExceptions.createBadRequestException(table, e);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw FoxtrotExceptions.createExecutionException(table, e);
        } finally {
            if (null != timer) {
                timer.stop();
            }
        }
    }

    @Override
    @Timed
    public Document get(String table, String id) throws FoxtrotException {
        table = ElasticsearchUtils.getValidTableName(table);
        Table fxTable;
        if (!tableMetadataManager.exists(table)) {
            throw FoxtrotExceptions.createBadRequestException(table,
                    String.format("unknown_table table:%s", table));
        }
        fxTable = tableMetadataManager.get(table);
        String lookupKey;
        Timer.Context timer = MetricUtil.getInstance().startTimer(ElasticsearchQueryStore.class, "get");
        SearchResponse searchResponse = connection.getClient()
                .prepareSearch(ElasticsearchUtils.getIndices(table))
                .setTypes(ElasticsearchUtils.DOCUMENT_TYPE_NAME)
                .setQuery(boolQuery().filter(termQuery(ElasticsearchUtils.DOCUMENT_META_ID_FIELD_NAME, id)))
                .setFetchSource(false)
                .setSize(1)
                .execute()
                .actionGet();
        timer.stop();
        if (searchResponse.getHits().getTotalHits() == 0) {
            logger.warn("Going into compatibility mode, looks using passed in ID as the data store id: {}", id);
            lookupKey = id;
        } else {
            lookupKey = searchResponse.getHits().getHits()[0].getId();
            logger.debug("Translated lookup key for {} is {}.", id, lookupKey);
        }
        return dataStore.get(fxTable, lookupKey);
    }

    @Override
    public List<Document> getAll(String table, List<String> ids) throws FoxtrotException {
        return getAll(table, ids, false);
    }

    @Override
    @Timed
    public List<Document> getAll(String table, List<String> ids, boolean bypassMetalookup) throws FoxtrotException {
        table = ElasticsearchUtils.getValidTableName(table);
        if (!tableMetadataManager.exists(table)) {
            throw FoxtrotExceptions.createBadRequestException(table,
                    String.format("unknown_table table:%s", table));
        }
        Map<String, String> rowKeys = Maps.newLinkedHashMap();
        for (String id : ids) {
            rowKeys.put(id, id);
        }
        if (!bypassMetalookup) {
            Timer.Context timer = MetricUtil.getInstance().startTimer(ElasticsearchQueryStore.class, "getBulk");
            SearchResponse response = connection.getClient().prepareSearch(ElasticsearchUtils.getIndices(table))
                    .setTypes(ElasticsearchUtils.DOCUMENT_TYPE_NAME)
                    .setQuery(boolQuery().filter(termsQuery(ElasticsearchUtils.DOCUMENT_META_ID_FIELD_NAME, ids.toArray(new String[ids.size()]))))
                    .setFetchSource(false)
                    .addStoredField(ElasticsearchUtils.DOCUMENT_META_ID_FIELD_NAME)
                    .setSize(ids.size())
                    .execute()
                    .actionGet();
            timer.stop();
            for (SearchHit hit : response.getHits()) {
                final String id = hit.getFields().get(ElasticsearchUtils.DOCUMENT_META_ID_FIELD_NAME).getValue().toString();
                rowKeys.put(id, hit.getId());
            }
        }
        logger.info("Get row keys: {}", rowKeys.size());
        return dataStore.getAll(tableMetadataManager.get(table), ImmutableList.copyOf(rowKeys.values()));
    }

    @Override
    @Timed
    public TableFieldMapping getFieldMappings(String table) throws FoxtrotException {
        table = ElasticsearchUtils.getValidTableName(table);

        if (!tableMetadataManager.exists(table)) {
            throw FoxtrotExceptions.createBadRequestException(table,
                    String.format("unknown_table table:%s", table));
        }
        try {
            ElasticsearchMappingParser mappingParser = new ElasticsearchMappingParser(mapper);
            Set<FieldTypeMapping> mappings = new HashSet<>();
            GetMappingsResponse mappingsResponse = connection.getClient().admin()
                    .indices().prepareGetMappings(ElasticsearchUtils.getIndices(table)).execute().actionGet();

            for (ObjectCursor<String> index : mappingsResponse.getMappings().keys()) {
                MappingMetaData mappingData = mappingsResponse.mappings().get(index.value).get(ElasticsearchUtils.DOCUMENT_TYPE_NAME);
                mappings.addAll(mappingParser.getFieldMappings(mappingData));
            }
            return new TableFieldMapping(table, mappings);
        } catch (IOException e) {
            throw FoxtrotExceptions.createExecutionException(table, e);
        }
    }

    @Override
    public void cleanupAll() throws FoxtrotException {
        Set<String> tables = tableMetadataManager.get().stream().map(Table::getName).collect(Collectors.toSet());
        cleanup(tables);
    }

    @Override
    @Timed
    public void cleanup(final String table) throws FoxtrotException {
        cleanup(ImmutableSet.of(table));
    }

    @Override
    @Timed
    public void cleanup(Set<String> tables) throws FoxtrotException {
        List<String> indicesToDelete = new ArrayList<String>();
        try {
            IndicesStatsResponse response = connection.getClient().admin().indices().prepareStats().execute().actionGet();
            Set<String> currentIndices = response.getIndices().keySet();

            for (String currentIndex : currentIndices) {
                String table = ElasticsearchUtils.getTableNameFromIndex(currentIndex);
                if (table != null && tables.contains(table)) {
                    boolean indexEligibleForDeletion;
                    try {
                        indexEligibleForDeletion = ElasticsearchUtils.isIndexEligibleForDeletion(currentIndex, tableMetadataManager.get(table));
                        if (indexEligibleForDeletion) {
                            logger.warn(String.format("Index eligible for deletion : %s", currentIndex));
                            indicesToDelete.add(currentIndex);
                        }
                    } catch (Exception ex) {
                        logger.error(String.format("Unable to Get Table details for Table : %s", table), ex);
                    }
                }
            }
            logger.warn(String.format("Deleting Indexes - Indexes - %s", indicesToDelete));
            if (indicesToDelete.size() > 0) {
                List<List<String>> subLists = Lists.partition(indicesToDelete, 5);
                for (List<String> subList : subLists) {
                    try {
                        connection.getClient().admin().indices().prepareDelete(subList.toArray(new String[subList.size()]))
                                .execute().actionGet(TimeValue.timeValueMinutes(5));
                        logger.warn(String.format("Deleted Indexes - Indexes - %s", subList));
                    } catch (Exception e) {
                        logger.error(String.format("Index deletion failed - Indexes - %s", subList), e);
                    }
                }
            }
        } catch (Exception e) {
            throw FoxtrotExceptions.createDataCleanupException(String.format("Index Deletion Failed indexes - %s", indicesToDelete), e);
        }
    }

    @Override
    public ClusterHealthResponse getClusterHealth() throws ExecutionException, InterruptedException {
        //Bug as mentioned in https://github.com/elastic/elasticsearch/issues/10574
        return connection.getClient().admin().cluster().prepareHealth().execute().get();
    }

    @Override
    public NodesStatsResponse getNodeStats() throws ExecutionException, InterruptedException {
        NodesStatsRequest nodesStatsRequest = new NodesStatsRequest();
        nodesStatsRequest.clear().jvm(true).os(true).fs(true).indices(true).process(true).breaker(true);
        return connection.getClient().admin().cluster().nodesStats(nodesStatsRequest).actionGet();
    }

    @Override
    public IndicesStatsResponse getIndicesStats() throws ExecutionException, InterruptedException {
        return connection.getClient()
                .admin()
                .indices()
                .prepareStats(ElasticsearchUtils.getAllIndicesPattern())
                .clear()
                .setDocs(true)
                .setStore(true)
                .execute()
                .get();
    }

    /**
     * This functions performs rollover for alias. If any single condition is matched, index will rollover and alias will point to new index.
     * As of now ES Java API only supports max_age and max_docs conditions
     * @param aliasConditions
     * @throws FoxtrotException
     */
    @Override
    public void indexRollOver(final AliasConditions aliasConditions) throws FoxtrotException {
        List<String> indicesToRollover = indexAliasManager.getAllAliases();
        try {
            for (String indexAlias : indicesToRollover) {
                logger.info("Rolling index for alias {}", indexAlias);
                RolloverRequestBuilder rolloverRequestBuilder = connection.getClient()
                        .admin()
                        .indices()
                        .prepareRolloverIndex(indexAlias);

                Settings.Builder aliasSettingsBuilder = Settings.builder();

                if (null != aliasConditions.getMaxDocs()) {
                    rolloverRequestBuilder.addMaxIndexDocsCondition(aliasConditions.getMaxDocs());
                }
                if (null != aliasConditions.getMaxAgeInDays()) {
                    rolloverRequestBuilder.addMaxIndexAgeCondition(new TimeValue(aliasConditions.getMaxAgeInDays(), TimeUnit.DAYS));
                }
                if (null != aliasConditions.getNoOfShards()) {
                    aliasSettingsBuilder.put("index.number_of_shards", aliasConditions.getNoOfShards());
                }
                if (null != aliasConditions.getNoOfReplicas()) {
                    aliasSettingsBuilder.put("index.number_of_replicas", aliasConditions.getNoOfReplicas());
                }
                if (!aliasSettingsBuilder.internalMap().isEmpty()) {
                    rolloverRequestBuilder.settings(aliasSettingsBuilder.build());
                }

                RolloverResponse response = rolloverRequestBuilder.execute().get();

                if (response.isRolledOver()) {
                    logger.info("Index rolled for alias {}", indexAlias);
                } else {
                    logger.info("Index not rolled for alias {}", indexAlias);
                }
            }
        } catch (Exception e) {
            throw FoxtrotExceptions.createIndexRolloverException(indicesToRollover, e.getMessage());
        }
    }

    private String convert(Document translatedDocument) {
        JsonNode metaNode = mapper.valueToTree(translatedDocument.getMetadata());
        ObjectNode dataNode = translatedDocument.getData().deepCopy();
        dataNode.put(ElasticsearchUtils.DOCUMENT_META_FIELD_NAME, metaNode);
        dataNode.put("timestamp", translatedDocument.getTimestamp());
        return dataNode.toString();
    }


}
