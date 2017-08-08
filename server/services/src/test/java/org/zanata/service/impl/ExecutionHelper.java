/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
class ExecutionHelper {

    /**
     * Utility method to generate a cartesian product of all possible scenarios
     */
    static Set<Object[]> cartesianProduct(Iterable<?>... colls) {
        // Base case
        if (colls.length == 1) {
            Iterable<?> lastSet = colls[0];
            Set<Object[]> product = new HashSet<Object[]>();

            for (Object elem : lastSet) {
                product.add(new Object[] { elem });
            }
            return product;
        } else {
            // Recursive case
            Iterable<?> lastSet = colls[colls.length - 1];
            Set<Object[]> subProduct =
                    cartesianProduct(Arrays.copyOfRange(colls, 0,
                            colls.length - 1));
            Set<Object[]> fullProduct = new HashSet<Object[]>();

            for (Object[] subProdElem : subProduct) {
                for (Object elem : lastSet) {
                    Object[] newSubProd =
                            Arrays.copyOf(subProdElem, subProdElem.length + 1);
                    newSubProd[newSubProd.length - 1] = elem;
                    fullProduct.add(newSubProd);
                }
            }

            return fullProduct;
        }
    }

}
