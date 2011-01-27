package net.openl10n.flies.webtrans.client.rpc;

import java.util.ArrayList;
import java.util.Arrays;

import net.openl10n.flies.webtrans.shared.model.Person;
import net.openl10n.flies.webtrans.shared.model.PersonId;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslatorList;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslatorListResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetTranslatorListCommand implements Command
{
   private final AsyncCallback<GetTranslatorListResult> callback;

   public DummyGetTranslatorListCommand(GetTranslatorList action, AsyncCallback<GetTranslatorListResult> callback)
   {
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      callback.onSuccess(new GetTranslatorListResult(new ArrayList<Person>(Arrays.asList(new Person(new PersonId("personID"), "Some Person with an Incredibly Long Name")))));
   }

}
