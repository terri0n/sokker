package com.formulamanager.sokker.entity;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.formulamanager.sokker.bo.AsistenteBO;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

public class Pais implements Comparable<Pais> {
	public static BigDecimal[] currency_rates = new BigDecimal[] {
		new BigDecimal("1"), 		// 1
		new BigDecimal("2.5"), 		// 2
		new BigDecimal("6.66667"), 	// 3
		new BigDecimal("4"), 		// 4
		new BigDecimal("2.5"), 		// 5
		new BigDecimal("2.5"),		// 6
		new BigDecimal("6.66667"), 	// 7
		new BigDecimal("0.02"), 	// 8
		new BigDecimal("1"), 		// 9
		new BigDecimal("4"), 		// 10
		new BigDecimal("0.04"), 	// 11
		new BigDecimal("4"), 		// 12
		new BigDecimal("0.133333"), // 13
		new BigDecimal("4"), 		// 14
		new BigDecimal("4"), 		// 15
		new BigDecimal("4"), 		// 16
		new BigDecimal("4"), 		// 17
		new BigDecimal("0.333333"), // 18
		new BigDecimal("4"), 		// 19
		new BigDecimal("4"), 		// 20
		new BigDecimal("1"), 		// 21
		new BigDecimal("0.5"), 		// 22
		new BigDecimal("4"), 		// 23
		new BigDecimal("2.5"), 		// 24
		new BigDecimal("4"), 		// 25
		new BigDecimal("4"), 		// 26
		new BigDecimal("0.5"), 		// 27
		new BigDecimal("4"), 		// 28
		new BigDecimal("0.5"), 		// 29
		new BigDecimal("4"), 		// 30
		new BigDecimal("0.5"), 		// 31
		new BigDecimal("4"), 		// 32
		new BigDecimal("4"), 		// 33
		new BigDecimal("0.5"), 		// 34
		new BigDecimal("0.125"), 	// 35
		new BigDecimal("1.25"), 	// 36
		new BigDecimal("1"), 		// 37
		new BigDecimal("2.22222"), 	// 38
		new BigDecimal("0.05"), 	// 39
		new BigDecimal("6.66667"), 	// 40
		new BigDecimal("2.5"), 		// 41
		new BigDecimal("5"), 		// 42
		new BigDecimal("0.125"),	// 43
		new BigDecimal("1.25"), 	// 44
		new BigDecimal("4"), 		// 45
		new BigDecimal("1.25"), 	// 46
		new BigDecimal("2"), 		// 47
		new BigDecimal("0.5"), 		// 48
		new BigDecimal("4"), 		// 49
		new BigDecimal("0.32"), 	// 50
		new BigDecimal("0.08"), 	// 51
		new BigDecimal("0.666667"), // 52
		new BigDecimal("0.04"), 	// 53
		new BigDecimal("2"), 		// 54
		new BigDecimal("4"), 		// 55
		new BigDecimal("0.05"),		// 56
		new BigDecimal("0.4"), 		// 57
		new BigDecimal("4"), 		// 58
		new BigDecimal("0.8"), 		// 59
		new BigDecimal("0.4"), 		// 60
		new BigDecimal("0.1"), 		// 61
		new BigDecimal("0.025"), 	// 62
		new BigDecimal("0.05"), 	// 63
		new BigDecimal("0.625"), 	// 64
		new BigDecimal("0.4"), 		// 65
		new BigDecimal("8"), 		// 66
		new BigDecimal("4"), 		// 67
		new BigDecimal("0.2"), 		// 68
		new BigDecimal("0.25"), 	// 69
		new BigDecimal("6.25"), 	// 70
		new BigDecimal("4"), 		// 71
		new BigDecimal("0.5"), 		// 72
		new BigDecimal("4"), 		// 73
		null,
		new BigDecimal("4"), 		// 75
		new BigDecimal("0.04"), 	// 76
		new BigDecimal("0.4"), 		// 77
		new BigDecimal("3.2"), 		// 78
		new BigDecimal("3.2"), 		// 79
		new BigDecimal("6.25"), 	// 80
		new BigDecimal("6.25"), 	// 81
		new BigDecimal("0.4"), 		// 82
		new BigDecimal("2"), 		// 83
		new BigDecimal("0.2"), 		// 84
		new BigDecimal("4"), 		// 85
		new BigDecimal("0.25"), 	// 86
		new BigDecimal("2"), 		// 87
		new BigDecimal("0.78125"), 	// 88
		new BigDecimal("0.32"), 	// 89
		new BigDecimal("0.08"), 	// 90
		null,
		new BigDecimal("4"), 		// 92
		null,
		null,
		null,
		new BigDecimal("0.625"),	// 96
		new BigDecimal("2.5"), 		// 97
		null,
		new BigDecimal("0.625"), 	// 99
		new BigDecimal("0.2"), 		// 100
		null,
		null,
		new BigDecimal("0.025"), 	// 103
		new BigDecimal("2.5"), 		// 104
		new BigDecimal("0.05"), 	// 105
		new BigDecimal("2") 		// 106	
	};

	private String nombre;
	private Integer id;
	private BigDecimal currency_rate;

	public Pais (String nombre, Integer id) {
		this.nombre = nombre;
		this.setId(id);
	}
	
	public static List<Pais> obtener_paises(WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		List<Pais> lista_paises = new ArrayList<Pais>();

		XmlPage pagina = navegador.getPage(AsistenteBO.SOKKER_URL + "/xml/countries.xml");
		
		List<DomNode> paises = (List<DomNode>) pagina.getByXPath("//country");
		for (DomNode pais : paises) {
			DomText nombre = (DomText) pais.getFirstByXPath("name/text()");
			DomText id = (DomText) pais.getFirstByXPath("countryID/text()");
			DomText currency_rate = (DomText) pais.getFirstByXPath("currencyRate/text()");
			Pais p = new Pais (nombre.asText(), Integer.valueOf(id.asText()));
			p.setCurrency_rate(new BigDecimal(currency_rate.asText()));
			lista_paises.add(p);
		}
		return lista_paises;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public static Comparator<? super Pais> getComparator() {
		return new Comparator<Pais>() {
			@Override
			public int compare(Pais o1, Pais o2) {
				return o1.nombre.compareTo(o2.nombre);
			}
		};
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public BigDecimal getCurrency_rate() {
		return currency_rate;
	}

	public void setCurrency_rate(BigDecimal currency_rate) {
		this.currency_rate = currency_rate;
	}

	@Override
	public int compareTo(Pais p) {
		return getId().compareTo(p.getId());
	}


}
