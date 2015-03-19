package org.zanata.service;

public interface GravatarService {
    int USER_IMAGE_SIZE = 115;

    String getUserImageUrl(int size);

    String getUserImageUrl(int size, String email);

    String getGravatarHash(String email);
}
