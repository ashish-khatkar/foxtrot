package com.flipkart.foxtrot.core.common;

import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TimerTask;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class IndexRollOverTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(IndexRollOverTask.class);
    private final QueryStore queryStore;
    private final Map<String, Object> conditions;

    public IndexRollOverTask(QueryStore queryStore, Map<String, Object> conditions) {
        this.queryStore = queryStore;
        this.conditions = conditions;
    }

    @Override
    public void run() {
        logger.info("Starting index rollover for job");
        try {
            queryStore.indexRollOver(conditions);
        } catch (FoxtrotException ex) {
            logger.error("Index rollover job failed", ex);
        }
        logger.info("Index rollover job finished");
    }
}
