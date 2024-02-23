package com.formulamanager.sokker.bo;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;

import com.formulamanager.sokker.entity.Equipo;
import com.formulamanager.sokker.entity.Liga;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class LigaBO {
	public static Liga obtener_equipos_liga(WebClient navegador, int pais, int division, int numero_liga) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Liga liga = null;
		
		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/league-" + pais + "-" + division + "-" + numero_liga + ".xml");
		if (pagina.getFirstByXPath("//league/info/leagueID/text()") != null) {
			int id = Integer.valueOf(((DomText) pagina.getFirstByXPath("//league/info/leagueID/text()")).asText());
			String nombre_liga = ((DomText)pagina.getFirstByXPath("//league/info/name/text()")).asText();

			liga = new Liga();
			liga.setDivision(division);
			liga.setNumero(numero_liga);
			liga.setNombre(nombre_liga);
			
			pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/league-" + id + ".xml");
			ArrayList<DomNode> nodoEquipos = (ArrayList<DomNode>) pagina.getByXPath("//league/teams/team");
			
			for (int i = 0; i < nodoEquipos.size(); i++) {
				DomNode nodoEquipo = nodoEquipos.get(i);
				int tid = Integer.valueOf(((DomText) nodoEquipo.getFirstByXPath("teamID/text()")).asText());
				int puntos = Integer.valueOf(((DomText) nodoEquipo.getFirstByXPath("points/text()")).asText());
				int gf = Integer.valueOf(((DomText) nodoEquipo.getFirstByXPath("goalsScored/text()")).asText());
				int gc = Integer.valueOf(((DomText) nodoEquipo.getFirstByXPath("goalsLost/text()")).asText());
				BigDecimal rank = new BigDecimal(((DomText) nodoEquipo.getFirstByXPath("rankTotal/text()")).asText());

				Equipo equipo = new Equipo();
				equipo.setTid(tid);
				equipo.setPuntos(puntos);
				equipo.setGf(gf);
				equipo.setGc(gc);
				equipo.setPosicion(i + 1);
				equipo.setLiga(liga);
				equipo.setRank(rank);
				
				liga.getEquipos().add(equipo);
			}
		}
		
		return liga;
	}
}
