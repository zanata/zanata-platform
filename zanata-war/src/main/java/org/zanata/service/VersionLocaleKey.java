package org.zanata.service;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.zanata.common.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AllArgsConstructor
@EqualsAndHashCode
public class VersionLocaleKey implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    private Long projectIterationId;

    @Getter
    private LocaleId localeId;
}
