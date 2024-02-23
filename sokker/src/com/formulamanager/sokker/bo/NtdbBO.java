package com.formulamanager.sokker.bo;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.formulamanager.sokker.auxiliares.Util;
import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;

/**
 * NOTA: solo he probado las dos webs mayoritarias y la de Francia:
 * - ntdb.sokker.cz
 * - online-manager.co
 * - yfolire.net/~sktables/add-player.php
 */
public class NtdbBO {
	public static int MAX_ID_SELECCION = 1000;
	public static int DIF_NT_U21 = 400;
	public static String NOTAS_NTDB = "Direct update from Sokker Asistente";
	
	public static String paises[] = {
			"Polska", // 1
			"New Zealand", // 2
			"England", // 3
			"USA", // 4
			"Canada", // 5
			"Australia", // 6
			"Scotland", // 7
			"Magyarország", // 8
			"România", // 9
			"Italia", // 10
			"Jamaica", // 11
			"Nederland", // 12
			"Česká republika", // 13
			"Slovensko", // 14
			"Deutschland", // 15
			"France", // 16
			"España", // 17
			"México", // 18
			"Argentina", // 19
			"Portugal", // 20
			"Brasil", // 21
			"South Africa", // 22
			"Lietuva", // 23
			"Schweiz", // 24
			"Suomi", // 25
			"Slovenija", // 26
			"Norge", // 27
			"België", // 28
			"Danmark", // 29
			"Österreich", // 30
			"Sverige", // 31
			"Ireland", // 32
			"Eesti", // 33
			"Hrvatska", // 34
			"Uruguay", // 35
			"Colombia", // 36
			"Perú", // 37
			"Nigeria", // 38
			"Srbija", // 39
			"Cymru", // 40
			"Türkiye", // 41
			"Chile", // 42
			"Rossiya", // 43
			"Venezuela", // 44
			"Latvija", // 45
			"Belarus", // 46
			"Bosna i Hercegovina", // 47
			"Misr", // 48
			"Hellas", // 49
			"Ghana", // 50
			"India", // 51
			"Israel", // 52
			"Shqipëria", // 53
			"Bulgaria", // 54
			"Lëtzebuerg", // 55
			"Severna Makedonija", // 56
			"Bolivia", // 57
			"Ecuador", // 58
			"Malaysia", // 59
			"Hong Kong", // 60
			"República Dominicana", // 61
			"Nippon", // 62
			"Ísland", // 63
			"Ukraina", // 64
			"Indonesia", // 65
			"Hayastan", // 66
			"Malta", // 67
			"Honduras", // 68
			"Moldova", // 69
			"Costa Rica", // 70
			"Andorra", // 71
			"Paraguay", // 72
			"Panamá", // 73
			"", // 74
			"Crna Gora", // 75
			"Kenya", // 76
			"Guatemala", // 77
			"Daehan Minguk", // 78
			"Azərbaycan", // 79
			"Cameroun", // 80
			"Sénégal", // 81
			"Zhōngguó", // 82
			"Singapore", // 83
			"Việt Nam", // 84
			"Kypros", // 85
			"Īrān", // 86
			"Tūnis", // 87
			"as-Saʻūdiyya", // 88
			"Al Maghrib", // 89
			"Prathet Thai", // 90
			"", // 91
			"El Salvador", // 92
			"", // 93
			"", // 94
			"", // 95
			"Pilipinas", // 96
			"Cuba", // 97
			"", // 98
			"U.A.E.", // 99
			"Nicaragua", // 100
			"", // 101
			"", // 102
			"Kazakhstán", // 103
			"O‘zbekiston", // 104
			"al-Jazā’ir", // 105
			"Sakartvelo", // 106
	};

	public static void actualizar_jugador_remoto(WebClient navegador, Jugador j, URL url, Usuario usuario) throws FailingHttpStatusCodeException, IOException {
		String parametros;
		
		if (url.getHost().contains("ntdb.sokker.cz")) {
			// Con la otra no funciona, me dice BlueZero que use esta
			url = new URL("https://ntdb.sokker.cz/redir.php");
			
			parametros = "teamname=" + j.getEquipo()
			+ "&tid=" + j.getTid()
			+ "&playername=" + j.getNombre()
			+ "&countryid=" + j.getPais()
			+ "&pid=" + j.getPid()
			+ "&age=" + j.getEdad()
			+ "&val=" + j.getValor_original()
			+ "&wag=" + j.getSalario_pais()
			+ "&frm=" + j.getForma()
			+ "&tac=" + j.getDisciplina_tactica()
			+ "&hei=" + j.getAltura()
			+ "&sta=" + j.getCondicion()
			+ "&kee=" + j.getPorteria()
			+ "&pac=" + j.getRapidez()
			+ "&def=" + j.getDefensa()
			+ "&tec=" + j.getTecnica()
			+ "&pla=" + j.getCreacion()
			+ "&pas=" + j.getPases()
			+ "&str=" + j.getAnotacion()
			+ "&langcode=en"
			+ "&obs=Sent by " + usuario.getLogin_sokker() + " through Sokker Asistente"
			;
		} else {
			parametros = "teamname=" + usuario.getEquipo()
				+ "&tid=" + j.getTid()
				+ "&playername=" + j.getNombre()
				+ "&countryid=" + j.getPais()
				+ "&pid=" + j.getPid()
				+ "&age=" + j.getEdad()
				+ "&val=" + j.getValor_original()
		//		+ "&wag=" + j.getSueldo()
				+ "&frm=" + j.getForma()
				+ "&tac=" + j.getDisciplina_tactica()
		//		+ "&hei=" + j.getAltura()
				+ "&sta=" + j.getCondicion()
				+ "&kee=" + j.getPorteria()
				+ "&pac=" + j.getRapidez()
				+ "&def=" + j.getDefensa()
				+ "&tec=" + j.getTecnica()
				+ "&pla=" + j.getCreacion()
				+ "&pas=" + j.getPases()
				+ "&str=" + j.getAnotacion()
		//		+ "&langcode=" + j.getIdioma()
				+ "&obs=Sent by " + usuario.getLogin_sokker() + " through Sokker Asistente"
				;
		}
	
		WebRequest requestSettings = new WebRequest(url, HttpMethod.POST);
		requestSettings.setRequestBody(parametros);
		HtmlPage page = navegador.getPage(requestSettings);
	
		if (j.getPais() == 16) {
			//---------
			// Francia
			//---------
			poner_valor(page, "ID", j.getPid()+"");
			poner_valor(page, "skillStamina", j.getCondicion()+"");
			poner_valor(page, "skillKeeper", j.getPorteria()+"");
			poner_valor(page, "skillPace", j.getRapidez()+"");
			poner_valor(page, "skillDefending", j.getDefensa()+"");
			poner_valor(page, "skillTechnique", j.getTecnica()+"");
			poner_valor(page, "skillPlaymaking", j.getCreacion()+"");
			poner_valor(page, "skillPassing", j.getPases()+"");
			poner_valor(page, "skillScoring", j.getAnotacion()+"");
			//---------
		} else {
			poner_valor(page, "position", j.getDemarcacion() == null ? "" : j.getDemarcacion().name());
		}

		HtmlSubmitInput input = ((HtmlSubmitInput)page.getFirstByXPath("//input[@type='submit']"));
		if (input != null) {
			Page respuesta = input.click();
		} else {
			HtmlButton button = page.getFirstByXPath("//button[@type='submit']");
			
			if (button != null) {
				Page respuesta = button.click();	
			} else {
				// redir.php envía un formulario sin botón de submit (redirige en un onload, pero no parece que haga nada)
				button = (HtmlButton) page.createElement("button");
				button.setAttribute("type", "submit");
				HtmlForm form = page.getForms().get(0);
				form.appendChild(button);
				HtmlPage respuesta = button.click();

				// La página de respuesta es el formulario que vemos en el navegador. ¿Habrá que volver a hacer submit?
				respuesta.getForms().get(0).click();

//				if (j.getPais() == 1) {
//					System.out.println(j.getNombre());
//					System.out.println(respuesta.isHtmlPage() ? ((HtmlPage)respuesta).asXml() : respuesta);
//				}
				
			}

		}

	}
	
	public static void enviar_jugadores(WebClient navegador, Usuario usuario, List<Jugador> jugadores, int jornada_actual) throws IOException {
		HashMap<String, String> urls = Util.leer_hashmap("URLs");
		
		for (Jugador j : jugadores) {
			if (j.getPid() > 0) {
				URL url = null;
				try {
					// Si no hay definida una web para los sub21, nos quedamos con la de la NT
					String s = urls.get("NT_" + j.getPais());
					if (j.getEdad() <= 21 && urls.get("U21_" + j.getPais()) != null) {
						s = urls.get("U21_" + j.getPais());
					}
					url = new URL(s);
	
					if (url.getHost().contains("raqueto")) {
						// Actualización interna: duplico el jugador para que no afecte al del equipo del usuario
						Jugador copia = new Jugador(j);
						copia.copiar_valores_publicos(j);
						actualizar_jugador_local(copia, jornada_actual);
					} else {
						actualizar_jugador_remoto(navegador, j, url, usuario);
					}
				} catch (Exception e) {
					System.out.println("Error en " + j.getPais() + ": " + url);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Puede venir directamente de SA o bien del formulario de NTDB
	 * @param navegador
	 * @param j
	 * @param jornada_actual
	 * @throws IOException
	 */
	public static void actualizar_jugador_local(Jugador j, int jornada_actual) throws IOException {
		if (j.isActualizado()) {
			j.setNotas(NtdbBO.NOTAS_NTDB);
		} else if (NtdbBO.NOTAS_NTDB.equals(j.getNotas())) {
			// Evitamos que se simule una actualización fiable
			j.setNotas(null);
		}
		
		Integer id_pais = j.getPais() + (j.getEdad() > 21 ? 0 : NtdbBO.DIF_NT_U21);
		
		// Refresco del histórico
		List<Jugador> jugadores_historico = AsistenteBO.leer_jugadores(id_pais, null, true, null/*usuario*/);
		if (jugadores_historico.contains(j)) {
			Jugador historico = jugadores_historico.get(jugadores_historico.indexOf(j));
			j = AsistenteBO.combinar_jugadores(j, historico);
			jugadores_historico.remove(historico);
			jugadores_historico.add(j);

			AsistenteBO.grabar_jugadores(jugadores_historico, id_pais, jornada_actual, true);
		} else {
			// Actualizar actual
			List<Jugador> jugadores = AsistenteBO.leer_jugadores(id_pais, null, false, null/*usuario*/);
			if (jugadores.contains(j)) {
				Jugador antiguo = jugadores.get(jugadores.indexOf(j));
				j = AsistenteBO.combinar_jugadores(j, antiguo);
				jugadores.remove(antiguo);
			}
			jugadores.add(j);

			Collections.sort(jugadores, Jugador.getComparator());
			AsistenteBO.grabar_jugadores(jugadores, id_pais, jornada_actual, false);
		}
	}
	
	private static void poner_valor(HtmlPage page, String name, String valor) {
		HtmlSelect select = (HtmlSelect)page.getFirstByXPath("//select[@name='" + name + "']");
		if (select != null) {
			select.setSelectedAttribute(valor, true);
		} else {
			HtmlInput input = (HtmlInput)page.getFirstByXPath("//input[@name='" + name + "']");
			if (input != null) {
				input.setValueAttribute(valor);
			}
		}
	}
}