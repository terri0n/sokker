import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public class UpdateAtomicityHarness {
    public static void main(String[] args) throws Exception {
        juniorFailureIsNotConvertedIntoAnEmptySquad();
        trainerFailureAbortsClubUpdateAtSharedThreshold();
    }

    private static void trainerFailureAbortsClubUpdateAtSharedThreshold() throws Exception {
        Usuario usuario = new Usuario(Integer.valueOf(995), "Club 995");
        WebClient failing = failingClient();
        try {
            try {
                AsistenteBO.leer_entrenadores(usuario, 1200, failing);
                throw new AssertionError("Trainer loading failure for club tid=995 must abort the update; it must not be skipped or swallowed");
            } catch (Exception expected) {
                if (!(rootCause(expected) instanceof IOException)) {
                    throw expected;
                }
            }
        } finally {
            failing.close();
        }
    }

    private static void juniorFailureIsNotConvertedIntoAnEmptySquad() throws Exception {
        Usuario usuario = new Usuario(Integer.valueOf(995), "Club 995");
        WebClient failing = failingClient();
        try {
            Method method = AsistenteBO.class.getDeclaredMethod("obtener_juveniles", int.class, boolean.class, Usuario.class, WebClient.class);
            method.setAccessible(true);
            try {
                method.invoke(null, Integer.valueOf(1200), Boolean.FALSE, usuario, failing);
                throw new AssertionError("Junior loading failure must abort the update instead of being converted into an empty junior list");
            } catch (InvocationTargetException expected) {
                if (!(rootCause(expected) instanceof IOException)) {
                    throw expected;
                }
            }
        } finally {
            failing.close();
        }
    }

    private static WebClient failingClient() {
        return new WebClient() {
            private static final long serialVersionUID = 1L;

            @Override
            public <P extends Page> P getPage(String url) throws IOException, FailingHttpStatusCodeException, MalformedURLException {
                throw new IOException("forced transport failure");
            }
        };
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
