package org.zanata.action;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.TimedAsyncHandle;
import org.zanata.service.SearchIndexManager;

import com.google.common.base.Optional;

@AutoCreate
@Name("reindexAction")
@Slf4j
@Scope(ScopeType.STATELESS)
@Restrict("#{s:hasRole('admin')}")
public class ReindexAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    SearchIndexManager searchIndexManager;

    public List<ReindexClassOptions> getClasses() {
        return searchIndexManager.getReindexOptions();
    }

    public void selectAll(boolean selected) {
        for (ReindexClassOptions opts : searchIndexManager.getReindexOptions()) {
            opts.setPurge(selected);
            opts.setReindex(selected);
            opts.setOptimize(selected);
        }
    }

    public boolean isPurgeAll() {
        for (ReindexClassOptions opts : searchIndexManager.getReindexOptions()) {
            if (!opts.isPurge()) {
                return false;
            }
        }
        return true;
    }

    public void setPurgeAll(boolean selected) {
        for (ReindexClassOptions opts : searchIndexManager.getReindexOptions()) {
            opts.setPurge(selected);
        }
    }

    public boolean isReindexAll() {
        for (ReindexClassOptions opts : searchIndexManager.getReindexOptions()) {
            if (!opts.isReindex()) {
                return false;
            }
        }
        return true;
    }

    public void setReindexAll(boolean selected) {
        for (ReindexClassOptions opts : searchIndexManager.getReindexOptions()) {
            opts.setReindex(selected);
        }
    }

    public boolean isOptimizeAll() {
        for (ReindexClassOptions opts : searchIndexManager.getReindexOptions()) {
            if (!opts.isOptimize()) {
                return false;
            }
        }
        return true;
    }

    public void setOptimizeAll(boolean selected) {
        for (ReindexClassOptions opts : searchIndexManager.getReindexOptions()) {
            opts.setOptimize(selected);
        }
    }

    public boolean isReindexedSinceServerRestart() {
        return searchIndexManager.getProcessHandle() != null;
    }

    public boolean isInProgress() {
        return searchIndexManager.getProcessHandle() != null
                && !searchIndexManager.getProcessHandle().isDone();
    }

    public String getCurrentClass() {
        return searchIndexManager.getCurrentClassName();
    }

    public boolean isError() {
        AsyncTaskHandle<Void> taskHandle = searchIndexManager.getProcessHandle();
        if (taskHandle == null) {
            return false;
        } else if (taskHandle.isDone()) {
            try {
                taskHandle.get();
            } catch (InterruptedException e) {
                return true;
            } catch (ExecutionException e) {
                return true;
            } catch (CancellationException e) {
                return false;
            }
        }
        return false;
    }

    public int getReindexCount() {
        if (searchIndexManager.getProcessHandle() == null) {
            return 0;
        } else {
            return searchIndexManager.getProcessHandle().getMaxProgress();
        }
    }

    public int getReindexProgress() {
        if (searchIndexManager.getProcessHandle() == null) {
            return 0;
        } else {
            return searchIndexManager.getProcessHandle().getCurrentProgress();
        }
    }

    public void reindexDatabase() {
        if (searchIndexManager.getProcessHandle() == null
                || searchIndexManager.getProcessHandle().isDone()) {
            searchIndexManager.startProcess();
        }
    }

    public void cancel() {
        searchIndexManager.getProcessHandle().cancel();
    }

    public boolean isCanceled() {
        return searchIndexManager.getProcessHandle() != null
                && searchIndexManager.getProcessHandle().isCancelled();
    }

    // TODO move to common location with ViewAllStatusAction
    private static final PeriodFormatter PERIOD_FORMATTER =
            new PeriodFormatterBuilder().appendDays()
                    .appendSuffix(" day", " days").appendSeparator(", ")
                    .appendHours().appendSuffix(" hour", " hours")
                    .appendSeparator(", ").appendMinutes()
                    .appendSuffix(" min", " mins")
                    .toFormatter();

    private String formatTimePeriod(long durationInMillis) {
        Period period = new Period(durationInMillis);

        if (period.toStandardMinutes().getMinutes() <= 0) {
            return "less than a minute"; // TODO Localize
        } else {
            return PERIOD_FORMATTER.print(period.normalizedStandard());
        }
    }

    public String getElapsedTime() {
        TimedAsyncHandle<Void> processHandle = searchIndexManager.getProcessHandle();
        if (processHandle == null) {
            log.error("processHandle is null when looking up elapsed time");
            return "";
        } else {
            long elapsedTime = processHandle.getElapsedTime();
            return formatTimePeriod(elapsedTime);
        }
    }

    public String getEstimatedTimeRemaining() {
        Optional<Long> estimate = searchIndexManager.getProcessHandle().getEstimatedTimeRemaining();
        if (estimate.isPresent()) {
            return formatTimePeriod(estimate.get());
        }
        // TODO localize (not expecting to display estimate when it is unavailable anyway).
        return "unknown";
    }
}
