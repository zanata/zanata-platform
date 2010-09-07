package net.openl10n.flies.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import net.openl10n.flies.hibernate.search.TranslatedFilterFactory;
import net.openl10n.flies.model.po.HPotEntryData;
import net.openl10n.flies.model.po.PoUtility;
import net.openl10n.flies.rest.dto.deprecated.TextFlow;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTarget;
import net.openl10n.flies.rest.dto.po.PotEntryData;
import net.openl10n.flies.search.DefaultNgramAnalyzer;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

/**
 * Represents a flow of text that should be processed as a stand-alone
 * structural unit.
 * 
 * @author Asgeir Frimannsson <asgeirf@redhat.com>
 * 
 */
@Entity
@Indexed
@FullTextFilterDef(name = "translated", impl = TranslatedFilterFactory.class)
public class HTextFlow implements Serializable, ITextFlowHistory, HasSimpleComment
{

   private static final long serialVersionUID = 3023080107971905435L;

   private Long id;

   private Integer revision = 1;

   private String resId;

   private Integer pos;

   private HDocument document;

   private boolean obsolete = false;

   private String content;

   private Map<HLocale, HTextFlowTarget> targets;

   public Map<Integer, HTextFlowHistory> history;

   private HSimpleComment comment;

   private HPotEntryData potEntryData;

   public HTextFlow()
   {

   }

   public HTextFlow(HDocument document, String resId, String content)
   {
      this.document = document;
      this.resId = resId;
      this.content = content;
   }

   public HTextFlow(TextFlow other, int revision)
   {
      this.resId = other.getId();
      this.content = other.getContent();
      this.revision = revision;
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

   // we can't use @NotNull because the position isn't set until the object has
   // been persisted
   @Column(insertable = false, updatable = false, nullable = false)
   // @Column(insertable=false, updatable=false)
   @Override
   public Integer getPos()
   {
      return pos;
   }

   public void setPos(Integer pos)
   {
      this.pos = pos;
   }

   // TODO make this case sensitive
   @NaturalId
   @Length(max = 255)
   @NotEmpty
   public String getResId()
   {
      return resId;
   }

   public void setResId(String resId)
   {
      this.resId = resId;
   }

   @NotNull
   @Override
   public Integer getRevision()
   {
      return revision;
   }

   public void setRevision(Integer revision)
   {
      this.revision = revision;
   }

   @Override
   public boolean isObsolete()
   {
      return obsolete;
   }

   /**
    * Caller must ensure that textFlow is in document.textFlows if and only if
    * obsolete = false
    * 
    * @param obsolete
    */
   public void setObsolete(boolean obsolete)
   {
      this.obsolete = obsolete;
   }

   @ManyToOne
   @JoinColumn(name = "document_id", insertable = false, updatable = false, nullable = false)
   @NaturalId
   public HDocument getDocument()
   {
      return document;
   }

   public void setDocument(HDocument document)
   {
      this.document = document;
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

   @NotNull
   @Type(type = "text")
   @Field(index = Index.TOKENIZED, analyzer = @Analyzer(impl = DefaultNgramAnalyzer.class))
   @Override
   public String getContent()
   {
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "textFlow")
   @MapKey(name = "revision")
   public Map<Integer, HTextFlowHistory> getHistory()
   {
      return history;
   }

   public void setHistory(Map<Integer, HTextFlowHistory> history)
   {
      this.history = history;
   }

   @OneToMany(cascade = CascadeType.ALL, mappedBy = "textFlow")
   @MapKey(name = "locale")
   public Map<HLocale, HTextFlowTarget> getTargets()
   {
      if (targets == null)
         targets = new HashMap<HLocale, HTextFlowTarget>();
      return targets;
   }

   public void setTargets(Map<HLocale, HTextFlowTarget> targets)
   {
      this.targets = targets;
   }

   public void setPotEntryData(HPotEntryData potEntryData)
   {
      this.potEntryData = potEntryData;
   }

   @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
   public HPotEntryData getPotEntryData()
   {
      return potEntryData;
   }

   public TextFlow toResource(int levels)
   {
      TextFlow textFlow = new TextFlow(this.getResId());
      HSimpleComment comment = this.getComment();
      if (comment != null)
      {
         textFlow.getOrAddComment().setValue(comment.getComment());
      }
      textFlow.setContent(this.getContent());
      textFlow.setLang(this.getDocument().getLocale().getLocaleId());
      textFlow.setRevision(this.getRevision());

      for (HLocale locale : getTargets().keySet())
      {
         HTextFlowTarget hTextFlowTarget = this.getTargets().get(locale);
         if (hTextFlowTarget != null)
         {
            TextFlowTarget textFlowTarget = new TextFlowTarget(textFlow, locale.getLocaleId());
            HSimpleComment tftComment = hTextFlowTarget.getComment();
            if (tftComment != null)
            {
               textFlowTarget.getOrAddComment().setValue(tftComment.getComment());
            }
            textFlowTarget.setContent(hTextFlowTarget.getContent());
            textFlowTarget.setResourceRevision(hTextFlowTarget.getTextFlowRevision());
            textFlowTarget.setState(hTextFlowTarget.getState());
            textFlow.addTarget(textFlowTarget);
         }
      }

      HPotEntryData fromHPotEntryData = this.getPotEntryData();
      if (fromHPotEntryData != null)
      {
         PotEntryData toPotEntryData = textFlow.getOrAddExtension(PotEntryData.class);
         toPotEntryData.setId(this.getResId());
         toPotEntryData.setContext(fromHPotEntryData.getContext());
         HSimpleComment extractedComment = fromHPotEntryData.getExtractedComment();
         toPotEntryData.setExtractedComment(HSimpleComment.toSimpleComment(extractedComment));
         List<String> toFlags = toPotEntryData.getFlags();
         toFlags.addAll(PoUtility.splitFlags(fromHPotEntryData.getFlags()));
         List<String> toReferences = toPotEntryData.getReferences();
         toReferences.addAll(PoUtility.splitRefs(fromHPotEntryData.getReferences()));
      }

      return textFlow;
   }

   /**
    * Used for debugging
    */
   @Override
   public String toString()
   {
      return "HTextFlow(" + "resId:" + getResId() + "content:" + getContent() + "revision:" + getRevision() + "comment:" + getComment() + "obsolete:" + isObsolete() + ")";
   }

}
