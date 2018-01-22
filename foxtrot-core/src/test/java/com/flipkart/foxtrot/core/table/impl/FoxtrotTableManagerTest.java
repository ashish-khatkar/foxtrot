package com.flipkart.foxtrot.core.table.impl;

import com.flipkart.foxtrot.common.Table;
import com.flipkart.foxtrot.common.TableCreationRequest;
import com.flipkart.foxtrot.common.TableUpdationRequest;
import com.flipkart.foxtrot.core.datastore.DataStore;
import com.flipkart.foxtrot.core.exception.ErrorCode;
import com.flipkart.foxtrot.core.exception.FoxtrotExceptions;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import com.flipkart.foxtrot.core.table.TableManager;
import com.flipkart.foxtrot.core.table.TableMetadataManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by rishabh.goyal on 05/12/15.
 */
public class FoxtrotTableManagerTest {

    private TableManager tableManager;
    private QueryStore queryStore;
    private DataStore dataStore;
    private TableMetadataManager metadataManager;


    @Before
    public void setUp() throws Exception {
        this.queryStore = mock(QueryStore.class);
        this.dataStore = mock(DataStore.class);
        this.metadataManager = mock(TableMetadataManager.class);
        this.tableManager = new FoxtrotTableManager(metadataManager, queryStore, dataStore);
    }

    @Test
    public void saveTableNullName() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        try {
            Table table = new Table();
            table.setName(null);
            table.setTtl(60);
            TableCreationRequest tableCreationRequest = new TableCreationRequest();
            tableCreationRequest.setTable(table);
            tableManager.save(tableCreationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.INVALID_REQUEST, e.getCode());
        }
    }

    @Test
    public void saveTableEmptyName() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        try {
            Table table = new Table();
            table.setName(" ");
            table.setTtl(60);
            TableCreationRequest tableCreationRequest = new TableCreationRequest();
            tableCreationRequest.setTable(table);
            tableManager.save(tableCreationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.INVALID_REQUEST, e.getCode());
        }
    }

    @Test
    public void saveNullTable() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        try {
            TableCreationRequest tableCreationRequest = new TableCreationRequest();
            tableCreationRequest.setTable(null);
            tableManager.save(tableCreationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.INVALID_REQUEST, e.getCode());
        }
    }

    @Test
    public void saveTableInvalidTtl() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        try {
            Table table = new Table();
            table.setName("abcd");
            table.setTtl(0);
            TableCreationRequest tableCreationRequest = new TableCreationRequest();
            tableCreationRequest.setTable(table);
            tableManager.save(tableCreationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.INVALID_REQUEST, e.getCode());
        }
    }

    @Test
    public void saveTable() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        Table table = new Table();
        table.setName("abcd");
        table.setTtl(10);
        TableCreationRequest tableCreationRequest = new TableCreationRequest();
        tableCreationRequest.setTable(table);
        tableManager.save(tableCreationRequest);
    }

    @Test
    public void saveExistingTable() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        doReturn(true).when(metadataManager).exists(any(String.class));
        try {
            Table table = new Table();
            table.setName("abcd");
            table.setTtl(10);
            TableCreationRequest tableCreationRequest = new TableCreationRequest();
            tableCreationRequest.setTable(table);
            tableManager.save(tableCreationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.TABLE_ALREADY_EXISTS, e.getCode());
        }
    }

    @Test
    public void saveTableQueryStoreFailed() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doThrow(FoxtrotExceptions.createExecutionException("dummy", new IOException())).when(queryStore).initializeTable(any(TableCreationRequest.class));
        try {
            Table table = new Table();
            table.setName("abcd");
            table.setTtl(10);
            TableCreationRequest tableCreationRequest = new TableCreationRequest();
            tableCreationRequest.setTable(table);
            tableManager.save(tableCreationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.STORE_EXECUTION_ERROR, e.getCode());
        }
    }

    @Test
    public void saveTableDataStoreFailed() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doThrow(FoxtrotExceptions.createExecutionException("dummy", new IOException())).when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        try {
            Table table = new Table();
            table.setName("abcd");
            table.setTtl(10);
            TableCreationRequest tableCreationRequest = new TableCreationRequest();
            tableCreationRequest.setTable(table);
            tableManager.save(tableCreationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.STORE_EXECUTION_ERROR, e.getCode());
        }
    }

    @Test
    public void saveTableDataStoreNoTableFound() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doThrow(FoxtrotExceptions.createTableMissingException("Dummy")).when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        try {
            Table table = new Table();
            table.setName("abcd");
            table.setTtl(10);
            TableCreationRequest tableCreationRequest = new TableCreationRequest();
            tableCreationRequest.setTable(table);
            tableManager.save(tableCreationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.TABLE_NOT_FOUND, e.getCode());
        }
    }

    @Test
    public void updateTable() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        doReturn(true).when(metadataManager).exists(anyString());
        Table table = new Table();
        table.setName("abcd");
        table.setTtl(10);
        doReturn(table).when(metadataManager).get("abcd");
        TableUpdationRequest tableUpdationRequest = new TableUpdationRequest("abcd");
        tableUpdationRequest.setTtl(10);
        tableManager.update(tableUpdationRequest);
    }

    @Test
    public void updateNonExistingTable() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        doReturn(false).when(metadataManager).exists(anyString());
        try {
            Table table = new Table();
            table.setName("abcd");
            table.setTtl(10);
            doReturn(table).when(metadataManager).get("abcd");
            TableUpdationRequest tableUpdationRequest = new TableUpdationRequest("abcd");
            tableManager.update(tableUpdationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.TABLE_NOT_FOUND, e.getCode());
        }
    }

    @Test
    public void updateNullTable() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        doReturn(false).when(metadataManager).exists(anyString());
        try {
            tableManager.update(null);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.INVALID_REQUEST, e.getCode());
        }
    }

    @Test
    public void updateTableNullName() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        doReturn(false).when(metadataManager).exists(anyString());
        try {
            TableUpdationRequest tableUpdationRequest = new TableUpdationRequest(null);
            tableManager.update(tableUpdationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.INVALID_REQUEST, e.getCode());
        }
    }

    @Test
    public void updateTableEmptyName() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        doReturn(false).when(metadataManager).exists(anyString());
        try {
            TableUpdationRequest tableUpdationRequest = new TableUpdationRequest(" ");
            tableManager.update(tableUpdationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.INVALID_REQUEST, e.getCode());
        }
    }

    @Test
    public void updateTableInvalidTtl() throws Exception {
        doNothing().when(metadataManager).save(any(Table.class));
        doNothing().when(dataStore).initializeTable(any(Table.class));
        doNothing().when(queryStore).initializeTable(any(TableCreationRequest.class));
        doReturn(false).when(metadataManager).exists(anyString());
        try {
            TableUpdationRequest tableUpdationRequest = new TableUpdationRequest("abcd", 0);
            tableManager.update(tableUpdationRequest);
            fail();
        } catch (FoxtrotException e) {
            assertEquals(ErrorCode.INVALID_REQUEST, e.getCode());
        }
    }
}