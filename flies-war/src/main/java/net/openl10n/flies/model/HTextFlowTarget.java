package net.openl10n.flies.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.hibernate.search.ContentStateBridge;
import net.openl10n.flies.hibernate.search.LocaleIdBridge;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTarget;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.NotNull;

/**
 * Represents a flow of text that should be processed as a stand-alone
 * structural unit.
 * 
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 * 
 */
@Entity
public class HTextFlowTarget extends AbstractFliesEntity implements ITextFlowTargetHistory, HasSimpleComment
{

   private static final long serialVersionUID = 302308010797605435L;

   private HTextFlow textFlow;
   private HLocale locale;

   private String content;
   private ContentState state = ContentState.New;
   private Integer textFlowRevision;
   private HPerson lastModifiedBy;

   private HSimpleComment comment;

   public HTextFlowTarget()
   {
   }

   public HTextFlowTarget(HTextFlow textFlow, HLocale locale)
   {
      this.locale = locale;
      this.textFlow = textFlow;
      this.textFlowRevision = textFlow.getRevision();
   }

   public HTextFlowTarget(TextFlowTarget target, HLocale locale)
   {
      this.content = target.getContent();
      this.locale = locale;
      this.textFlowRevision = target.getResourceRevision();
      this.state = target.getState();
      // setTextFlow(target.getTextFlow);
      // setComment(target.comment);
      // setDocumentTarget(target.documentTarget);
   }

   @Id
   @GeneratedValue
   public Long getId()
   {
      return id;
   }

   protected void setId(Long id)
   {
      this.id = id;
   }

   public void copy(TextFlowTarget tfTarget)
   {
      this.content = tfTarget.getContent();
      this.state = tfTarget.getState();
   }

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "locale", nullable = false)
   @Field(index = Index.UN_TOKENIZED)
   @FieldBridge(impl = LocaleIdBridge.class)
   public HLocale getLocale()
   {
      return locale;
   }

   public void setLocale(HLocale locale)
   {
      this.locale = locale;
   }

   @NotNull
   @Field(index = Index.UN_TOKENIZED)
   @FieldBridge(impl = ContentStateBridge.class)
   @Override
   public ContentState getState()
   {
      return state;
   }

   public void setState(ContentState state)
   {
      this.state = state;
   }

   @NotNull
   @Column(name = "tf_revision")
   @Override
   public Integer getTextFlowRevision()
   {
      return textFlowRevision;
   }

   public void setTextFlowRevision(Integer textFlowRevision)
   {
      this.textFlowRevision = textFlowRevision;
   }

   @ManyToOne(cascade = { CascadeType.MERGE })
   @JoinColumn(name = "last_modified_by_id", nullable = true)
   @Override
   public HPerson getLastModifiedBy()
   {
      return lastModifiedBy;
   }

   public void setLastModifiedBy(HPerson lastModifiedBy)
   {
      this.lastModifiedBy = lastModifiedBy;
   }

   @NaturalId
   @ManyToOne
   @JoinColumn(name = "tf_id")
   @IndexedEmbedded(depth = 2)
   public HTextFlow getTextFlow()
   {
      return textFlow;
   }

   public void setTextFlow(HTextFlow textFlow)
   {
      this.textFlow = textFlow;
      // setResourceRevision(textFlow.getRevision());
   }

   @NotNull
   @Type(type = "text")
   // @Field(index=Index.NO) // no searching on target text yet
   @Override
   public String getContent()
   {
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   @OneToOne(optional = true, cascade = CascadeType.ALL)
   @JoinColumn(name = "comment_id")
   public HSimpleComment getComment()
   {
      return comment;
   }

   public void setComment(HSimpleComment comment)
   {
      this.comment = comment;
   }

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "HTextFlowTarget(" + "content:" + getContent() + "locale:" + getLocale() + "state:" + getState() + "comment:" + getComment() + "textflow:" + getTextFlow().getContent() + ")";
   }

   @Transient
   public void clear()
   {
      setContent("");
      setState(ContentState.New);
      setComment(null);
      setLastModifiedBy(null);
   }

}
