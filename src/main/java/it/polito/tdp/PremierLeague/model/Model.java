package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private Graph<Player, DefaultWeightedEdge> grafo;
	private PremierLeagueDAO dao;
	private Map<Integer, Player> playersIdMap;
	private List<Arco> archi;
	
	public Model() {
		dao = new PremierLeagueDAO();
	}
	
	public List<Match> listaMatch() {
		
		List<Match> m = new ArrayList<Match>(this.dao.listAllMatches());
		
		Collections.sort(m, new Comparator<Match>() {
			 @Override
			 public int compare(Match m1, Match m2) {
			 return (int) (m1.getMatchID() - m2.getMatchID ());
			 }});
		
		return m;
	}
	
	public void creaGrafo(Match match) {
		playersIdMap = new HashMap<Integer, Player>();
		grafo = new DefaultDirectedWeightedGraph<Player, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		archi = new ArrayList<Arco>();
		this.dao.creaVertici(playersIdMap, match.getMatchID());
		Graphs.addAllVertices(this.grafo, playersIdMap.values());
		for (Player p1 : playersIdMap.values()) {
			for (Player p2 : playersIdMap.values()) {
				if (p1.getPlayerID() > p2.getPlayerID()) {
					if (p1.getTeamId() != p2.getTeamId()) {
						double peso = p1.getEfficienza() - p2.getEfficienza();
						if (peso > 0) {
							Graphs.addEdgeWithVertices(this.grafo, p1, p2, peso);
							archi.add(new Arco(p1, p2, peso));
						}
						if (peso < 0){
							Graphs.addEdgeWithVertices(this.grafo, p2, p1, Math.abs(peso));
							archi.add(new Arco(p2, p1, Math.abs(peso)));
						}
					}
			  }
		   }
		}
	}
	
	public String giocatoreMigliore() {
		
		String result = "";
		for (Player p : playersIdMap.values()) {
			double sommaPesiEntranti = 0;
			Set<DefaultWeightedEdge> archiEntranti = this.grafo.incomingEdgesOf(p);
			for (DefaultWeightedEdge e : archiEntranti) {
				sommaPesiEntranti = sommaPesiEntranti + this.grafo.getEdgeWeight(e);
			}
			double sommaPesiUscenti = 0;
			Set<DefaultWeightedEdge> archiUscenti = this.grafo.outgoingEdgesOf(p);
			for (DefaultWeightedEdge e : archiUscenti) {
				sommaPesiUscenti = sommaPesiUscenti + this.grafo.getEdgeWeight(e);
			}
			double delta = sommaPesiUscenti - sommaPesiEntranti;
			p.setDeltaEfficienza(delta);
		}
		double deltaMax = 0;
		for (Player p : playersIdMap.values()) {
			if (p.getDeltaEfficienza() > deltaMax) {
				deltaMax = p.getDeltaEfficienza();
			}
		}
		for (Player p : playersIdMap.values()) {
			if (p.getDeltaEfficienza() == deltaMax) {
				result = result + p.toString() + ", delta efficienza = " + deltaMax + "\n";
			}
		}
		return result;
	}
	
	public int numeroVertici() {
		return this.grafo.vertexSet().size();
		}
	
		 public int numeroArchi() {
		return this.grafo.edgeSet().size();
		}
		 
		 public boolean verifica() {
			 if (playersIdMap.isEmpty()) {
				 return false;
			 }
			 else {
				 return true;
			 }
		 }
	
}
