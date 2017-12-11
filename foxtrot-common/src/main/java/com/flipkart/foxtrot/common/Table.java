/**
 * Copyright 2014 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.foxtrot.common;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com)
 * Date: 15/03/14
 * Time: 9:51 PM
 */
public class Table implements Serializable {

    private static final long serialVersionUID = -3086868483579299018L;
    
    @NotNull
    @NotEmpty
    private String name;

    @Min(1)
    @Max(180)
    private int ttl;

    private boolean seggregatedBackend = false;

    @NotNull
    private IndexDetails defaultIndexDetails;

    /**
     * This map stores index rollover related settings corresponding to configName
     * If foxtrot index settings corresponding to configName are not present in the map defaultIndexDetails will be used
     */
    private Map<String, IndexDetails> indexDetails;

    public Table() {
    }

    public Table(String name, int ttl) {
        this.name = name;
        this.ttl = ttl;
    }

    public Table(String name, int ttl, boolean seggregatedBackend) {
        this.name = name;
        this.ttl = ttl;
        this.seggregatedBackend = seggregatedBackend;
    }

    public Table(String name, int ttl, IndexDetails defaultIndexDetails) {
        this.name = name;
        this.ttl = ttl;
        this.defaultIndexDetails = defaultIndexDetails;
    }

    public Table(String name, int ttl, boolean seggregatedBackend, IndexDetails defaultIndexDetails) {
        this.name = name;
        this.ttl = ttl;
        this.seggregatedBackend = seggregatedBackend;
        this.defaultIndexDetails = defaultIndexDetails;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public IndexDetails getDefaultIndexDetails() {
        return defaultIndexDetails;
    }

    public void setDefaultIndexDetails(IndexDetails defaultIndexDetails) {
        this.defaultIndexDetails = defaultIndexDetails;
    }

    public Map<String, IndexDetails> getIndexDetails() {
        return indexDetails;
    }

    public void setIndexDetails(Map<String, IndexDetails> indexDetails) {
        this.indexDetails = indexDetails;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("ttl", ttl)
                .append("seggregatedBackend", seggregatedBackend)
                .toString();
    }

    public boolean isSeggregatedBackend() {
        return seggregatedBackend;
    }

    public void setSeggregatedBackend(boolean seggregatedBackend) {
        this.seggregatedBackend = seggregatedBackend;
    }

}
