package net.openl10n.flies.util;

import java.util.ArrayList;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.mock.DBUnitSeamTest;
import org.testng.annotations.Test;

/**
 * Imports some data into the database with the help of DBUnit. This allows us
 * to use the same dataset files as in unit testing, but in the regular
 * application startup during development. Also helps to avoid maintaining the
 * crude Hibernate import.sql file for development deployment.
 * 
 * @author Christian Bauer
 */
@Name("dbunitImporter")
@Scope(ScopeType.APPLICATION)
@Install(false)
@Test(groups = { "seam-tests" })
public class DBUnitImporter extends DBUnitSeamTest
{

   // You can listen to this event during startup, e.g. to index the imported
   // data
   public static final String IMPORT_COMPLETE_EVENT = "DBUnitImporter.importComplete";

   @Logger
   private static Log log;

   private boolean prepared;

   protected List<String> datasets = new ArrayList<String>();

   public List<String> getDatasets()
   {
      return datasets;
   }

   public void setDatasets(List<String> datasets)
   {
      this.datasets = datasets;
   }

   private void prepare()
   {
      if (!prepared)
      {
         if (datasets == null)
            return;
         for (String dataset : datasets)
         {
            log.info("Adding DBUnit dataset to import: " + dataset);
            beforeTestOperations.add(new DataSetOperation(dataset, DatabaseOperation.CLEAN_INSERT));
         }
         prepared = true;
      }
   }

   protected void prepareDBUnitOperations()
   {
      prepare();
   }

   // Do it when the application starts (but after everything else has been
   // loaded)
   @Observer("org.jboss.seam.postInitialization")
   @Override
   public void prepareDataBeforeTest()
   {
      log.info("Importing DBUnit datasets using datasource JNDI name: " + datasourceJndiName);
      // make sure prepare() gets called, whether using seam 2.2.0 or 2.2.1
      prepare();
      super.prepareDataBeforeTest();
      Events.instance().raiseEvent(IMPORT_COMPLETE_EVENT); // TODO include
                                                           // component name of
                                                           // this instance
   }

}
