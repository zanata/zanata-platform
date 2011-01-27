package net.openl10n.flies.rest;

public class StringSet extends ElemSet<String>
{

   public StringSet(String values)
   {
      super(values);
   }

   @Override
   protected String valueOfElem(String value)
   {
      return value;
   }

}
