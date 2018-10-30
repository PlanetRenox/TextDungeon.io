import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/* This class cleans up expired temporary login auths inside ServletContext every 25 mins. */

@WebListener
public class ControllerBackground implements ServletContextListener {

	private ScheduledExecutorService scheduler;

	@Override
	public void contextInitialized(ServletContextEvent event) {

		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new ServletContextCleanup(event.getServletContext()), 0, 25, TimeUnit.MINUTES);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		scheduler.shutdownNow();
	}

	private class ServletContextCleanup implements Runnable {

		ServletContext context;

		public ServletContextCleanup(ServletContext context) {

			this.context = context;
		}

		@Override
		public void run() {

			int expireTime = 60000 * 20 ; // 20min | 60000 = 1 min

			Enumeration<?> e = context.getAttributeNames();
			while (e.hasMoreElements()) {
				String id = (String) e.nextElement();

				if (Character.getNumericValue(id.charAt(id.length() - 13)) == 1) {
					String sessTime = id.substring(id.length() - 13, id.length());

					if ((System.currentTimeMillis() - Long.valueOf(sessTime)) > expireTime) {
						
						context.removeAttribute(id);

					}

				}

			}

		}

	}
}
