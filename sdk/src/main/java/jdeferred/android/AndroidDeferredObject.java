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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import jdeferred.android.annotation.ExecutionScope;

public class AndroidDeferredObject<D, F, P> extends DeferredObject<D, F, P> {
	private static final InternalHandler sHandler = new InternalHandler();

	private static final int MESSAGE_POST_DONE = 0x1;
	private static final int MESSAGE_POST_PROGRESS = 0x2;
	private static final int MESSAGE_POST_FAIL = 0x3;
	private static final int MESSAGE_POST_ALWAYS = 0x4;

	final protected Logger log = LoggerFactory
			.getLogger(AndroidDeferredObject.class);
	
	private final AndroidExecutionScope defaultAndroidExecutionScope;

	public AndroidDeferredObject(Promise<D, F, P> promise) {
		this(promise, AndroidExecutionScope.UI);
	}

	public AndroidDeferredObject(Promise<D, F, P> promise,
			AndroidExecutionScope defaultAndroidExecutionScope) {
		this.defaultAndroidExecutionScope = defaultAndroidExecutionScope;
		promise.done(new DoneCallback<D>() {
			@Override
			public void onDone(D result) {
				AndroidDeferredObject.this.resolve(result);
			}
		}).progress(new ProgressCallback<P>() {
			@Override
			public void onProgress(P progress) {
				AndroidDeferredObject.this.notify(progress);
			}
		}).fail(new FailCallback<F>() {
			@Override
			public void onFail(F result) {
				AndroidDeferredObject.this.reject(result);
			}
		});
	}

	private static class InternalHandler extends Handler {
		public InternalHandler() {
			super(Looper.getMainLooper());
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void handleMessage(Message msg) {
			CallbackMessage<?, ?, ?, ?> result = (CallbackMessage<?, ?, ?, ?>) msg.obj;
			switch (msg.what) {
			case MESSAGE_POST_DONE:
				((DoneCallback) result.callback).onDone(result.resolved);
				break;
			case MESSAGE_POST_PROGRESS:
				((ProgressCallback) result.callback)
						.onProgress(result.progress);
				break;
			case MESSAGE_POST_FAIL:
				((FailCallback) result.callback).onFail(result.rejected);
				break;
			case MESSAGE_POST_ALWAYS:
				((AlwaysCallback) result.callback).onAlways(result.state,
						result.resolved, result.rejected);
				break;
			}
		}
	}

	protected void triggerDone(DoneCallback<D> callback, D resolved) {
		if (determineAndroidExecutionScope(callback) == AndroidExecutionScope.UI) {
			executeInUiThread(MESSAGE_POST_DONE, callback, State.RESOLVED,
					resolved, null, null);
		} else {
			super.triggerDone(callback, resolved);
		}
	};

	protected void triggerFail(FailCallback<F> callback, F rejected) {
		if (determineAndroidExecutionScope(callback) == AndroidExecutionScope.UI) {
			executeInUiThread(MESSAGE_POST_FAIL, callback, State.REJECTED,
					null, rejected, null);
		} else {
			super.triggerFail(callback, rejected);
		}
	};

	protected void triggerProgress(ProgressCallback<P> callback, P progress) {
		if (determineAndroidExecutionScope(callback) == AndroidExecutionScope.UI) {
			executeInUiThread(MESSAGE_POST_PROGRESS, callback, State.PENDING,
					null, null, progress);
		} else {
			super.triggerProgress(callback, progress);
		}
	};

	protected void triggerAlways(AlwaysCallback<D, F> callback, State state,
			D resolve, F reject) {
		if (determineAndroidExecutionScope(callback) == AndroidExecutionScope.UI) {
			executeInUiThread(MESSAGE_POST_ALWAYS, callback, state, resolve,
					reject, null);
		} else {
			super.triggerAlways(callback, state, resolve, reject);
		}
	};

	protected <Callback> void executeInUiThread(int what, Callback callback,
			State state, D resolve, F reject, P progress) {
		Message message = sHandler.obtainMessage(what,
				new CallbackMessage<Callback, D, F, P>(this, callback, state,
						resolve, reject, progress));
		message.sendToTarget();
	}
	
	protected AndroidExecutionScope determineAndroidExecutionScope(Class<?> clazz, String methodName, Class<?> ... arguments) {
		ExecutionScope scope = null;
		
		if (methodName != null) {
			try {
				Method method = clazz.getMethod(methodName, arguments);
				scope = method.getAnnotation(ExecutionScope.class);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		if (scope == null) {
			scope = clazz.getAnnotation(ExecutionScope.class);
		}
		
		return scope == null ? defaultAndroidExecutionScope : scope.value();
	}

	protected AndroidExecutionScope determineAndroidExecutionScope(Object callback) {
		AndroidExecutionScope scope = null;
		if (callback instanceof AndroidExecutionScopeable) {
			scope = ((AndroidExecutionScopeable) callback).getExecutionScope();
		} else if (callback instanceof DoneCallback) {
			return determineAndroidExecutionScope(callback.getClass(), "onDone", Object.class);
		} else if (callback instanceof FailCallback) {
			return determineAndroidExecutionScope(callback.getClass(), "onFail", Object.class);
		} else if (callback instanceof ProgressCallback) {
			return determineAndroidExecutionScope(callback.getClass(), "onProgress", Object.class);
		} else if (callback instanceof AlwaysCallback) {
			return determineAndroidExecutionScope(callback.getClass(), "onAlways", State.class, Object.class, Object.class);
		}
		return scope == null ? defaultAndroidExecutionScope : scope;
	}

	@SuppressWarnings("rawtypes")
	private static class CallbackMessage<Callback, D, F, P> {
		final Deferred deferred;
		final Callback callback;
		final D resolved;
		final F rejected;
		final P progress;
		final State state;

		CallbackMessage(Deferred deferred, Callback callback, State state,
				D resolved, F rejected, P progress) {
			this.deferred = deferred;
			this.callback = callback;
			this.state = state;
			this.resolved = resolved;
			this.rejected = rejected;
			this.progress = progress;
		}
	}

}
