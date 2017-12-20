package com.flipkart.foxtrot.core.common;

import com.codahale.metrics.Timer;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.querystore.QueryStore;
import com.flipkart.foxtrot.core.util.MetricUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class IndexRollOverTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(IndexRollOverTask.class);
    private final QueryStore queryStore;
    private final AliasConditions aliasConditions;

    public IndexRollOverTask(QueryStore queryStore, AliasConditions aliasConditions) {
        this.queryStore = queryStore;
        this.aliasConditions = aliasConditions;
    }

    @Override
    public void run() {
        logger.info("Starting index rollover for job");
        Timer.Context timer = MetricUtil.getInstance().startTimer(IndexRollOverTask.class, "rolloverTask");
        try {
            queryStore.indexRollOver(aliasConditions);
        } catch (FoxtrotException ex) {
            MetricUtil.getInstance().markMeter(IndexRollOverTask.class, "rolloverFail");
            logger.error("Index rollover job failed", ex);
        } finally {
            timer.stop();
        }
        logger.info("Index rollover job finished");
    }
}
