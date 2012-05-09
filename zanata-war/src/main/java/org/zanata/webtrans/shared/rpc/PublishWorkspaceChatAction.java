package org.zanata.webtrans.shared.rpc;

public class PublishWorkspaceChatAction extends AbstractWorkspaceAction<PublishWorkspaceChatResult>
{
   private static final long serialVersionUID = -8145724589597122017L;
   private String person;
   private String msg;


   @SuppressWarnings("unused")
   private PublishWorkspaceChatAction()
   {
      this(null, null);
   }

   public PublishWorkspaceChatAction(String person, String msg)
   {
      this.person = person;
      this.msg = msg;
   }

   public String getPerson()
   {
      return person;
   }

   public String getMsg()
   {
      return msg;
   }
}
