package org.fedorahosted.flies.util;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.mock.DBUnitSeamTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Imports some data into the database with the help of DBUnit. This allows us to
 * use the same dataset files as in unit testing, but in the regular application startup during
 * development. Also helps to avoid maintaining the crude Hibernate import.sql file for development
 * deployment.
 *
 * @author Christian Bauer
 */
@Name("dbunitImporter")
@Scope(ScopeType.APPLICATION)
@Install(false)
public class DBUnitImporter extends DBUnitSeamTest {

    // You can listen to this event during startup, e.g. to index the imported data
    public static final String IMPORT_COMPLETE_EVENT = "DBUnitImporter.importComplete";

    @Logger
    static Log log;

    protected List<String> datasets = new ArrayList<String>();

    public List<String> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<String> datasets) {
        this.datasets = datasets;
    }

    protected void prepareDBUnitOperations() {
        if (datasets == null) return;

        for (String dataset : datasets) {
            log.info("Adding DBUnit dataset to import: " + dataset);
            beforeTestOperations.add(
                new DataSetOperation(dataset, DatabaseOperation.CLEAN_INSERT)
            );
        }
    }

    // Do it when the application starts (but after everything else has been loaded)
    @Observer("org.jboss.seam.postInitialization")
    @Override
    public void prepareDataBeforeTest() {
        log.info("Importing DBUnit datasets using datasource JNDI name: " + datasourceJndiName);
        super.prepareDataBeforeTest();
        Events.instance().raiseEvent(IMPORT_COMPLETE_EVENT);
    }

}
