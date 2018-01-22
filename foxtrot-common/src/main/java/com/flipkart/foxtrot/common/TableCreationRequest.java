package com.flipkart.foxtrot.common;

import javax.validation.constraints.NotNull;

/**
 * Created by ashish.khatkar on 18/01/18.
 */
public class TableCreationRequest {

    @NotNull
    private Table table;

    @NotNull
    private IndexTemplate tableTemplate;

    public TableCreationRequest() {
    }

    public TableCreationRequest(Table table, IndexTemplate tableTemplate) {
        this.table = table;
        this.tableTemplate = tableTemplate;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public IndexTemplate getTableTemplate() {
        return tableTemplate;
    }

    public void setTableTemplate(IndexTemplate tableTemplate) {
        this.tableTemplate = tableTemplate;
    }

    @Override
    public String toString() {
        return "TableCreationRequest{" +
                "table=" + table.toString() +
                ", tableTemplate=" + tableTemplate.toString() +
                '}';
    }
}
