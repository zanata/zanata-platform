/*
 * CHEditorOption, Red Hat, Inc. and individual contributors as indicated by the
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Entity
@Setter
@Getter
@Access(AccessType.FIELD)
@ToString
@NoArgsConstructor
public class HAccountOption extends ModelEntityBase {
    private static final long serialVersionUID = 1L;

    private String name;

    private String value;

   @ManyToOne(optional = false)
   @JoinColumn(name = "account_id")
   private HAccount account;

    public HAccountOption(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

   @Transient
   public Boolean getValueAsBoolean()
   {
      return Boolean.parseBoolean(getValue());
   }

    @Transient
    public Integer getValueAsInt() {
        return Integer.parseInt(getValue());
    }
}
