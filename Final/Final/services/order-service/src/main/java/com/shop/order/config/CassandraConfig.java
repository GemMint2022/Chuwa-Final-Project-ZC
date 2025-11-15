package com.shop.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;

import java.util.Collections;
import java.util.List;

@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Override
    protected String getKeyspaceName() {
        return "order_keyspace";
    }

    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }

    @Override
    public String[] getEntityBasePackages() {
        return new String[]{"com.shop.order.model"};
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        return Collections.singletonList(
                CreateKeyspaceSpecification.createKeyspace(getKeyspaceName())
                        .ifNotExists()
                        .with(KeyspaceOption.DURABLE_WRITES, true)
                        .withSimpleReplication(1L)
        );
    }

    @Override
    protected String getContactPoints() {
        return System.getProperty("spring.data.cassandra.contact-points", "cassandra");
    }

    @Override
    protected int getPort() {
        return Integer.parseInt(System.getProperty("spring.data.cassandra.port", "9042"));
    }

    @Override
    protected String getLocalDataCenter() {
        return System.getProperty("spring.data.cassandra.local-datacenter", "datacenter1");
    }
}