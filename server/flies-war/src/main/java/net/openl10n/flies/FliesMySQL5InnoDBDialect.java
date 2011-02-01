package net.openl10n.flies;

import java.sql.Types;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class FliesMySQL5InnoDBDialect extends MySQL5InnoDBDialect
{

   @Override
   protected void registerVarcharTypes()
   {
      registerColumnType(Types.VARCHAR, "longtext");
      // registerColumnType( Types.VARCHAR, 16777215, "mediumtext" );
      // registerColumnType( Types.VARCHAR, 65535, "text" );
      registerColumnType(Types.VARCHAR, 255, "varchar($l) binary");
   }

   @Override
   public boolean areStringComparisonsCaseInsensitive()
   {
      return false;
   }

}
