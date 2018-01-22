package com.flipkart.foxtrot.core.table.impl;

import com.flipkart.foxtrot.common.IndexTemplate;
import com.flipkart.foxtrot.common.Table;
import com.flipkart.foxtrot.common.TableCreationRequest;
import com.flipkart.foxtrot.common.TableUpdationRequest;
import com.flipkart.foxtrot.core.datastore.DataStore;
import com.flipkart.foxtrot.core.exception.FoxtrotExceptions;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import com.flipkart.foxtrot.core.table.TableManager;
import com.flipkart.foxtrot.core.table.TableMetadataManager;

import java.util.List;

/**
 * Created by rishabh.goyal on 05/12/15.
 */

public class FoxtrotTableManager implements TableManager {

    private final TableMetadataManager metadataManager;
    private final QueryStore queryStore;
    private final DataStore dataStore;

    public FoxtrotTableManager(TableMetadataManager metadataManager, QueryStore queryStore, DataStore dataStore) {
        this.metadataManager = metadataManager;
        this.queryStore = queryStore;
        this.dataStore = dataStore;
    }

    @Override
    public void save(TableCreationRequest tableCreationRequest) throws FoxtrotException {
        validateTableParams(tableCreationRequest.getTable());
        if (metadataManager.exists(tableCreationRequest.getTable().getName())) {
            throw FoxtrotExceptions.createTableExistsException(tableCreationRequest.getTable().getName());
        }
        queryStore.initializeTable(tableCreationRequest);
        dataStore.initializeTable(tableCreationRequest.getTable());
        metadataManager.save(tableCreationRequest.getTable());
    }

    @Override
    public Table get(String name) throws FoxtrotException {
        Table table = metadataManager.get(name);
        if (table == null) {
            throw FoxtrotExceptions.createTableMissingException(name);
        }
        return table;
    }

    @Override
    public List<Table> getAll() throws FoxtrotException {
        return metadataManager.get();
    }

    @Override
    public void update(TableUpdationRequest tableUpdationRequest) throws FoxtrotException {
        validateTableUpdateParams(tableUpdationRequest);
        if (!metadataManager.exists(tableUpdationRequest.getTableName())) {
            throw FoxtrotExceptions.createTableMissingException(tableUpdationRequest.getTableName());
        }
        if (null != tableUpdationRequest.getTtl()) {
            Table table = metadataManager.get(tableUpdationRequest.getTableName());
            table.setTtl(tableUpdationRequest.getTtl());
            metadataManager.save(table);
        }
        if (null != tableUpdationRequest.getIndexTemplate()) {
            queryStore.updateTableIndexTemplate(tableUpdationRequest.getTableName(), tableUpdationRequest.getIndexTemplate());
        }
    }

    @Override
    public void delete(String tableName) throws FoxtrotException {
        // TODO Implement this once downstream implications are figured out
    }

    private void validateTableUpdateParams(TableUpdationRequest tableUpdationRequest) throws FoxtrotException {
        if (tableUpdationRequest == null ||
                tableUpdationRequest.getTableName() == null ||
                tableUpdationRequest.getTableName().trim().isEmpty() ||
                (null != tableUpdationRequest.getTtl() && tableUpdationRequest.getTtl() <= 0)) {
            throw FoxtrotExceptions.createBadRequestException(tableUpdationRequest != null ? tableUpdationRequest.getTableName() : null, "Invalid updation params");
        }
    }
    private void validateTableParams(Table table) throws FoxtrotException {
        if (table == null || table.getName() == null || table.getName().trim().isEmpty() || table.getTtl() <= 0) {
            throw FoxtrotExceptions.createBadRequestException(table != null ? table.getName() : null, "Invalid Table Params");
        }
    }
}
