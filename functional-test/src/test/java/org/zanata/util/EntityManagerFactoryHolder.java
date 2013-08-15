package org.zanata.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityManagerFactoryHolder {
    private static final EntityManagerFactoryHolder HOLDER =
            new EntityManagerFactoryHolder();
    private static EntityManagerFactory emFactory;

    public static EntityManagerFactoryHolder holder() {
        return HOLDER;
    }

    public EntityManagerFactory getEmFactory() {
        if (emFactory == null) {
            synchronized (HOLDER) {
                if (emFactory == null) {
                    emFactory = Persistence.createEntityManagerFactory(
                                    "zanataTestDataDatasourcePU", null);
                }
            }
        }
        return emFactory;
    }

}
