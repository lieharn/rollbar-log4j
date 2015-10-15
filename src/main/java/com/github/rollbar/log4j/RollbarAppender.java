package com.github.rollbar.log4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.MDC;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.json.JSONException;
import org.json.JSONObject;


public class RollbarAppender extends AppenderSkeleton {

	private NotifyBuilder notifyBuilder;

	private URL url;
	private String accessToken = "c6af3dbf6acb498ca25664f457a0b5a9";
	private String environment = "local";
	private String rollbarContext;
	private final boolean async = true;
	private boolean errors = false;
	private final IHttpRequester httpRequester = new HttpRequester();

	public RollbarAppender(String accessToken, String environment) {
		try {
			this.url = new URL("https://api.rollbar.com/api/1/item/");
		} catch (MalformedURLException e) {
			LogLog.error("error initializing url", e);
			errors = true;
		}
		if (this.accessToken == null || this.accessToken.isEmpty()) {
			LogLog.error("No accessToken set for the appender named [" + getName() + "].");
			errors = true;
        }
        if (this.environment == null || this.environment.isEmpty()) {
        	LogLog.error("No environment set for the appender named [" + getName() + "].");
			errors = true;
        }
		try {
			notifyBuilder = new NotifyBuilder(accessToken, environment, rollbarContext);
		} catch (JSONException | UnknownHostException e) {
			LogLog.error("an error occurs while initializing the builder", e);
			errors = true;
		}
	}


	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		if (errors) {
			return;
		}

		String levelName = event.getLevel().toString().toLowerCase();
		String message = (String) event.getMessage();
		@SuppressWarnings("unchecked")
		Map<String, String> context = MDC.getContext();

		Throwable throwable = getThrowable(event);

		final JSONObject payload = notifyBuilder.build(levelName, message, throwable, context);
		final HttpRequest request = new HttpRequest(url, "POST");
		request.setHeader("Content-Type", "application/json");
		request.setHeader("Accept", "application/json");
		request.setBody(payload.toString());

		if (async) {
			EXECUTOR.execute(new Runnable() {
				@Override
				public void run() {
					try {
						sendRequest(request);
					} catch (Throwable e) {
						LogLog.error("There was an error notifying the error.", e);
					}
				}
			});
		} else {
			sendRequest(request);
		}

	}

	private void sendRequest(HttpRequest request) {
		try {
			int statusCode = httpRequester.send(request);
			if (statusCode >= 200 && statusCode <= 299) {
				// Everything went OK
			} else {
				LogLog.error("Non-2xx response from Rollbar: " + statusCode);
			}

		} catch (IOException e) {
			LogLog.error("Exception sending request to Rollbar", e);
		}
	}

	private Throwable getThrowable(final LoggingEvent loggingEvent) {
		ThrowableInformation throwableInfo = loggingEvent.getThrowableInformation();
		if (throwableInfo != null)
			return throwableInfo.getThrowable();

		Object message = loggingEvent.getMessage();
		if (message instanceof Throwable) {
			return (Throwable) message;
		} else if (message instanceof String) {
			return new Exception((String) message);
		}

		return null;
	}

	public void setUrl(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			LogLog.error("Error setting url", e);
		}
	}

	public void setApiKey(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2, new ThreadFactory() {
		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setName("RollbarAppender-" + new Random().nextInt(100));
			return thread;
		}
	});
   

}
