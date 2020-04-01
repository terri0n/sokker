package com.formulamanager.sokker.bo;

import java.net.URL;
import java.util.List;

import com.formulamanager.sokker.entity.Jugador;
import com.formulamanager.sokker.entity.Usuario;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * NOTA: solo he probado las dos webs mayoritarias:
 * - ntdb.sokker.cz
 * - online-manager.co
 */
public class NtdbBO {
	public static String urls[] = {
		"http://ntdb.sokker.cz/index.php",				// 1
		"http://ntdb.sokker.cz/index.php",				// 2
		"http://england.online-manager.co/plugin/",		// 3
		"http://ntdb-usa.online-manager.co/plugin/index.php", 	// 4
		"http://canada.online-manager.co/plugin/", 		// 5
		"http://ntdb.sokker.cz/index.php", 				// 6
		"http://scotland.online-manager.co/plugin/",	// 7
		"http://ntdb.sokker.cz/index.php", 				// 8
		"http://ntdb.sokker.cz/index.php", 				// 9
		"http://ntdb.sokker.cz/index.php",				// 10
		"http://ntdb.sokker.cz/index.php",				// 11
		"http://ntdb.sokker.cz/index.php", 				// 12
		"http://ostsokker.eu/ntdb/13", 					// 13
		"http://ostsokker.eu/ntdb/14", 					// 14
		"http://db.sokker-deutschland.de/indexsk.php", 	// 15
		"http://sokker-table.host56.com/osokker.php", 	// 16
		"http://ntdb.sokker.cz/index.php", 				// 17
		"http://ntdb.sokker.cz/index.php", 				// 18
		"http://ntdb.sokker.cz/index.php", 				// 19
		"http://ntdb.sokker.cz/index.php", 				// 20
		"http://ntdb.sokker.cz/index.php", 				// 21
		"http://ntdb.sokker.cz/index.php", 				// 22
		"http://ntdb.sokker.cz/index.php", 				// 23
		"http://swiss.sokker-deutschland.de/indexsk.php", 	// 24
		"http://ntdb.sokker.cz/index.php", 				// 25
		"http://ntdb.sokker.cz/index.php", 				// 26
		"http://ntdb.sokker.cz/index.php", 				// 27
		"http://ntdb.sokker.cz/index.php", 				// 28
		"http://ostsokker.tk/ntdb/29",	 				// 29
		"http://ntdb.loopsworld.at/indexsk.php", 		// 30
		"http://ntdb.sokker.cz/index.php", 				// 31
		"http://ntdb.sokker.cz/index.php", 				// 32
		"http://ntdb.sokker.cz/index.php",	 			// 33
		"http://ntdb.sokker.cz/index.php", 				// 34
		"http://ntdb.sokker.cz/index.php", 				// 35
		"http://ntdb.sokker.cz/index.php", 				// 36
		"http://ntdb.sokker.cz/index.php", 				// 37
		"http://ntdb.sokker.cz/index.php", 				// 38
		"http://ntdb.sokker.cz/index.php",			 	// 39
		"http://ntdb.sokker.cz/index.php", 				// 40
		"http://ntdb.sokker.cz/index.php", 				// 41
		"http://ntdb.sokker.cz/index.php", 				// 42
		"http://ntdb.sokker.cz/index.php", 				// 43
		"http://ntdb.sokker.cz/index.php", 				// 44
		"http://ntdb.sokker.cz/index.php", 				// 45
		"http://ntdb.sokker.cz/index.php", 				// 46
		"http://ntdb.sokker.cz/index.php", 				// 47
		"http://ntdb.sokker.cz/index.php", 				// 48
		"http://ntdb.sokker.cz/index.php", 				// 49
		"http://ntdb.sokker.cz/index.php", 				// 50
		"http://ntdb.sokker.cz/index.php", 				// 51
		"http://ntdb.sokker.cz/index.php?langcode=en&countryid=52", 	// 52
		"http://ntdb.sokker.cz/index.php", 				// 53
		"http://bulgaria.online-manager.co/plugin.php", // 54
		"http://ntdb.sokker.cz/index.php", 				// 55
		"http://ntdb.sokker.cz/index.php", 				// 56
		"http://ntdb.sokker.cz/index.php", 				// 57
		"http://ntdb.sokker.cz/index.php", 				// 58
		"http://ntdb.sokker.cz/index.php", 				// 59
		"http://hong-kong.online-manager.co/plugin/index.php", 	// 60
		"http://ntdb.sokker.cz/index.php", 				// 61
		"http://ntdb.sokker.cz/index.php?langcode=fi&countryid=62",	// 62
		"http://ntdb.sokker.cz/index.php", 				// 63
		"http://ukrsokker.000webhostapp.com/",			// 64
		"http://ntdb.sokker.cz/index.php", 				// 65
		"http://ntdb.sokker.cz/index.php", 				// 66
		"http://ntdb.sokker.cz/index.php", 				// 67
		"http://honduras.online-manager.co/",			// 68
		"http://ntdb.sokker.cz/index.php",				// 69
		"http://ntdb.sokker.cz/index.php", 				// 70
		"http://ntdb.sokker.cz/index.php", 				// 71
		"http://ntdb.sokker.cz/index.php", 				// 72
		"http://ntdb.sokker.cz/index.php", 				// 73
		null,
		"http://ntdb.sokker.cz/index.php", 				// 75
		"http://ntdb.sokker.cz/index.php", 				// 76
		"http://ntdb.sokker.cz/index.php", 				// 77
		"http://ntdb.sokker.cz/index.php", 				// 78
		"http://ntdb.sokker.cz/index.php", 				// 79
		"http://ntdb.sokker.cz/index.php", 				// 80
		"http://ntdb.sokker.cz/index.php", 				// 81
		"http://ntdb.sokker.cz/index.php", 				// 82
		"http://ntdb.sokker.cz/index.php", 				// 83
		"http://ntdb.sokker.cz/index.php", 				// 84
		"http://ntdb.sokker.cz/index.php", 				// 85
		"http://ntdb.sokker.cz/index.php", 				// 86
		"http://ntdb.sokker.cz/index.php", 				// 87
		"http://ntdb.sokker.cz/index.php", 				// 88
		"http://ntdb.sokker.cz/index.php", 				// 89
		"http://ntdb.sokker.cz/index.php", 				// 90
		null,							 				// 91 -> tenían ntdb.sokker.cz pero la bandera no existe. Lo quito
		"http://ntdb.sokker.cz/index.php", 				// 92
		null,	
		null,
		null,	
		"http://ntdb.sokker.cz/index.php", 				// 96
		"http://ntdb.sokker.cz/index.php", 				// 97
		null,	
		"http://ntdb.sokker.cz/index.php",				// 99
		"http://ntdb.sokker.cz/index.php",				// 100
		null,	
		null,	
		"http://ntdb.sokker.cz/index.php",				// 103
		"http://ntdb.sokker.cz/index.php",				// 104
		"http://ntdb.sokker.cz/index.php",				// 105
		"http://ntdb.sokker.cz/index.php",				// 106
	};

	public static void enviar_jugadores(WebClient navegador, Usuario usuario, List<Jugador> jugadores) {
		for (Jugador j : jugadores) {
			try {
				String parametros = "teamname=" + usuario.getEquipo()
						+ "&tid=" + j.getTid()
						+ "&playername=" + j.getNombre()
						+ "&countryid=" + j.getPais()
						+ "&pid=" + j.getPid()
						+ "&age=" + j.getEdad()
						+ "&val=" + j.getValor_original()
//						+ "&wag=" + j.getSueldo()
						+ "&frm=" + j.getForma()
						+ "&tac=" + j.getDisciplina_tactica()
//						+ "&hei=" + j.getAltura()
						+ "&sta=" + j.getCondicion()
						+ "&kee=" + j.getPorteria()
						+ "&pac=" + j.getRapidez()
						+ "&def=" + j.getDefensa()
						+ "&tec=" + j.getTecnica()
						+ "&pla=" + j.getCreacion()
						+ "&pas=" + j.getPases()
						+ "&str=" + j.getAnotacion()
//						+ "&langcode=" + j.getIdioma()
						;
				
				URL url = new URL(urls[j.getPais() - 1]);
				WebRequest requestSettings = new WebRequest(url, HttpMethod.POST);
				requestSettings.setRequestBody(parametros);
				
				HtmlPage page = navegador.getPage(requestSettings);
				HtmlSelect position = (HtmlSelect)page.getFirstByXPath("//select[@name='position']");
				if (position != null) {
					position.setSelectedAttribute(j.getDemarcacion().name(), true);
				}
				
				HtmlPage respuesta = ((HtmlSubmitInput)page.getFirstByXPath("//input[@type='submit']")).click();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}