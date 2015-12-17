package org.zanata.seam.test;

import java.util.concurrent.Future;

public interface CopyTransService {

    Future<Boolean> copy();
}
