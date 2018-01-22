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
package com.flipkart.foxtrot.server.resources;

import com.flipkart.foxtrot.common.Table;
import com.flipkart.foxtrot.common.TableCreationRequest;
import com.flipkart.foxtrot.common.TableUpdationRequest;
import com.flipkart.foxtrot.core.exception.FoxtrotException;
import com.flipkart.foxtrot.core.querystore.impl.ElasticsearchUtils;
import com.flipkart.foxtrot.core.table.TableManager;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/tables")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TableManagerResource {

    private final TableManager tableManager;

    public TableManagerResource(TableManager tableManager) {
        this.tableManager = tableManager;
    }

    @POST
    public Response save(@Valid final TableCreationRequest tableCreationRequest) throws FoxtrotException {
        tableCreationRequest.getTable().setName(ElasticsearchUtils.getValidTableName(tableCreationRequest.getTable().getName()));
        tableManager.save(tableCreationRequest);
        return Response.ok(tableCreationRequest).build();
    }

    @GET
    @Path("/{name}")
    public Response get(@PathParam("name") String name) throws FoxtrotException {
        name = ElasticsearchUtils.getValidTableName(name);
        Table table = tableManager.get(name);
        return Response.ok().entity(table).build();
    }

    @PUT
    @Path("/{name}")
    public Response get(@PathParam("name") final String name,
                        @Valid final TableUpdationRequest tableUpdationRequest) throws FoxtrotException {
        tableManager.update(tableUpdationRequest);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{name}/delete")
    public Response delete(@PathParam("name") String name) throws FoxtrotException {
        name = ElasticsearchUtils.getValidTableName(name);
        tableManager.delete(name);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    public Response getAll() throws FoxtrotException {
        return Response.ok().entity(tableManager.getAll()).build();
    }
}
