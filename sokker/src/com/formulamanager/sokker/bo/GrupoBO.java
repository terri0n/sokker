package com.formulamanager.sokker.bo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.formulamanager.sokker.entity.Equipo;
import com.formulamanager.sokker.entity.Grupo;
import com.formulamanager.sokker.entity.Jornada;
import com.formulamanager.sokker.entity.Partido;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;

public class GrupoBO {
	public static List<Grupo> crear_grupos(List<Integer> lista_ids, Integer equipos_grupo, Integer num_jornadas, boolean doble_vuelta, WebClient navegador) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		List<Grupo> grupos = new ArrayList<Grupo>();
		if (equipos_grupo == null) {
			equipos_grupo = lista_ids.size();
		}
		int num_grupos = ((lista_ids.size() - 1) / equipos_grupo) + 1;
		for (int i = 1; i <= num_grupos; i++) {
			grupos.add(new Grupo(i, num_jornadas != null ? num_jornadas : equipos_grupo - 1, null));
		}

		List<Equipo> equipos = new ArrayList<Equipo>();
		
		// Obtener equipos
		for (int tid : lista_ids) {
			Equipo e = Equipo.nuevo(tid, navegador);
			equipos.add(e);
		}

		Collections.sort(equipos, Equipo.getComparator_rank());

		// Asignar grupo
		int i = 0;
		for (Equipo e : equipos) {
			//---------
			if (i % num_grupos == 0) {
				System.out.println("Bombo " + (i / num_grupos + 1));
			}
			System.out.println(e.getNombre() + " [" + e.getTid() + "] : " + e.getRank());
			//--------

			Grupo g = grupos.get(i++ % num_grupos);
			e.setPosicion(g.getEquipos().size() + 1);
			g.getEquipos().add(e);

			// Cada posición en los grupos tendrá los equipos distribuidos de forma aleatoria
			if (i % num_grupos == 0) {
				Collections.shuffle(grupos);
			}
		}

		Collections.sort(grupos);
		
		// Crear emparejamientos
		for (Grupo grupo : grupos) {
			crear_emparejamientos(grupo, num_jornadas, doble_vuelta);
		}

		return grupos;
	}

	public static void crear_emparejamientos(Grupo grupo, Integer num_jornadas, boolean doble_vuelta) {
		List<Equipo> equipos = new ArrayList<Equipo>();
		equipos.addAll(grupo.getEquipos());
		
		if (num_jornadas == null) {
			// Todos contra todos
			List<Jornada> jornadas = grupo.getJornadas();

			if (equipos.size() % 2 == 1) {
				equipos.add(Equipo.DESCANSO);
			}

			num_jornadas = equipos.size() - 1;
			int contador1 = 0;
			int contador2 = num_jornadas - 1;
	
			for (int i = 0; i < num_jornadas; i++) {
				Jornada jornada = new Jornada(jornadas.size() + 1);
	
				int local = i % 2 == 0 ? num_jornadas : contador1;
				int visitante = i % 2 == 0 ? contador1 : num_jornadas;
				jornada.getPartidos().add(new Partido(equipos.get(local), equipos.get(visitante)));
				contador1 = ((contador1 + 1) % num_jornadas - 1) + 1;
				
				for (int j = 1; j < equipos.size() / 2; j++) {
					local = contador1;
					visitante = contador2;
					jornada.getPartidos().add(new Partido(equipos.get(local), equipos.get(visitante)));
	
					contador1 = (contador1 + 1) % num_jornadas;
					contador2 = (contador2 + num_jornadas - 1) % num_jornadas;
				}
				jornadas.add(jornada);
			}
		} else {
			// Liga tipo juveniles
			generar_jornada(grupo);
		}
	
		grupo.actualizar_posiciones();
	}

	/**
	 * Para liga tipo juveniles
	 * 
	 * @param grupo
	 */
	public static void generar_jornada(Grupo grupo) {
		Jornada jornada = new Jornada(grupo.getJornadas().size() + 1);

		List<Equipo> equipos = grupo.getEquipos();

		// Si el nº de equipos es múltiplo de 2 pero no de 4, creamos los grupos de distinto tamaño
		int num_equipos = equipos.size() / 2;
		if (num_equipos % 2 == 1) {
			num_equipos--;
		}
		List<Equipo> bombo1 = equipos.subList(0, num_equipos);
		List<Equipo> bombo2 = equipos.subList(num_equipos, equipos.size());
		
		Collections.shuffle(bombo1);
		Collections.shuffle(bombo2);
		
		for (int i = 0; i < bombo1.size() - 1; i += 2) {
			jornada.getPartidos().add(new Partido(bombo1.get(i), bombo1.get(i+1)));
		}
		
		for (int i = 0; i < bombo2.size() - 1; i += 2) {
			jornada.getPartidos().add(new Partido(bombo2.get(i), bombo2.get(i+1)));
		}
		
		grupo.getJornadas().add(jornada);
	}

	/**
	 * Para champions/uefa/otros
	 * 
	 * @param grupo
	 */
	public static void generar_jornadas_competicion(Grupo grupo) {
		int num_partidos = grupo.getEquipos().size() / 2;

		List<Integer> posiciones = new ArrayList<Integer>();
		crear_lista_emparejamientos(posiciones, grupo.getNum_jornadas() + 1);

		for (int i = 0; i < grupo.getNum_jornadas(); i++) {
			Jornada jornada = new Jornada(grupo.getJornadas().size() + 1);

			for (int j = 0; j < num_partidos; j++) {
				if (i == 0) {
					jornada.getPartidos().add(new Partido(grupo.getEquipos().get(posiciones.get(j * 2)), grupo.getEquipos().get(posiciones.get(j * 2 + 1))));
				} else {
					jornada.getPartidos().add(new Partido(Equipo.VACIO, Equipo.VACIO));
				}
			}

			num_partidos = num_partidos / 2;
			grupo.getJornadas().add(jornada);
		}
	}

	public static void crear_lista_emparejamientos(List<Integer> posiciones, int num_rondas) {
		if (num_rondas <= 1) {
			posiciones.add(0);
		} else {
			crear_lista_emparejamientos(posiciones, num_rondas - 1);

			int total = posiciones.size() * 2 - 1;
			for (int i = posiciones.size(); i > 0; i--) {
				posiciones.add(i, total - posiciones.get(i - 1));
			}
		}
	}
	
	public static void comprobar_partidos_competicion(Grupo grupo) {
		// Comprobamos los partidos finalizados
		for (Jornada j : grupo.getJornadas()) {
			// Nos saltamos la final
			if (j.getNum_partidos() > 1) {
				int i = 0;
				for (Partido p : j.getPartidos()) {
					if (p.getGoles_l() != null && p.getGoles_v() != null) {
						if (p.getGoles_l() != p.getGoles_v()) {
							Partido sig_partido = grupo.getJornadas().get(j.getNumero()).getPartidos().get(i / 2);
							Equipo equipo = p.getGoles_l() > p.getGoles_v() ? p.getLocal() : p.getVisitante();
							if (i % 2 == 0) {
								sig_partido.setLocal(equipo);
							} else {
								sig_partido.setVisitante(equipo);
							}
						}
					}
					i++;
				}
			}
		}
	}

//	public static void main(String[] args) {
//		List<Integer> posiciones = new ArrayList<Integer>();
//		crear_lista_emparejamientos(posiciones, 7);
//		for (int i = 0; i < posiciones.size(); i += 2) {
//			System.out.println((posiciones.get(i) + 1) + " VS " + (posiciones.get(i+1) + 1));
//		}
//	
//	}
}
