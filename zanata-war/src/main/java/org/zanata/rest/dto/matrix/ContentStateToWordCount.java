package org.zanata.rest.dto.matrix;

import org.zanata.common.ContentState;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @see org.zanata.rest.dto.matrix.UserWorkMatrix
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Data
@AllArgsConstructor
public class ContentStateToWordCount {
    private ContentState contentState;
    private long wordCount;
}
