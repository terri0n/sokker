import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.formulamanager.sokker.tomcat.ServletContextListener;

public class ServletContextLifecycleHarness {
    public static void main(String[] args) throws Exception {
        Set<Thread> before = new HashSet<Thread>(Thread.getAllStackTraces().keySet());
        ServletContext context = (ServletContext) Proxy.newProxyInstance(
                ServletContextLifecycleHarness.class.getClassLoader(),
                new Class<?>[] { ServletContext.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] methodArgs) {
                        if ("getRealPath".equals(method.getName())) {
                            return "/tmp/";
                        }
                        Class<?> type = method.getReturnType();
                        if (type == boolean.class) return Boolean.FALSE;
                        if (type == int.class) return Integer.valueOf(0);
                        if (type == long.class) return Long.valueOf(0L);
                        return null;
                    }
                });

        ServletContextListener listener = new ServletContextListener();
        ServletContextEvent event = new ServletContextEvent(context);
        Thread worker = null;
        try {
            listener.contextInitialized(event);
            worker = findListenerThread(before);
            if (worker == null) {
                throw new AssertionError("ServletContextListener must start its scheduled worker thread");
            }

            listener.contextDestroyed(event);
            worker.join(1000L);
            if (worker.isAlive()) {
                throw new AssertionError("ServletContextListener worker must stop when the web context is destroyed");
            }
        } finally {
            stopListenerThreads(before);
        }
    }

    private static Thread findListenerThread(Set<Thread> before) throws InterruptedException {
        for (int attempt = 0; attempt < 20; attempt++) {
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (!before.contains(thread) && thread.isAlive() && isListenerThread(thread)) {
                    return thread;
                }
            }
            Thread.sleep(25L);
        }
        return null;
    }

    private static void stopListenerThreads(Set<Thread> before) throws InterruptedException {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (!before.contains(thread) && thread.isAlive() && isListenerThread(thread)) {
                thread.interrupt();
                thread.join(1000L);
            }
        }
    }

    private static boolean isListenerThread(Thread thread) {
        return thread.getClass().getName().startsWith("com.formulamanager.sokker.tomcat.ServletContextListener$");
    }
}
