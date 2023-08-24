package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph <Fermata, DefaultEdge> grafo;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;	//dall'idFermata prendo l'oggetto Fermata
	
	
	public void creaGrafo() {
		
		//crea l'ogetto grafo
		this.grafo = new SimpleGraph<Fermata, DefaultEdge> (DefaultEdge.class);
		
		//aggiungo i vertici
		MetroDAO dao = new MetroDAO();
		this.fermate = dao.readFermate();
		
		//mappa che associa idFermata alla fermata -> mi permette di fare una query più semplice
		fermateIdMap = new HashMap<>();
		for (Fermata f: this.fermate) {
			this.fermateIdMap.put(f.getIdFermata(), f);
		}
		
		Graphs.addAllVertices(this.grafo, this.fermate);
		
		
		//aggiungo gli archi
		/**
		 * METODO 1: ITERO SU OGNI COPPIA DI VERTICI, LENTO MA FUNZIONA SEMPRE.
		 */
/*			for (Fermata partenza: this.grafo.vertexSet()) {
				for (Fermata arrivo: this.grafo.vertexSet()) {	//c'è un arco?
					if (dao.isConnesse(partenza, arrivo)){	//così vedo anche la coppia inversa A-B B-A
						this.grafo.addEdge(partenza, arrivo);
					}
				}
}*/
		
		
		/**
		 * METODO 2: DATO CIASCUN VERTICE, TROVA QUELLI AD ESSO ADIACENTI
		 * 
		 * per poter fare l'add edge dei collegare l'id all'oggetto fermata
		 * ho due strade:  2a) ritrovo l'oggetto a partire dall'id
		 * 				   2b) il DAO mi restituisce elenco di oggetti di tipo fermata	
		 * 		           2c) il DAO mi restituisce un elenco di ID numerici, che conferto in oggetti
		 * 					   tramite una Map<Integer, Fermata> -> Identity Map
		 * 		 
		 * Posso iterare su fermate oppure su this.grafo.vertexSet()
		 */
		
		//2a)
	/*	for (Fermata partenza : fermate) {
			List<Integer> idConnesse = dao.getIdFermateConesse(partenza);
			for (Integer id: idConnesse) {
				//fermata arrivo = fermata che continene questo id
				//scandire la lista delle fermate e ceerco l'id
				Fermata arrivo = null;
				for (Fermata f: fermate) {
					if (f.getIdFermata()==id) {
						arrivo = f;
						break;
					}
				}
			    this.grafo.addEdge(partenza, arrivo);
			}
		}*/
		
		//2b)
	/*	for (Fermata partenza : fermate) {
			List<Fermata> arrivi = dao.getFermateConnesse(partenza);
			for (Fermata arrivo: arrivi) {
				this.grafo.addEdge(partenza, arrivo); //passo 2 oggetti di tipo fermata
			}
		}*/
		
		//2c) METODO MIGLIORE
		
		for (Fermata partenza: fermate) {
			List<Integer> idConnesse = dao.getIdFermateConesse(partenza);
			for (int id: idConnesse) {
				Fermata arrivo = fermateIdMap.get(id);
				this.grafo.addEdge(partenza, arrivo);
			}
		}
		
		/**
		 * METODO 3: faccio una sola query che mi restituisca le coppie di fermate da collegare
		 * 			 e applico gli archi
		 * 
		 * 	USARE MAPPA
		 */
		
		List<CoppiaId> fermateDaCollegare =dao.getAllFermateConnesse();
		for (CoppiaId coppia: fermateDaCollegare) {
			this.grafo.addEdge(
					fermateIdMap.get(coppia.getIdPartenza()),
					fermateIdMap.get(coppia.getIdArrivo()));
		
		}
		
		
		System.out.println(this.grafo);
		System.out.println("Vertici: " + this.grafo.vertexSet().size());
		System.out.println("Archi: " + this.grafo.edgeSet().size());
		
//		visitaGrafo(fermate.get(0));
	}
	
//	public void visitaGrafo(Fermata partenza) {
//		GraphIterator <Fermata, DefaultEdge> visita = 
//				new BreadthFirstIterator<> (this.grafo, partenza);
//		while (visita.hasNext()) {
//			Fermata f = visita.next();
//			System.out.println(f);
//		}	
//	}
	
	/**
	 * DETERMINA IL PERCORSO MINIMO TRA LE DUE FERMATE AVENDO IL GRAFO GIA' CREATO
	 * @param partenza
	 * @param arrivo
	 * @return
	 */
	public List<Fermata> percorso(Fermata partenza, Fermata arrivo) {
		//VISITA PARTENDO DA PARTENZA
		//L'iteratore ora e' sul primo vertice
		BreadthFirstIterator <Fermata, DefaultEdge> visita = new BreadthFirstIterator<>(this.grafo, partenza);
		List<Fermata> raggiungibili = new ArrayList<Fermata>();
		
		while(visita.hasNext()) {	//FINCHE' CI SONO ANCORA VERTICI DA SCOPRIRE
			Fermata f = visita.next();	//DAMMI IL PROX VERTICE 
//			raggiungibili.add(f);	//AGGIUNGILO A QUELLI RAGGIUNGIBILI
		}
//		System.out.println(raggiungibili);
//		return null;
		
		//TROVA IL PERCORSO SULL'ALBERO DI VISITA -> il percorso all'indietro è univoco
		List<Fermata> percorso = new ArrayList<Fermata>();
		Fermata corrente = arrivo;
		percorso.add(arrivo);
		DefaultEdge e = visita.getSpanningTreeEdge(corrente);	//corrente è il vertice finale, dammi l'arco con cui ci sei arrivato
		while (e!=null) {
			Fermata precedente = Graphs.getOppositeVertex(this.grafo, e, corrente);
			percorso.add(0, precedente);	//ordino da sopra a sottotube 
			corrente = precedente; 	//ripeto
			e = visita.getSpanningTreeEdge(corrente);	//corrente è il vertice finale, dammi l'arco con cui ci sei arrivato
		}
		return percorso;
	}

	public List<Fermata> getAllFermate(){
		MetroDAO dao = new MetroDAO();
		return dao.readFermate();
	}
	
	public boolean isGrafoLoader() {
		return this.grafo.vertexSet().size()>0;
	}
}
