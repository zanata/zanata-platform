/*
 * Copyright (c) 2009, Lombardi Software
 *  All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 * * Neither the name of Lombardi software nor the names of its
 *   contributors may be used to endorse or promote products
 *   derived from this software without specific prior
 *   written permission of Lombardi Software
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fedorahosted.flies.webtrans.client;

import java.util.Iterator;
import java.util.NoSuchElementException;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.DOM;

/**
 * A container with space for a fixed height header and a variable height content area. Uses an extension to
 * DecoratorPanel to provide the panel.
 *
 * @author amoffat Alex Moffat
 */
public class RoundedContainerWithHeader extends Composite {

    /**
     * The widget that goes into the header.
     */
    private final Widget header;
    /**
     * The widget that contains the content to display in the center of the panel.
     */
    private final Widget content;

    public RoundedContainerWithHeader(Widget header, Widget content) {
        this.header = header;
        this.content = content;

        DecoratorPanelWithHeader panel = new DecoratorPanelWithHeader();
        initWidget(panel);

        setStyleName("roundedContainerWithHeader");
    }

    /**
     * Extend the DecoratorPanel.
     */
    private class DecoratorPanelWithHeader extends DecoratorPanel {

        private DecoratorPanelWithHeader() {
            setHeader(header);
            add(content);
        }

        /**
         * Set the widget that goes in the center of the top row.
         *
         * @param header The widget.
         */
        private void setHeader(Widget header) {
            DOM.appendChild(getCellElement(0, 1), header.getElement());
            adopt(header);
        }

        /**
         * Need to include the header in the results returned from the iterator so that it gets hooked up correctly.
         */
        public Iterator<Widget> iterator() {
            final Iterator<Widget> superIterator = super.iterator();
            return new Iterator<Widget>() {
                boolean hasHeader = header != null;

                public boolean hasNext() {
                    return superIterator.hasNext() || hasHeader;
                }

                public Widget next() {
                    if (superIterator.hasNext()) {
                        return superIterator.next();
                    } else {
                        if (hasHeader && (header != null)) {
                            hasHeader = false;
                            return header;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
                }

                /**
                 * This is in violation of the contract in that it throws an UnsupportedOperationException. However
                 * it works fine in the DecoratorPanelWithHeader.
                 */
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
