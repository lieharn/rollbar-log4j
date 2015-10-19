package com.github.rollbar.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.prefs.Preferences;


public class RollbarAppender extends AppenderSkeleton {

    private NotifyBuilder payloadBuilder;
    private URL url;
    private String apiKey;
    private String environment;
    private String rollbarContext;
    private boolean async = false;
    private boolean enabled = true;
    private boolean initialised = false;
    private Level level = Level.ERROR;
    private static final boolean ACTIVATED_DEFAULT = false;

    private static final Preferences prefs = Preferences.userNodeForPackage(RollbarAppender.class);

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }



    private IHttpRequester httpRequester = new HttpRequester();

    public RollbarAppender() {
        try {
            this.url = new URL("https://api.rollbar.com/api/1/item/");
        } catch (MalformedURLException e) {
            LogLog.error("Error initializing url", e);
        }
    }

    @Override
    public synchronized void activateOptions() {
        super.activateOptions();
        if(!prefs.getBoolean("activated", ACTIVATED_DEFAULT)) {
            LogLog.warn("preference for this appender is not set.");
            enabled = false;
        }
        if (enabled) {
            if (this.url != null && this.apiKey != null && !this.apiKey.isEmpty() && this.environment != null && !this.environment.isEmpty() && this.layout != null) {
                try {
                    payloadBuilder = new NotifyBuilder(apiKey, environment, rollbarContext);
                    initialised = true;
                } catch (JSONException e) {
                    LogLog.error("JSON Error building NotifyBuilder", e);
                } catch (UnknownHostException e) {
                    LogLog.error("Network Error building NotifyBuilder", e);
                }
            } else {
                LogLog.error("Rollbar's url or apiKey or environment or layout is empty");
            }
        }
    }

    @Override
    protected void append(LoggingEvent event) {
        // Not initialised, don't push log
        if (!this.initialised) {
            return;
        }
        // Log Level is not enough, don't push log
        if (!hasToNotify(event.getLevel())) {
            return;
        }
        String levelName = event.getLevel().toString().toLowerCase();
        String message = this.layout.format(event);
        Throwable throwable = null;
        if (this.thereIsThrowableIn(event)) {
            throwable = this.getThrowable(event);
        }
        final JSONObject payload = payloadBuilder.build(levelName, message, throwable, new HashMap<String, String>());
        final HttpRequest request = new HttpRequest(url, "POST");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        request.setBody(payload.toString());
        if (async) {
            payloadBuilder.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    sendRequest(request);
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
            } else {
                LogLog.warn("Non-2xx response from Rollbar: " + statusCode);
            }

        } catch (IOException e) {
            LogLog.error("Exception sending request to Rollbar", e);
        }
    }

    private boolean thereIsThrowableIn(LoggingEvent loggingEvent) {
        return loggingEvent.getThrowableInformation() != null || loggingEvent.getMessage() instanceof Throwable;
    }


    private Throwable getThrowable(final LoggingEvent loggingEvent) {
        ThrowableInformation throwableInfo = loggingEvent.getThrowableInformation();
        if (throwableInfo != null) return throwableInfo.getThrowable();
        Object message = loggingEvent.getMessage();
        if (message instanceof Throwable) {
            return (Throwable) message;
        } else if (message instanceof String) {
            return new Exception((String) message);
        }

        return null;
    }

    public boolean hasToNotify(Priority priority) {
        return priority.isGreaterOrEqual(level);
    }


    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return true;
    }


    public void setHttpRequester(IHttpRequester httpRequester) {
        this.httpRequester = httpRequester;
    }

    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            LogLog.error("Error setting url", e);
        }
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setRollbarContext(String context) {
        this.rollbarContext = context;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
