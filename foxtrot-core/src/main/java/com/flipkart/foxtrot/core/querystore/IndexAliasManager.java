package com.flipkart.foxtrot.core.querystore;

import com.flipkart.foxtrot.core.exception.FoxtrotException;
import io.dropwizard.lifecycle.Managed;

import java.util.List;

/**
 * Created by ashish.khatkar on 12/12/17.
 */
public interface IndexAliasManager extends Managed {
    void save(String aliasName) throws FoxtrotException;
    boolean exists(String aliasName) throws FoxtrotException;
    List<String> get() throws FoxtrotException;
}
