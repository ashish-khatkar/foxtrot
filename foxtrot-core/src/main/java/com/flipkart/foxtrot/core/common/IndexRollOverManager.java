package com.flipkart.foxtrot.core.common;

import com.flipkart.foxtrot.core.querystore.QueryStore;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public class IndexRollOverManager implements Managed {
    private static final Logger logger = LoggerFactory.getLogger(IndexRollOverManager.class);
    final IndexRollOverManagerConfig config;
    final QueryStore querystore;
    final Timer timer;

    public IndexRollOverManager(IndexRollOverManagerConfig config, QueryStore queryStore) {
        this.config = config;
        this.querystore = queryStore;
        this.timer = new Timer(true);
    }

    @Override
    public void start() throws Exception {
        logger.info("Starting index rollover manager");
        if (config.isActive()) {
            logger.info("Scheduling index rollover job");
            this.timer.scheduleAtFixedRate(new IndexRollOverTask(querystore, config.getAliasConditions()),
                    config.getInitialDelayInSeconds() * 1000l,
                    config.getIntervalInSeconds() * 1000l);
            logger.info("Scheduled index rollover job");
        } else {
            logger.info("Not scheduling index rollover job");
        }
        logger.info("Started index rollover manager");
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping index rollover manager");
        timer.cancel();
        logger.info("Stopped index rollover manager");
    }
}
