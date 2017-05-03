package org.zanata.service;

import java.io.Serializable;

public interface GravatarService extends Serializable {
    int USER_IMAGE_SIZE = 115;

    String getUserImageUrl(int size);

    String getUserImageUrl(int size, String email);

    String getGravatarHash(String email);
}
