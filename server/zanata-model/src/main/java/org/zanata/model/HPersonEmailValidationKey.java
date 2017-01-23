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

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.model.validator.EmailDomain;

@Entity
@EntityListeners({ HPersonEmailValidationKey.EntityListener.class })
public class HPersonEmailValidationKey implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String keyHash;
    private HPerson person;
    private Date creationDate;
    private String email;

    public HPersonEmailValidationKey(HPerson person, String email,
            String keyHash) {
        this.person = person;
        this.keyHash = keyHash;
        this.email = email;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @Column(nullable = false, unique = true)
    public String getKeyHash() {
        return keyHash;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    public Date getCreationDate() {
        return creationDate;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "personId", nullable = false, unique = true)
    public HPerson getPerson() {
        return person;
    }

    @Email
    @NotEmpty
    @EmailDomain
    public String getEmail() {
        return email;
    }

    public static class EntityListener {

        @SuppressWarnings("unused")
        @PrePersist
        private void onPersist(HPersonEmailValidationKey key) {
            key.creationDate = new Date();
        }
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setKeyHash(final String keyHash) {
        this.keyHash = keyHash;
    }

    public void setPerson(final HPerson person) {
        this.person = person;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "HPersonEmailValidationKey(id=" + this.getId() + ", keyHash="
                + this.getKeyHash() + ", person=" + this.getPerson()
                + ", creationDate=" + this.getCreationDate() + ", email="
                + this.getEmail() + ")";
    }

    public HPersonEmailValidationKey() {
    }
}
