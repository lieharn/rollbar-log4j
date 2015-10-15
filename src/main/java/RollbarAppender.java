import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;



/**
 * 
 * @author qunfei, lieharn
 */
public class RollbarAppender extends AppenderSkeleton {

	private static boolean init = false;
	private final boolean enabled = true;
	private final boolean onlyThrowable = true;
	private final boolean logs = true;

	private final Level notifyLevel = Level.ERROR;
	private String apiKey;
	private String env;
	private final String url = "https://api.rollbar.com/api/1/item/";
	
	private static final int DEFAULT_LOGS_LIMITS = 100;
	private static LimitedQueue<String> LOG_BUFFER = new LimitedQueue<String>(DEFAULT_LOGS_LIMITS);


	public void close() {
	}

	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		if (!enabled)
			return;
		if (!hasToNotify(event.getLevel()))
			return;

		// add to the LOG_BUFFER buffer
		LOG_BUFFER.add(this.layout.format(event).trim());

		boolean hasThrowable = thereIsThrowableIn(event);
		if (onlyThrowable && !hasThrowable)
			return;

		initNotifierIfNeeded();

		final Map<String, Object> context = getContext(event);

		if (hasThrowable) {
			RollbarNotifier.notify((String) event.getMessage(), getThrowable(event), context);
		} else {
			RollbarNotifier.notify((String) event.getMessage(), context);
		}

	}

	private void initNotifierIfNeeded() {
		if (init) {
			return;
		}
		RollbarNotifier.init(url, apiKey, env);
		init = true;
	}

	private boolean hasToNotify(Priority priority) {
		return priority.isGreaterOrEqual(notifyLevel);
	}

	private Map<String, Object> getContext(final LoggingEvent event) {
		@SuppressWarnings("unchecked")
		final Map<String, Object> context = MDC.getContext();
		context.put("LOG_BUFFER", new ArrayList<String>(LOG_BUFFER));

		return context;
	}

	private boolean thereIsThrowableIn(LoggingEvent loggingEvent) {
		return loggingEvent.getThrowableInformation() != null || loggingEvent.getMessage() instanceof Throwable;
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

	private static class LimitedQueue<E> extends LinkedList<E> {

		private static final long serialVersionUID = -8575950653758674702L;
		private final int limit;

        public LimitedQueue(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E o) {
            super.add(o);
            while (size() > limit) {
                super.remove();
            }
            return true;
        }
    }
}
