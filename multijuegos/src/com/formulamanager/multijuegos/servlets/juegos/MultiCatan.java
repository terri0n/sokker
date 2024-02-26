package com.formulamanager.multijuegos.servlets.juegos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MultiCatan
 */
@WebServlet("/multicatan")
public class MultiCatan extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public Integer[][] mapa;
    public int[][] numeros;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MultiCatan() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		mapa = new Integer[][] {
	    		{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
	    		{0,9,9,9,9,0,0,9,9,9,9,0,0,9,9,9,9,0},
	    		{0,0,9,0,9,0,0,0,9,0,9,0,0,0,9,0,9,0},
	    		{9,9,0,9,0,9,9,9,0,9,0,9,9,9,0,9,0,9},
	    		{9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9},
	    		{9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9},
	    		{9,0,0,0,0,0,9,0,0,0,0,0,9,0,0,0,0,0},
	    		{0,9,9,9,9,0,0,9,9,9,9,0,0,9,9,9,9,0},
	    		{0,0,9,0,9,0,0,0,9,0,9,0,0,0,9,0,9,0},
	    		{0,null,0,null,0,null,0,null,0,null,0,null,0,null,0,null,0}
	    };
	    numeros = new int[mapa.length][mapa[0].length];

		for (int isla = 0; isla < 3; isla ++) {
		    crear_isla_principal(isla);
	    	List<Integer> recursos_resto = new ArrayList<>();
	    	List<Integer> numeros_resto = new ArrayList<>();
	    	rellenar_islas_perifericas(isla, recursos_resto, numeros_resto);
	    	rellenar_isla_principal(isla, recursos_resto, numeros_resto);
	    }

//    	for (int i = 0; i < mapa.length; i++) {
//    		System.out.println(Arrays.asList(mapa[i]));
//    	}

    	request.setAttribute("mapa", mapa);
	    request.setAttribute("numeros", numeros);
	    
		request.getRequestDispatcher("/jsp/multicatan.jsp").forward(request, response);
	}

	private void rellenar_isla_principal(int isla, List<Integer> recursos_resto, List<Integer> numeros_resto) {
		// Un número se quedará fuera: debe ser un 3, 4, 10 u 11
		while (!Arrays.asList(3, 4, 10, 11).contains(numeros_resto.get(0))) {
			int aux = numeros_resto.remove(0);
			numeros_resto.add(aux);
		}
		numeros_resto.remove(0);

		// Añado más recursos, mínimo dos de cada
	    for (int i = 1; i < 6; i++) {
	    	recursos_resto.add(i);
	    	recursos_resto.add(i);
	    	recursos_resto.add((int)(Math.random() * 5) + 1);
	    	recursos_resto.add((int)(Math.random() * 5) + 1);
	    }
	    Collections.shuffle(recursos_resto);

		boolean repetir_todo;
		List<Integer> numeros_centro = new ArrayList<>();
		numeros_centro.addAll(numeros_resto);
		List<Integer> recursos_centro = new ArrayList<>();
		recursos_centro.addAll(recursos_resto);

		do {
			repetir_todo = false;

			for (int i = isla * 6; i < isla * 6 + 6; i++) {
				for (int j = 3; j <= 6; j++) {
					if (mapa[j][i] != null && mapa[j][i] > 0) {
						int repeticiones = 0;
						do {
							mapa[j][i] = recursos_centro.remove(0);
							if (comprobar_triangulo(j, i) || comprobar_recursos(j, i, mapa[j][i], new HashSet<String>())) {
								// Si se forma un triángulo o 4 recursos anidados...
								recursos_centro.add(mapa[j][i]);
								repeticiones++;
							} else {
								numeros[j][i] = numeros_centro.remove(0);

								if (comprobar_numeros(j, i)) {
									// o se juntan dos números rojos
									recursos_centro.add(mapa[j][i]);
									if (numeros_centro.size() > 1) {
										numeros_centro.add(2, numeros[j][i]);
									} else {
										numeros_centro.add(numeros[j][i]);
									}
									repeticiones++;
								} else {
									repeticiones = 0;
								}
							}
							
							if (repeticiones > numeros_centro.size()) {
								// Reseteo el mapa para que las comprobaciones sean correctas
								for (int i2 = isla * 6; i2 < isla * 6 + 6; i2++) {
									for (int j2 = 3; j2 <= 6; j2++) {
										if (mapa[j2][i2] > 0) {
											mapa[j2][i2] = 9;
										}
										numeros[j2][i2] = 0;
									}
								}
								numeros_centro.clear();
								numeros_centro.addAll(numeros_resto);
								Collections.shuffle(numeros_centro);
								recursos_centro.clear();
								recursos_centro.addAll(recursos_resto);
								Collections.shuffle(recursos_centro);
								repetir_todo = true;
								break;
							}
						} while (repeticiones > 0);
					}
					if (repetir_todo) {
		    			break;
		    		}
	    		}
				if (repetir_todo) {
	    			break;
	    		}
	    	}
		} while (repetir_todo);
	}

	public boolean comprobar_numeros(int j, int i) {
		if (es_rojo(numeros[j][i])) {
			return es_rojo(numeros[j - 1][i])
				|| es_rojo(numeros[j - 1 + (i&1)][(i + numeros[0].length - 1) % numeros[0].length])
				|| es_rojo(numeros[j + (i&1)][(i + numeros[0].length - 1) % numeros[0].length])
				|| es_rojo(numeros[j + 1][i])
				|| es_rojo(numeros[j + (i&1)][(i + 1) % numeros[0].length])
				|| es_rojo(numeros[j - 1 + (i&1)][(i + 1) % numeros[0].length]);
		} else {
			return false;
		}
	}

	public boolean comprobar_triangulo(int j, int i) {
		boolean c1 = mapa[j][i] == mapa[j - 1][i];
		boolean c2 = mapa[j][i] == mapa[j - 1 + (i&1)][(i + mapa[0].length - 1) % mapa[0].length];
		boolean c3 = mapa[j][i] == mapa[j + (i&1)][(i + mapa[0].length - 1) % mapa[0].length];
		boolean c4 = mapa[j][i] == mapa[j + 1][i];
		boolean c5 = mapa[j][i] == mapa[j + (i&1)][(i + 1) % mapa[0].length];
		boolean c6 = mapa[j][i] == mapa[j - 1 + (i&1)][(i + 1) % mapa[0].length];

		return c1 && c2 || c2 && c3 || c3 && c4 || c4 && c5 || c5 && c6 || c6 && c1;
	}
	
	public boolean comprobar_recursos(int j, int i, int n, HashSet<String> set) {
		if (!set.contains(j+"_"+i)) {
			if (mapa[j][i] == n) {
				set.add(j+"_"+i);
				comprobar_recursos(j - 1, i, n, set);
				comprobar_recursos(j - 1 + (i&1), (i + mapa[0].length - 1) % mapa[0].length, n, set);
				comprobar_recursos(j + (i&1), (i + mapa[0].length - 1) % mapa[0].length, n, set);
				comprobar_recursos(j + 1, i, n, set);
				comprobar_recursos(j + (i&1), (i + 1) % mapa[0].length, n, set);
				comprobar_recursos(j - 1 + (i&1), (i + 1) % mapa[0].length, n, set);
			}
		}
		return set.size() > 3;
	}
	
	private void rellenar_islas_perifericas(int isla, List<Integer> recursos_resto, List<Integer> numeros_resto) {
	    // Añado un recurso de cada tipo + otro al azar
	    for (int i = 1; i < 6; i++) {
	    	recursos_resto.add(i);
	    }
	    recursos_resto.add((int)(Math.random() * 5) + 1);
	    Collections.shuffle(recursos_resto);
	    
	    List<Integer> inicio;
	    do {
		    numeros_resto.clear();
		    numeros_resto.addAll(Arrays.asList(2,3,3,3,4,4,4,5,5,5,6,6,6,8,8,8,9,9,9,10,10,10,11,11,11,12));
		    Collections.shuffle(numeros_resto);
		    inicio = new ArrayList<>();
		    
		    for (int i = 0; i < 6; i++) {
		    	while (inicio.contains(numeros_resto.get(0))) {
		    		int n = numeros_resto.remove(0);
		    		numeros_resto.add(n);
		    	}
		    	inicio.add(numeros_resto.remove(0));
		    }
	    } while (!validar(inicio));

		while (es_rojo(inicio.get(0)) && es_rojo(inicio.get(1)) ||
				es_rojo(inicio.get(0)) && es_rojo(inicio.get(2)) ||
				es_rojo(inicio.get(1)) && es_rojo(inicio.get(2)) ||
				es_rojo(inicio.get(1)) && es_rojo(inicio.get(3)) ||
				es_rojo(inicio.get(2)) && es_rojo(inicio.get(3)) ||
				es_rojo(inicio.get(3)) && es_rojo(inicio.get(4)) ||
				es_rojo(inicio.get(3)) && es_rojo(inicio.get(5)) ||
				es_rojo(inicio.get(4)) && es_rojo(inicio.get(5))) {
			Collections.shuffle(inicio);
		}

		for (int i = 1 + 6 * isla; i < 5 + 6 * isla; i++) {
			for (int j = 1; j < 3; j++) {
				if (mapa[j][i] > 0) {
					mapa[j][i] = recursos_resto.remove(0);
					numeros[j][i] = inicio.remove(0);
					
					mapa[9 - j - (i & 1)][i] = mapa[j][i];
					numeros[9 - j - (i & 1)][i] = numeros[j][i];
					
					// Si el nº es un 2 o un 12, como solo hay uno de cada, lo duplicamos y quitamos el otro
					numeros_resto.remove((Object)(numeros[j][i] == 2 ? 12 : numeros[j][i] == 12 ? 2 : numeros[j][i]));
				}
			}
		}
	}

	private void crear_isla_principal(int isla) {
		// Borramos 2 bloques del área de cada jugador
	    for (int n = 0; n < 2; n++) {
		    List<Integer> bloques = new ArrayList<Integer>();
		    bloques.addAll(Arrays.asList(0, 1, 2, 3, 4, 5));
		    Collections.shuffle(bloques);

		    for (int i = 0; i < 2; i++) {
			    int ii = (Integer) bloques.remove(0) + 6 * isla;
	
		    	for (int j = 0; j < 2; j++) {
		    		int jj = n == 0 ? 3 + j - (ii & 1) : 6 - j;
		    		if (mapa[jj][ii] > 0) {
		    			mapa[jj][ii] = 0;
		    			break;
		    		}
		    	}
			}
	    }
	}

	private boolean validar(List<Integer> array) {
    	int total = 0;
    	for (Integer i : array) {
    		total += Math.abs(7 - i);
    	}
    	return total == 15;
	}
	
	private boolean es_rojo(int i) {
		return i == 6 || i == 8;
	}
	 
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
