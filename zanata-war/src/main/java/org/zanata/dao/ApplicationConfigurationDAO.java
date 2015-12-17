package org.zanata.dao;

import org.hibernate.Session;
import javax.inject.Named;
import org.zanata.model.HApplicationConfiguration;

@Named("applicationConfigurationDAO")

@javax.enterprise.context.Dependent
public class ApplicationConfigurationDAO extends
        AbstractDAOImpl<HApplicationConfiguration, Long> {

    public ApplicationConfigurationDAO() {
        super(HApplicationConfiguration.class);
    }

    public ApplicationConfigurationDAO(Session session) {
        super(HApplicationConfiguration.class, session);
    }

    public HApplicationConfiguration findByKey(String key) {
        return (HApplicationConfiguration) getSession()
                .byNaturalId(HApplicationConfiguration.class).using("key", key)
                .load();
    }

}
