package com.flipkart.foxtrot.common;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by ashish.khatkar on 08/12/17.
 */
public class IndexDetails implements Serializable {
    private static final long serialVersionUID = -3086868483579299018L;

    private final String indexDateFormat = "dd-M-yyyy";
    private final String indexHourFormat = "HH";

    @NotNull
    @Min(1)
    @Max(24)
    private int indexSplitInHours;

    public String getIndexDateFormat() {
        return indexDateFormat;
    }

    public String getIndexHourFormat() {
        return indexHourFormat;
    }

    public int getIndexSplitInHours() {
        return indexSplitInHours;
    }

    public void setIndexSplitInHours(int indexSplitInHours) {
        this.indexSplitInHours = indexSplitInHours;
    }
}
