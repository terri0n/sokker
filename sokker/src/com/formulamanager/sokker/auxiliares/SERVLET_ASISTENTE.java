package com.formulamanager.sokker.auxiliares;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.acciones.asistente.Login;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

public abstract class SERVLET_ASISTENTE extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected boolean login(HttpServletRequest request) {
		return getUsuario(request) != null;
	}
	
	protected Usuario getUsuario(HttpServletRequest request) {
		return (Usuario)request.getSession().getAttribute("usuario");
	}

	protected boolean admin(HttpServletRequest request) {
		return login(request) && request.getSession().getAttribute("admin") != null;
	}

	protected void setAdmin(boolean b, HttpServletRequest request) {
		request.getSession().setAttribute("admin", true);
	}

	protected String getIP(HttpServletRequest request) {      
/*		String ip = request.getRemoteAddr();
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	    	ip = request.getHeader("X-Forwarded-For");
	    }
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("Proxy-Client-IP");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("WL-Proxy-Client-IP");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("HTTP_X_FORWARDED_FOR");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("HTTP_X_FORWARDED");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("HTTP_CLIENT_IP");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("HTTP_FORWARDED_FOR");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("HTTP_FORWARDED");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("HTTP_VIA");  
	    }  
	    if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown") || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {  
	        ip = request.getHeader("REMOTE_ADDR");  
	    }  
	    return ip;*/
		return (String) request.getSession().getAttribute("ip");
	}   
	
	protected void _log(HttpServletRequest request, String linea) throws IOException {
		File file = new File(SystemUtil.getVar("path") + "logs");
		if (!file.exists()) {
			file.mkdirs();
		}
		
		linea = new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date()) + "\t" + 
				getIP(request) + "\t" +
				(getUsuario(request) != null ? (
						getUsuario(request).getLogin() + "\t" + 
						getUsuario(request).getDef_tid() + "\t"
				) : "(sin usuario)\t") +
				getClass().getSimpleName() + "\t" + 
				linea + "\r\n";
		String LOG = SystemUtil.getVar("path") + "logs/_" + (getUsuario(request) == null ? "_NTDB" : getUsuario(request).getLogin()) + ".log";
		
		file = new File(LOG);
		if (!file.exists()) {
			System.out.println(file.getAbsolutePath());
			file.createNewFile();
		}
		
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true))) {
			bos.write(linea.getBytes());
		}
	}

	public static void _log_linea(String login, String linea) throws IOException {
		linea = new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date()) + "\t" + linea + "\r\n";

		String LOG = SystemUtil.getVar("path") + "logs/_" + login + ".log";
		
		File file = new File(LOG);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true))) {
			bos.write(linea.getBytes());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// 05/08/2021: lo deshabilito porque está dando problemas en Opera
//			if (getIP(request) == null && !(this instanceof Logout)) {
//				response.sendRedirect(request.getContextPath() + "/asistente?error=1&mensaje=session_expired");
//			} else {
				execute(request, response);
//			}
		} catch (LoginException | FailingHttpStatusCodeException | ParseException e) {
			if (e instanceof LoginExceptionExt) {
				// Error al acceder a Sokker: contraseña errónea, usuario baneado, sin equipo, en bancarrota...
				synchronized (request.getSession().getServletContext()) {
					String ip = getIP(request);
					HashMap<String, String> ips = Util.leer_hashmap("IPs");
					int penalizacion = this instanceof Login ? 1 : 4;
					ips.put(ip, String.valueOf(Integer.valueOf(ips.getOrDefault(ip, "0")) + penalizacion));
					Util.guardar_hashmap(ips, "IPs");

					LoginExceptionExt ex = (LoginExceptionExt)e;
					// Registramos el error si se ha escrito un usuario
					if (ex.getUsuario() != null) {
						_log_linea(ex.getUsuario(), ex.getMessage() + ": " + ex.getContrasenya());
					}

					if (Integer.valueOf(ips.get(ip)) >= 8) {
						Usuario usuario = getUsuario(request);
_log(request, "IP deshabilitada: " + ip);
_log_linea("_BLOQUEO", "IP deshabilitada: " + ip + (usuario == null ? "" : " " + usuario.getLogin()));
						request.getSession().invalidate();
					}
				}
			}
			
			e.printStackTrace();
			response.sendRedirect(request.getContextPath() + "/asistente?error=2&mensaje=" + e.getMessage());
		}
	}
	
	protected abstract void execute(HttpServletRequest req, HttpServletResponse resp) throws LoginException, ServletException, IOException, FailingHttpStatusCodeException, ParseException;
}
