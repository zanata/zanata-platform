/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
import { useRouterHistory } from 'react-router'
import { createHistory } from 'history'

/**
 * Creates a history object used by the router.
 *
 * This is created in a separate module so that other modules can hook in and
 * extend it.
 */

// DONE change to browserHistory
// DONEset basename for the history to the serving location
// DONE make sure /profile/view/{username} routes to the right place
// DONE make sure Profile link on left points to /profile/view/{username}
// DONE change the default route to go to /profile/view/{username} instead of
// /profile/{username}
// TODO fix the explore link/page to work with non-hash history
// TODO fix the explore components that use hash
//      (search for user definitely works in master, both logged in and not)
// TODO fix the profile link in UserTeaser component and make sure it works
//      (on explore page)
// TODO make all the other hash history links go to the non-hash places
//      this includes rewriting to the app URL, and the client-side part

export const history = useRouterHistory(createHistory)({
  basename: window.config.baseUrl
})
