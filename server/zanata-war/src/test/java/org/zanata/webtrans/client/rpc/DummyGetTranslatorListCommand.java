package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;
import java.util.Arrays;

import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

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
