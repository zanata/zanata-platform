/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import javax.validation.constraints.NotNull;
import org.zanata.model.type.ConditionRuleActionType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Persistent representation of Copy Trans options.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@TypeDef(name = "conditionRuleAction", typeClass = ConditionRuleActionType.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Setter
@Getter
@Access(AccessType.FIELD)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HCopyTransOptions extends ModelEntityBase
{
   /**
    * Indicates the different actions that can be taken when evaluating conditions
    * for a Text Flow during Copy Trans.
    */
   public enum ConditionRuleAction
   {
      /**
       * Indicates to reject the text flow.
       */
      REJECT,

      /**
       * Indicates to flag the potentially copied translation as fuzzy.
       */
      DOWNGRADE_TO_FUZZY,

      /**
       * Indicates to ignore the condition entirely.
       */
      IGNORE;

      public char getInitial()
      {
         return name().charAt(0);
      }

      public static ConditionRuleAction valueOf(char initial)
      {
         switch (initial)
         {
            case 'R':
               return REJECT;
            case 'D':
               return DOWNGRADE_TO_FUZZY;
            case 'I':
               return IGNORE;
            default:
               throw new IllegalArgumentException(String.valueOf(initial));
         }
      }
   }

   @Type(type = "conditionRuleAction")
   @NotNull
   private ConditionRuleAction contextMismatchAction = ConditionRuleAction.REJECT;

   @Type(type = "conditionRuleAction")
   @NotNull
   private ConditionRuleAction docIdMismatchAction = ConditionRuleAction.REJECT;

   @Type(type = "conditionRuleAction")
   @NotNull
   private ConditionRuleAction projectMismatchAction = ConditionRuleAction.REJECT;
}
