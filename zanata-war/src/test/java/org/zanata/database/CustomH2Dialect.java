package org.zanata.database;

import org.hibernate.dialect.H2Dialect;

// This class is a workaround for:
// https://hibernate.atlassian.net/browse/HHH-7002
// https://hibernate.atlassian.net/browse/HHH-2775
public class CustomH2Dialect extends H2Dialect {

    public CustomH2Dialect() {
        super();
        // Register SQL spec literals
        registerKeyword("true");
        registerKeyword("false");
        registerKeyword("unknown");
    }

    @Override
    public String getDropSequenceString(String sequenceName) {
        // Adding the "if exists" clause to avoid warnings
        return "drop sequence if exists " + sequenceName;
    }

    @Override
    public boolean dropConstraints() {
        // We don't need to drop constraints before dropping tables; that just
        // leads to error messages about missing tables when we don't have a
        // schema in the database
        return false;
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public boolean supportsIfExistsAfterTableName() {
        return false;
    }

    @Override
    public String getCascadeConstraintsString() {
        return " CASCADE ";
    }
}
