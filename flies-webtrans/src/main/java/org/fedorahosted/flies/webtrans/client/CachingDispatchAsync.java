package org.fedorahosted.flies.webtrans.client;

import java.util.HashMap;
import java.util.Map;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

/**
 * Dispatcher which support caching of data in memory
 * 
 */
public class CachingDispatchAsync implements DispatchAsync {
	private DispatchAsync dispatcher;
	private Map<Action<Result>, Result> cache = new HashMap<Action<Result>, Result>();

	@Inject
	public CachingDispatchAsync(final DispatchAsync dispatcher) {
		this.dispatcher = dispatcher;
	}

	/*
	 * (non-Javadoc)
	 * @see net.customware.gwt.dispatch.client.DispatchAsync#execute(A, com.google.gwt.user.client.rpc.AsyncCallback)
	 */
	public <A extends Action<R>, R extends Result> void execute(final A action, final AsyncCallback<R> callback) {
		dispatcher.execute(action, callback);
	}

	/**
	 * Execute the give Action. If the Action was executed before it will get fetched from the cache
	 * 
	 * @param <A> Action implementation
	 * @param <R> Result implementation
	 * @param action the action
	 * @param callback the callback
	 */
	@SuppressWarnings("unchecked")
	public <A extends Action<R>, R extends Result> void executeWithCache(final A action, final AsyncCallback<R> callback) {
		final Result r = cache.get(action);
		
		if (r != null) {
			callback.onSuccess((R) r);
		}
		else {
			dispatcher.execute(action, new AsyncCallback<R>() {

				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				public void onSuccess(R result) {
					cache.put((Action) action, (Result) result);
					callback.onSuccess(result);
				}

			});
		}
	}

	/**
	 * Clear the cache
	 */
	public void clear() {
		cache.clear();
	}
}