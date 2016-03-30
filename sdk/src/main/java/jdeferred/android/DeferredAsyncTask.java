/*******************************************************************************
 * Copyright 2013 Ray Tsang
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package jdeferred.android;

import java.util.concurrent.CancellationException;

import org.jdeferred.Promise;
import org.jdeferred.DeferredManager.StartPolicy;
import org.jdeferred.impl.DeferredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.AsyncTask;

/**
 * Wrapper for AsyncTask, so that AsyncTask cancellation, completion, and progress callbacks are
 * wired into a promise callback.
 * 
 * @author Ray Tsang
 *
 * @param <Params>
 * @param <Progress>
 * @param <Result>
 */
public abstract class DeferredAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
	protected final Logger log = LoggerFactory.getLogger(DeferredAsyncTask.class);
	
	private final DeferredObject<Result, Throwable, Progress> deferred = new DeferredObject<Result, Throwable, Progress>();
	private final StartPolicy startPolicy;
	
	private Throwable throwable;
	
	public DeferredAsyncTask() {
		this.startPolicy = StartPolicy.DEFAULT;
	}
	
	public DeferredAsyncTask(StartPolicy startPolicy) {
		this.startPolicy = startPolicy;
	}
	
	@Override
	protected final void onCancelled() {
		deferred.reject(new CancellationException());
	}
	
	protected final void onCancelled(Result result) {
		deferred.reject(new CancellationException());
	};
	
	@Override
	protected final void onPostExecute(Result result) {
		if (throwable != null) {
			deferred.reject(throwable);
		} else {
			deferred.resolve(result);
		}
	}
	
	@Override
	protected final void onProgressUpdate(Progress ... values) {
		if (values == null || values.length == 0) {
			deferred.notify(null);
		} else if (values.length > 0) {
			log.warn("There were multiple progress values.  Only the first one was used!");
			deferred.notify(values[0]);
		}
	};
	
	protected final Result doInBackground(Params ... params) {
		try {
			return doInBackgroundSafe(params);
		} catch (Throwable e) {
			throwable = e;
			return null;
		}
	};
	
	protected abstract Result doInBackgroundSafe(Params ... params) throws Exception;
	
	@SuppressWarnings("unchecked")
	protected final void notify(Progress progress) {
		publishProgress(progress);
	}
	
	public Promise<Result, Throwable, Progress> promise() {
		return deferred.promise();
	}

	public StartPolicy getStartPolicy() {
		return startPolicy;
	}
}
