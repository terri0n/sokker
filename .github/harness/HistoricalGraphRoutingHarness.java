import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.formulamanager.sokker.servlets.Servlet;

public final class HistoricalGraphRoutingHarness {
    private HistoricalGraphRoutingHarness() {
    }

    public static void main(String[] args) throws Exception {
        Method resolver;
        try {
            resolver = Servlet.class.getDeclaredMethod("resolverHistoricoGrafica", HttpServletRequest.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Missing historical graph routing resolver", e);
        }
        resolver.setAccessible(true);

        require(resolve(resolver, "1", 0), "Explicit historico=1 must select historical data");
        require(!resolve(resolver, "0", 1), "Explicit historico=0 must override historical session state");
        require(resolve(resolver, "", 1), "Blank historico must fall back to historical session state");
        require(!resolve(resolver, null, 0), "Missing historico must fall back to current session state");

        Path tag = Paths.get("sokker/WebContent/WEB-INF/tags/jugadores.tag");
        String text = new String(Files.readAllBytes(tag), StandardCharsets.UTF_8).replace("\r\n", "\n");
        require(text.contains("onclick=\"grafica_ajax($(this), 'talento', ${j.pid}, ${sessionScope.historico});\""),
                "Junior talent graph must pass the active historical-view flag");
        require(text.contains("onclick=\"grafica_ajax($(this), 'talento', ${j.juvenil.pid}, 1);\""),
                "Former junior talent graph must explicitly select historical junior data");

        System.out.println("Historical graph routing harness OK");
    }

    private static boolean resolve(Method resolver, String requestValue, int sessionValue) throws Exception {
        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                HistoricalGraphRoutingHarness.class.getClassLoader(),
                new Class<?>[] {HttpSession.class},
                new SessionHandler(sessionValue));
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HistoricalGraphRoutingHarness.class.getClassLoader(),
                new Class<?>[] {HttpServletRequest.class},
                new RequestHandler(requestValue, session));
        return ((Boolean) resolver.invoke(null, request)).booleanValue();
    }

    private static final class SessionHandler implements InvocationHandler {
        private final Map<String, Object> attributes = new HashMap<String, Object>();

        SessionHandler(int historico) {
            attributes.put("historico", Integer.valueOf(historico));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getAttribute".equals(method.getName())) {
                return attributes.get((String) args[0]);
            }
            return defaultValue(method.getReturnType());
        }
    }

    private static final class RequestHandler implements InvocationHandler {
        private final String historico;
        private final HttpSession session;

        RequestHandler(String historico, HttpSession session) {
            this.historico = historico;
            this.session = session;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getParameter".equals(method.getName()) && "historico".equals(args[0])) {
                return historico;
            }
            if ("getSession".equals(method.getName())) {
                return session;
            }
            return defaultValue(method.getReturnType());
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return Boolean.FALSE;
        if (type == byte.class) return Byte.valueOf((byte) 0);
        if (type == short.class) return Short.valueOf((short) 0);
        if (type == int.class) return Integer.valueOf(0);
        if (type == long.class) return Long.valueOf(0L);
        if (type == float.class) return Float.valueOf(0F);
        if (type == double.class) return Double.valueOf(0D);
        if (type == char.class) return Character.valueOf('\0');
        return null;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
