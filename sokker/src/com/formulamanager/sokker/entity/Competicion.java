package com.formulamanager.sokker.entity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.formulamanager.sokker.bo.CompeticionBO;
import com.formulamanager.sokker.bo.GrupoBO;
import com.formulamanager.sokker.entity.Equipo.TIPO_COMPETICION;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

public class Competicion {
	private List<Grupo> grupos;
	private int equipos_champions;
	private int equipos_uefa;
	private int equipos_cp;

	private List<Grupo> competiciones;

	public Competicion() {
		grupos = new ArrayList<Grupo>();
		competiciones = new ArrayList<Grupo>();
	}

	public Competicion(List<Integer> lista_ids, Integer equipos_grupo, Integer num_jornadas, int equipos_champions, int equipos_uefa, Integer equipos_cp, boolean doble_vuelta, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		this.equipos_champions = equipos_champions;
		this.equipos_uefa = equipos_uefa;
		this.equipos_cp = equipos_cp != null ? equipos_cp : CompeticionBO.menorOIgualMultiplo2(lista_ids.size() - equipos_champions - equipos_uefa);
		
		grupos = GrupoBO.crear_grupos(lista_ids, equipos_grupo, num_jornadas, doble_vuelta, navegador);
		competiciones = new ArrayList<Grupo>();
		asignar_competiciones();
	}
	
	public void asignar_competiciones() {
		List<Equipo> lista_equipos = new ArrayList<Equipo>();
		for (Grupo g : grupos) {
			lista_equipos.addAll(g.getEquipos());
		}
		
		Collections.sort(lista_equipos, Equipo.getComparator_competicion());
		
		for (int i = 0; i < lista_equipos.size(); i++) {
			if (i < equipos_champions) {
				lista_equipos.get(i).setTipo_competicion(TIPO_COMPETICION.CHAMPIONS);
			} else if (i < equipos_champions + equipos_uefa) {
				lista_equipos.get(i).setTipo_competicion(TIPO_COMPETICION.UEFA);
			} else if (i < equipos_champions + equipos_uefa + equipos_cp) {
				lista_equipos.get(i).setTipo_competicion(TIPO_COMPETICION.OTROS);
			} else {
				lista_equipos.get(i).setTipo_competicion(null);
			}
		}
	}

	public void actualizar_clasificaciones() {
		for (Grupo g : grupos) {
			g.actualizar_clasificacion();
		}
		
		asignar_competiciones();
	}

	public void comprobar_fin_jornada() {
		boolean nueva = false;

		for (Grupo g : grupos) {
			nueva |= g.comprobar_fin_jornada();
		}
		
		if (nueva) {
			actualizar_clasificaciones();
		}

		for (Grupo g : competiciones) {
			GrupoBO.comprobar_partidos_competicion(g);
		}
	}
	
	public void comprobar_fin_liga() {
		if (competiciones.size() == 0) {
			for (Grupo g : grupos) {
				if (!g.grupo_termuinado()) {
					return;
				}
			}
			
			competiciones.add(crear_competicion(TIPO_COMPETICION.CHAMPIONS));
			competiciones.add(crear_competicion(TIPO_COMPETICION.UEFA));
			competiciones.add(crear_competicion(TIPO_COMPETICION.OTROS));
		}
	}

	public List<Equipo> getEquipos_competicion(TIPO_COMPETICION tipo_competicion) {
		List<Equipo> equipos = new ArrayList<Equipo>();
		
		for (Grupo g : grupos) {
			for (Equipo e : g.getEquipos()) {
				if (e.getTipo_competicion() == tipo_competicion) {
					equipos.add(e);
				}
			}
		}
		
		// Los ordenamos
		Collections.sort(equipos, Equipo.getComparator_competicion());
		
		// Quitamos los últimos equipos hasta que el nº sea una potencia de 2
		int num_equipos = (int) Math.pow(2, (int) (Math.log(equipos.size()) / Math.log(2)));

		while (equipos.size() > num_equipos) {
			equipos.remove(equipos.size() - 1);
		}
		
		return equipos;
	}
	
	public Grupo crear_competicion(TIPO_COMPETICION tipo_competicion) {
		List<Equipo> equipos = getEquipos_competicion(tipo_competicion);
		
		int jornadas = (int) (Math.log(equipos.size()) / Math.log(2));
		Grupo grupo = new Grupo(tipo_competicion.ordinal() + 1, jornadas, tipo_competicion);
		grupo.setEquipos(equipos);
		
		GrupoBO.generar_jornadas_competicion(grupo);
		
		return grupo;
	}

	//-----
	// G&S
	//-----
	
	public List<Grupo> getGrupos() {
		return grupos;
	}

	public void setGrupos(List<Grupo> grupos) {
		this.grupos = grupos;
	}

	public int getEquipos_champions() {
		return equipos_champions;
	}

	public void setEquipos_champions(int equipos_champions) {
		this.equipos_champions = equipos_champions;
	}

	public int getEquipos_uefa() {
		return equipos_uefa;
	}

	public void setEquipos_uefa(int equipos_uefa) {
		this.equipos_uefa = equipos_uefa;
	}

	public List<Grupo> getCompeticiones() {
		return competiciones;
	}

	public void setCompeticiones(List<Grupo> competiciones) {
		this.competiciones = competiciones;
	}

	public int getEquipos_cp() {
		return equipos_cp;
	}

	public void setEquipos_cp(int equipos_cp) {
		this.equipos_cp = equipos_cp;
	}
}
