/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
 *  @author tags. See the copyright.txt file in the distribution for a full
 *  listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */

package org.zanata.service;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.zanata.dao.AccountDAO;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.annotations.VisibleForTesting;

import static org.zanata.service.GraphQLScalarsKt.getAllScalarTypes;

/**
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com),
 * Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
@ApplicationScoped
public class GraphQLService {
    private final AccountDAO accountDAO;
    private GraphQL graphQL;
    private GraphQLSchema schema;

    @Inject
    public GraphQLService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @SuppressWarnings("unused")
    protected GraphQLService() {
        this(null);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @PostConstruct
    @VisibleForTesting
    public void postConstruct() {
        //register the service
        schema = new GraphQLSchemaGenerator()
                .withAdditionalTypes(getAllScalarTypes())
                .withOperationsFromSingleton(accountDAO, AccountDAO.class)
                .generate();
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    public String getSchema() {
        return new SchemaPrinter().print(schema);
    }

    public ExecutionResult query(@Nonnull String query) {
        return graphQL.execute(query);
    }

}
