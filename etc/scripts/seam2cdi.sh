#!/bin/bash -e

# Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
# @author tags. See the copyright.txt file in the distribution for a full
# listing of individual contributors.
#
# This is free software; you can redistribute it and/or modify it under the
# terms of the GNU Lesser General Public License as published by the Free
# Software Foundation; either version 2.1 of the License, or (at your option)
# any later version.
#
# This software is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
# details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this software; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
# site: http://www.fsf.org.


#########################################################################
# seam2cdi.sh, Sean Flanigan sflaniga@redhat.com
#
# Quick and dirty migration of Seam annotations to CDI equivalents.
#
#########################################################################
# Requirements:
# A modern bash
# GNU sed
#########################################################################
# To use this script:
# $ shopt -s globstar
# $ etc/scripts/seam2cdi.sh **/src/**/*.java **/src/**/*.groovy
#
# Note: Seam components (POJOs) with no declared scope (ie Event scope)
# will become @Dependent by default, not @RequestScoped.  Our unscoped
# components all appear to be stateless services, so this should be okay.
#########################################################################

# NB here-doc indentation must use tab!
sed -i "$@" -f - <<-EOF
	# @Install(false) -> @Exclude(...)
	s/import org.jboss.seam.annotations.Install;/import org.apache.deltaspike.core.api.exclude.Exclude;\nimport org.apache.deltaspike.core.api.projectstage.ProjectStage;/g
	s|@Install(false)|@Exclude(ifProjectStage = ProjectStage.UnitTest.class) /* TODO [CDI] Set ProjectStage for unit tests */|g
	s/org.jboss.seam.annotations.In;/javax.inject.Inject;/g
	s/org.jboss.seam.annotations.In$/javax.inject.Inject/g

	# In -> Inject
	s/@In("applicationConfiguration")/@Inject/g
	s/@In("blobPersistService")/@Inject/g
	s/@In("commonMarkRenderer")/@Inject/g
	s/@In("event")/@Inject/g
	s/@In("filePersistService")/@Inject/g
	s/@In("jsfMessages")/@Inject/g
	s/@In("localeServiceImpl")/@Inject/g
	s/@In("textFlowTargetDAO")/@Inject/g
	s/@In("translationMemoryServiceImpl")/@Inject/g
	s/@In(create = true)/@Inject/g

	# Convert single-line @In(something) to @Inject() /* something */
	s|^\( \+\)@In(\(.*\))|\1@Inject /* TODO [CDI] check this: migrated from @In(\2) */|g

	# Convert multi-line @In(... to @Inject(... but don't touch the above TODO comment
	s/@In(\([^)]*\)$/@Inject(\1/g
	s/@In /@Inject /g
	s/@In$/@Inject/g

	# Name -> Named
	s/org.jboss.seam.annotations.Name;/javax.inject.Named;/g
	s/@Name(/@Named(/g

	# Seam scopes -> CDI/DeltaSpike scopes
	/import org.jboss.seam.ScopeType;\?/d
	/import org.jboss.seam.annotations.Scope;\?/d
	s/@Scope(\(ScopeType.\)\?APPLICATION)/@javax.enterprise.context.ApplicationScoped/g
	s/@Scope(\(ScopeType.\)\?EVENT)/@javax.enterprise.context.RequestScoped/g
	s/@Scope(\(ScopeType.\)\?PAGE)/@javax.faces.bean.ViewScoped/g
	s|@Scope(\(ScopeType.\)\?CONVERSATION)|@org.apache.deltaspike.core.api.scope.ViewAccessScoped /* TODO [CDI] check this: migrated from ScopeType.CONVERSATION */|g
	s/@Scope(\(ScopeType.\)\?SESSION)/@javax.enterprise.context.SessionScoped/g
	s/@Scope(\(ScopeType.\)\?STATELESS)/@javax.enterprise.context.Dependent/g

	# Seam's AutoCreate behaviour is the default in CDI
	/import org.jboss.seam.annotations.AutoCreate;\?/d
	s/ *@AutoCreate//g

	# @Factory/@Out/@Unwrap -> @Produces
	s/org.jboss.seam.annotations.Factory/javax.enterprise.inject.Produces/g
	s/org.jboss.seam.annotations.Out/javax.enterprise.inject.Produces/g
	s/org.jboss.seam.annotations.Unwrap/javax.enterprise.inject.Produces/g

	# Seam's @Synchronized to ours
	s/org.jboss.seam.annotations.Synchronized/org.zanata.util.Synchronized/g

	# Seam's @Transactional to deltaspike's @Transactional
	s/org.jboss.seam.annotations.Transactional/org.apache.deltaspike.jpa.api.transaction.Transactional/g

	# In our code, @Factory without args tends to be Hibernate Search
	# Factory, not Seam.
	s|@Factory(|@Produces(/* TODO [CDI] check this: migrated from @Factory */|g
	s/org.jboss.seam.annotations.Out/javax.enterprise.inject.Produces/g
	s|@Out|@Produces /* FIXME [CDI] check this: migrated from @Out */|g
	s|@Unwrap|@Produces /* FIXME [CDI] check this: migrated from @Unwrap */|g

	# Use standard lifecycle methods, not Seam's
	s/org.jboss.seam.annotations.Create/javax.annotation.PostConstruct/g
	s/@Create/@PostConstruct/g
	s/org.jboss.seam.annotations.Destroy/javax.annotation.PreDestroy/g
	s/@Destroy/@PreDestroy/g
	s|@Startup|/* TODO [CDI] Remove @PostConstruct from startup method and make it accept (@Observes @Initialized ServletContext context) */|g

	# Optional but recommended change for Hibernate 3 -> 4
	s/@Type(type *= *"text")/@javax.persistence.Lob/g

	# CDI-style events -> real CDI events
	s/org.zanata.util.Event/javax.enterprise.event.Event/g

	# @RequestParameter -> @HttpParam
	s/import org.jboss.seam.annotations.web.RequestParameter;\?/import org.zanata.servlet.annotations.HttpParam;/g
	s|@RequestParameter|@Inject @HttpParam|g

	# @Restrict -> @CheckLoggedIn/@CheckRole/@CheckPermission
	s/import org.jboss.seam.annotations.security.Restrict;\?/import org.zanata.security.annotations.CheckLoggedIn;\nimport org.zanata.security.annotations.CheckPermission;\nimport org.zanata.security.annotations.CheckRole;/g
	s/@Restrict("#{identity.loggedIn}")/@CheckLoggedIn/g
	s/@Restrict("#{s:hasRole('admin')}")/@CheckRole("admin")/g
	s/@Restrict("#{s:hasPermission('', '\(.*\)')}")/@CheckPermission("\1")/g
	s/@Restrict("#{s:hasPermission(\\'\\', \\'\(.*\)\\')}")/@CheckPermission("\1")/g

	# This won't compile (because we removed the import), but should force
	# us to examine the code
	s|@Restrict(\(.*\))|@Restrict(\1) /* FIXME [CDI] call checkPermission in the method (or use @PermissionTarget) */|g

	# migrate security checks (.java and .xhtml)
	s/s:hasPermission/identity.hasPermission/g
EOF
