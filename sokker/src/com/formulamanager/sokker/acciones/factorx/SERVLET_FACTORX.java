package com.formulamanager.sokker.acciones.factorx;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.formulamanager.sokker.auxiliares.SERVLET;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.entity.Jugador;

/**
 * Servlet implementation class ServletSokker
 */
public abstract class SERVLET_FACTORX extends SERVLET {
	public static enum TIPO_FACTORX { junior(""), senior("_senior"), internacional("_inter");
		private String sufijo;
		private TIPO_FACTORX(String sufijo) {
			this.sufijo = sufijo;
		}
		public String getSufijo() {
			return sufijo;
		}
	}
	
	public enum TIPO_HISTORICO { puntos, subidas, valor;
		public Comparator<Jugador> getComparator() {
			switch (this) {
				case puntos: return Jugador.comparator_puntos();
				case subidas: return Jugador.comparator_subidas();
				case valor: return Jugador.comparator_valor();
				default: return null;
			}
		}
	}

	private static final long serialVersionUID = 1L;
	
    protected SERVLET_FACTORX.TIPO_FACTORX getTipo(HttpServletRequest request) {
		return Util.nnvl(request.getParameter("tipo")) == null ? TIPO_FACTORX.junior : TIPO_FACTORX.valueOf(Util.getString(request, "tipo"));
    }

    protected SERVLET_FACTORX.TIPO_HISTORICO getTipo_hist(HttpServletRequest request) {
		return Util.nnvl(request.getParameter("tipo_hist")) == null ? TIPO_HISTORICO.puntos : TIPO_HISTORICO.valueOf(Util.getString(request, "tipo_hist"));
    }

	public static void _log_linea(String linea) throws IOException {
		linea = SimpleDateFormat.getInstance().format(new Date()) + "\t" + linea + "\r\n";

		String LOG = SystemUtil.getVar("path") + "/logs/factorx.log";
		
		File file = new File(LOG);
		if (file.exists()) {
			file.delete();
		}

		file.createNewFile();
		
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true))) {
			bos.write(linea.getBytes());
		}
	}
}
