package com.flipkart.foxtrot.common;


import com.google.gson.Gson;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Created by ashish.khatkar on 06/12/17.
 */
public class TableV2 {

    private static final Gson gson = new Gson();

    @NotNull
    private Table table;

    @NotNull
    private List<IndexTemplate> tableTemplates;

    public TableV2() {
    }

    public TableV2(Table table, List<IndexTemplate> tableTemplates) {
        this.table = table;
        this.tableTemplates = tableTemplates;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<IndexTemplate> getTableTemplates() {
        return tableTemplates;
    }

    public void setTableTemplates(List<IndexTemplate> tableTemplates) {
        this.tableTemplates = tableTemplates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("table", table.toString())
                .append("tableTemplate", gson.toJson(tableTemplates.toString()))
                .toString();
    }
}
