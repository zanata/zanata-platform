<figure>
<img alt="Process manager" src="images/admin-manage-search.png" />
</figure>
<br/>

Zanata uses lucene index for fast searching and perform various tasks such as translation memory search.

### Purge index

Mark all existing index entries for the table obsolete. Obsolete entries still occupy disk space but are not returned in any searches.

### Reindex

Index all rows in the given table. Rows will be indexed automatically when data is persisted, so this operation is only necessary when the index is out-of-date (e.g. when the database has been restored from backup, after a failed reindex, if index files have been removed).

All rows of the given table will be reindexed regardless whether they already have an entry in the index. Rows that have already been indexed will have their entries updated, which will usually have no effect on the entry.

**Warning:** This operation can take hours for large tables, and will increase memory use significantly above baseline. It is strongly recommended to run this operation during off-peak times when average server memory use is at a minimum.

### Optimize

This arrange index entries to maximize search speed and also removes any obsolete entries from the index. 
This operation will not influence indexing time.