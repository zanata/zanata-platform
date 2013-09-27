package org.zanata.service;

public interface GravatarService {
    String getUserImageUrl(int size);

    String getUserImageUrl(int size, String email);
}
