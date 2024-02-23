package com.formulamanager.sokker.servlets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.formulamanager.sokker.auxiliares.Navegador;
import com.formulamanager.sokker.auxiliares.SERVLET_ASISTENTE;
import com.formulamanager.sokker.auxiliares.SystemUtil;
import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.bo.AsistenteBO;
import com.formulamanager.sokker.bo.EquipoBO;
import com.formulamanager.sokker.bo.NtdbBO;
import com.formulamanager.sokker.bo.UsuarioBO;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Juvenil;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

@WebServlet("/servlet/*")
public class Servlet extends SERVLET_ASISTENTE {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain;charset=UTF-8");
		String accion = request.getPathInfo();

		Usuario usuario = (Usuario) request.getSession().getAttribute("usuario");
		if (usuario != null || accion.equals("/ip")) {
			try {
				String salida = (String) getClass().getDeclaredMethod(accion.split("/")[1], HttpServletRequest.class, HttpServletResponse.class, Usuario.class).invoke(this, request, response, usuario);
				if (salida != null) {
					response.getWriter().write(salida);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		} else {
			response.sendError(401, "Session expired");
//			response.getWriter().write("Session expired");
		}
	}
	
	public String jugador_bbcode(HttpServletRequest request, HttpServletResponse response, Usuario usuario) {
		int pid = Integer.valueOf(request.getParameter("pid"));
		Jugador j = AsistenteBO.buscar_jugador(request, pid, false, false);
		
		return j == null ? "Not found" : j.getStr_bbcode();
	}

	public String exportar_bbcode(HttpServletRequest request, HttpServletResponse response, Usuario usuario) {
		// La coma del final nos llega en el QueryString
		String pids = "," + request.getQueryString();
		boolean historico = Util.getInt(request.getSession(), "historico") > 0;
		List<Jugador> jugadores = leer_lista_jugadores(pids, usuario, historico);
		
		return AsistenteBO.exportar_jugadores(jugadores);
	}
	
	private List<Jugador> leer_lista_jugadores(String pids, Usuario usuario, boolean historico) {
		List<Jugador> jugadores = new ArrayList<Jugador>();
		
		List<Jugador> lista_jugadores = AsistenteBO.leer_jugadores(usuario.getDef_tid(), usuario.getDef_equipo(), historico, usuario);
		for (Jugador j : lista_jugadores) {
			// Si no se envía ningún PID se devuelven todos los jugadores
			// NOTA: -3 porque pids tiene la coma del inicio y de la queryString tendría que venir con una coma al final
			if (pids.indexOf("," + j.getPid() + ",") > -1 || pids.length() < 3) {
				jugadores.add(j);
			}
		}
		return jugadores;
	}

	/**
	 * Exporta una lista de jugadores a una URL externa
	 * 
	 * @param request
	 * @param response
	 * @param usuario
	 * @return "ok" si todo ha ido bien
	 * @throws FailingHttpStatusCodeException
	 * @throws MalformedURLException
	 * @throws LoginException
	 * @throws IOException
	 * @throws ParseException
	 */
	public String exportar_url(HttpServletRequest request, HttpServletResponse response, Usuario usuario) throws FailingHttpStatusCodeException, MalformedURLException, LoginException, IOException, ParseException {
		URL url = new URL(request.getParameter("url"));
		String pids = "," + request.getParameter("pids");
		boolean historico = Util.getInt(request.getSession(), "historico") > 0;
		List<Jugador> jugadores = leer_lista_jugadores(pids, usuario, historico);

		new Navegador() {
			@Override
			protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
				for (Jugador j : jugadores) {
					NtdbBO.actualizar_jugador_remoto(navegador, j, url, usuario);
				}
			}
		};
		
		return jugadores.size() + "";
	}
	
	public String datos_grafica(HttpServletRequest request, HttpServletResponse response, Usuario usuario) {
		response.setContentType("application/json;charset=UTF-8");

		int pid = Integer.valueOf(request.getParameter("pid"));
		String tipo = request.getParameter("tipo");
		boolean historico = Util.getInt(request.getSession(), "historico") > 0;
		boolean juveniles = "talento".equals(tipo);
		Jugador j = AsistenteBO.buscar_jugador(request, pid, historico, juveniles);

		if (j != null) {
			switch (tipo) {
				// Jugadores
				case "valor":				return j.getDatos_grafica_valor();
				case "forma":				return j.getDatos_grafica_forma();
				case "condicion":			return j.getDatos_grafica_condicion();
				case "rapidez":				return j.getDatos_grafica_rapidez();
				case "tecnica":				return j.getDatos_grafica_tecnica();
				case "pases":				return j.getDatos_grafica_pases();
				case "porteria":			return j.getDatos_grafica_porteria();
				case "defensa":				return j.getDatos_grafica_defensa();
				case "creacion":			return j.getDatos_grafica_creacion();
				case "anotacion":			return j.getDatos_grafica_anotacion();
				case "experiencia":			return j.getDatos_grafica_experiencia();
				case "disciplina_tactica":	return j.getDatos_grafica_disciplina_tactica();
				case "trabajo_en_equipo":	return j.getDatos_grafica_trabajo_en_equipo();
				case "suma_habilidades":	return j.getDatos_grafica_suma_habilidades();
				// Juveniles
				case "talento":				
					((Juvenil)j).calcular_niveles();
					return ((Juvenil)j).getDatos_grafica_nivel();
			}
		}
		
		return "Not found";
	}

	/**
	 * Recibo la ip nada m�s cargarse la p�gina y la guardo en la sesi�n
	 * Solo conf�o en el env�o si la diferencia de timestamp es menor que 5000
	 */
	public void ip(HttpServletRequest request, HttpServletResponse response, Usuario nul) {
		if (new Date().getTime() - (Long)request.getSession().getAttribute("timestamp") < 5000) {
			request.getSession().setAttribute("ip", request.getParameter("ip"));
		}
	}

	public void guardar_notas(HttpServletRequest request, HttpServletResponse response, Usuario usuario) throws IOException {
		String notas = URLDecoder.decode(request.getQueryString(), "UTF-8");
		if (notas.length() > 5000) {
			notas = notas.substring(0, 5000);
		}
		usuario.setNotas(notas);
		UsuarioBO.grabar_usuario(usuario);
	}

	public void descargar(HttpServletRequest request, HttpServletResponse response, Usuario usuario) throws IOException {
		String archivo;
		switch (Util.getInteger(request, "tipo")) {
			case 1: archivo = "_" + usuario.getLogin().toLowerCase() + ".properties";
					break;
			case 2: archivo = usuario.getDef_tid() + ".properties";
					break;
			case 3: archivo = usuario.getDef_tid() + "_juveniles.properties";
					break;
			case 4: archivo = usuario.getDef_tid() + "_historico.properties";
					break;
			case 5: archivo = usuario.getDef_tid() + "_juveniles_historico.properties";
					break;
			default: throw new RuntimeException("tipo incorrecto");
		}
		
		response.setContentType("text/plain;charset=UTF-8");
		response.setHeader("Content-Disposition","attachment; filename=\"" + archivo + "\"");

		File myObj = new File(SystemUtil.getVar("path") + archivo);
		Scanner myReader = new Scanner(myObj);
		while (myReader.hasNextLine()) {
			String data = myReader.nextLine();
			response.getWriter().println(data);
		}
		myReader.close();
	}
	
	public String enviar_skmail(HttpServletRequest request, HttpServletResponse response, Usuario usuario) throws IOException, FailingHttpStatusCodeException, LoginException, ParseException {
		if (request.getParameter("confirmed2").equals("1")) {
			String[] pids = request.getParameter("pids").split(",");
			String asunto = request.getParameter("asunto");
			String mensaje = request.getParameter("mensaje");
			String ilogin = request.getParameter("ilogin");
			String ipassword = request.getParameter("ipassword");

			// Usamos un navegador sin xml para enviar los sk-mails y otro xml para obtener el login del due�o del jugador
			new Navegador(true, request) {
				@Override
				protected void execute(WebClient navegador_xml) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
					new Navegador(false, ilogin, ipassword, request) {
						@Override
						protected void execute(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException, LoginException, ParseException {
							boolean primero = true;
							for (String pid : pids) {
								Jugador j = AsistenteBO.buscar_jugador(request, Integer.valueOf(pid), false, false);
								if (j != null) {
									if (primero) {
										primero = false;
									} else {
										try {
											Thread.sleep(200);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									j.setLogin_duenyo(EquipoBO.obtener_login(j.getTid(), navegador_xml));
									//j.setLogin_duenyo("terrion");
									if (j.getLogin_duenyo() != null) {
										AsistenteBO.mandar_skmail(navegador, asunto, mensaje, j);
									}
								}
							}
						}
					};
				}
			};
			
			return "ok";
		} else {
			throw new RuntimeException("Login not confirmed");
		}
	}

	@Override
	protected void execute(HttpServletRequest req, HttpServletResponse resp) throws LoginException, ServletException, IOException, FailingHttpStatusCodeException, ParseException {
		
	}
}
