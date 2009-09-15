package org.fedorahosted.flies.rest.service;

import org.fedorahosted.flies.rest.dto.Documents;

interface DocumentsServiceAction {

    public Documents get();

    public void post(Documents docs);

    public void put(Documents docs);

}