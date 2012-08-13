package org.zanata.webtrans.shared.rpc;


// TODO replace this class with NoOpResult
public class ExitWorkspaceResult implements DispatchResult
{

   private static final long serialVersionUID = 1L;

   private String userName;

   @SuppressWarnings("unused")
   private ExitWorkspaceResult()
   {
   }

   public ExitWorkspaceResult(String userName)
   {
      this.userName = userName;
   }

   public String getuserName()
   {
      return userName;
   }
}
