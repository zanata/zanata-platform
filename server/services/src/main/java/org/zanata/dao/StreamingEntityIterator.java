package org.zanata.dao;

import javax.annotation.Nonnull;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.zanata.util.CloseableIterator;

class StreamingEntityIterator<E> implements CloseableIterator<E> {
    // MIN_VALUE gives a hint to mysql JDBC driver to stream results instead of
    // keeping everything in memory.
    // Note that this will tie up the JDBC connection until the ResultSet is
    // closed.
    // See
    // http://www.numerati.com/2012/06/26/reading-large-result-sets-with-hibernate-and-mysql/
    // and
    // http://dev.mysql.com/doc/refman/5.5/en/connector-j-reference-implementation-notes.html
    private final int fetchSize = Integer.MIN_VALUE;

    private final @Nonnull
    Session session;
    private int rowNum;
    private ScrollableResultsIterator iter;

    public StreamingEntityIterator(@Nonnull Session session) {
        this.session = session;
    }

    @Override
    public void close() {
        if (iter != null) {
            iter.close();
        }
        session.close();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    public Session getSession() {
        return session;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public E next() {
        if (++rowNum % 1000 == 0)
            session.clear();
        E tf = (E) iter.next()[0];
        session.evict(tf);
        return tf;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void initQuery(Query q) {
        assert iter == null;
        q.setFetchSize(fetchSize);
        q.setReadOnly(true);
        ScrollableResults scroll = q.scroll(ScrollMode.FORWARD_ONLY);
        iter = new ScrollableResultsIterator(scroll);
    }

}
