import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;

import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.bo.JugadorBO;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.formulamanager.sokker.tomcat.ServletContextListener;

public class ServletContextLifecycleHarness {
    public static void main(String[] args) throws Exception {
        Path tomcat = Files.createTempDirectory("sqlite-servlet-lifecycle-");
        Path webapp = tomcat.resolve("webapps/sokker");
        Path data = tomcat.resolve("data");
        Path home = tomcat.resolve("home");
        Files.createDirectories(webapp);
        Files.createDirectories(data);
        Files.createDirectories(home.resolve("factorx"));
        Files.createDirectories(tomcat.resolve("conf"));
        String dataPath = data.toAbsolutePath().toString() + File.separator;
        String serverXml = "<Server><GlobalNamingResources><Environment name=\\"path\\" value=\\""
                + dataPath.replace("&", "&amp;").replace("\\", "\\\\").replace("\"", "&quot;")
                + "\\" type=\\"java.lang.String\\"/></GlobalNamingResources></Server>";
        Files.write(tomcat.resolve("conf/server.xml"), serverXml.getBytes(StandardCharsets.UTF_8));
        SystemUtil.REAL_PATH = webapp.toAbsolutePath().toString() + File.separator;
        JugadorBO.PATH_BASE = home.toAbsolutePath().toString() + File.separator;

        Set<Thread> before = new HashSet<Thread>(Thread.getAllStackTraces().keySet());
        ServletContext context = (ServletContext) Proxy.newProxyInstance(
                ServletContextLifecycleHarness.class.getClassLoader(),
                new Class<?>[] { ServletContext.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] methodArgs) {
                        if ("getRealPath".equals(method.getName())) {
                            return webapp.toAbsolutePath().toString() + File.separator;
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
            worker = findNewNonDaemonThread(before);
            if (worker == null) {
                throw new AssertionError("ServletContextListener must start its scheduled worker thread");
            }

            listener.contextDestroyed(event);
            worker.join(1000L);
            if (worker.isAlive()) {
                throw new AssertionError("ServletContextListener worker must stop when the web context is destroyed");
            }
        } finally {
            stopNewNonDaemonThreads(before);
        }
    }

    private static Thread findNewNonDaemonThread(Set<Thread> before) throws InterruptedException {
        for (int attempt = 0; attempt < 20; attempt++) {
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (!before.contains(thread) && thread.isAlive() && !thread.isDaemon()) {
                    return thread;
                }
            }
            Thread.sleep(25L);
        }
        return null;
    }

    private static void stopNewNonDaemonThreads(Set<Thread> before) throws InterruptedException {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (!before.contains(thread) && thread.isAlive() && !thread.isDaemon()) {
                thread.interrupt();
                thread.join(1000L);
            }
        }
    }
}
