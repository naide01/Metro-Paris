package it.polito.tdp.metroparis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.javadocmd.simplelatlng.LatLng;

import it.polito.tdp.metroparis.model.Connessione;
import it.polito.tdp.metroparis.model.CoppiaId;
import it.polito.tdp.metroparis.model.Fermata;
import it.polito.tdp.metroparis.model.Linea;

public class MetroDAO {

	public List<Fermata> readFermate() {

		final String sql = "SELECT id_fermata, nome, coordx, coordy FROM fermata ORDER BY nome ASC";
		List<Fermata> fermate = new ArrayList<Fermata>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Fermata f = new Fermata(rs.getInt("id_Fermata"), rs.getString("nome"),
						new LatLng(rs.getDouble("coordx"), rs.getDouble("coordy")));
				fermate.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return fermate;
	}

	public List<Linea> readLinee() {
		final String sql = "SELECT id_linea, nome, velocita, intervallo FROM linea ORDER BY nome ASC";

		List<Linea> linee = new ArrayList<Linea>();

		try {
			Connection conn = DBConnect.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Linea f = new Linea(rs.getInt("id_linea"), rs.getString("nome"), rs.getDouble("velocita"),
						rs.getDouble("intervallo"));
				linee.add(f);
			}

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Errore di connessione al Database.");
		}

		return linee;
	}
	
	/**
	 * ESISTE ALMENO UNA CONNESSIONE TRA PARTENZA E ARRIVO
	 * @param partenza
	 * @param arrivo
	 * @return
	 */

	public boolean isConnesse(Fermata partenza, Fermata arrivo) {
		String sql = "SELECT count(*) AS c "
				+ "FROM connessione "
				+ "WHERE id_stazP=? "
				+ "AND id_stazA=? ";
		try {
		Connection conn = DBConnect.getConnection();
		PreparedStatement st = conn.prepareStatement(sql);
		st.setInt(1, partenza.getIdFermata());
		st.setInt(2, arrivo.getIdFermata());
		
		ResultSet res = st.executeQuery();
		
		res.first();
		
		int c = res.getInt("c");
		
		//chiudere la connesione
		conn.close();
		
		return c > 0;
		
		
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException ("Errore database ", e);
		}
	}
	
	/**
	 * Metodo 2a) 
	 */
	public List<Integer> getIdFermateConesse(Fermata partenza) {
		String sql = "SELECT id_stazA "
				+ "FROM connessione "
				+ "WHERE id_stazP = ? "
				+ "GROUP BY id_stazA ";
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, partenza.getIdFermata());
			ResultSet res = st.executeQuery();
			List<Integer> result = new ArrayList<Integer>();
			while (res.next()) {
				result.add(res.getInt("id_stazA"));
			}
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}
	/**
	 * Metodo 2b)
	 */
	
	public List<Fermata> getFermateConnesse(Fermata partenza){
		String sql = "SELECT id_fermata, nome, coordx, coordy "
				+ "FROM fermata f "
				+ "WHERE id_fermata IN (SELECT id_stazA "
				+ "FROM connessione "
				+ "WHERE id_stazP = ? "
				+ "GROUP BY id_stazA "
				+ ") "
				+ "ORDER BY nome ASC ";
		
		List<Fermata> fermate = new ArrayList<Fermata>();
		try {
			 Connection conn = DBConnect.getConnection();
			 PreparedStatement st = conn.prepareStatement(sql);
			 st.setInt(1, partenza.getIdFermata());
			 ResultSet rs = st.executeQuery();
			 
			 while (rs.next()) {
				 Fermata f = new Fermata(rs.getInt("id_Fermata"), rs.getString("nome"),
							new LatLng(rs.getDouble("coordx"), rs.getDouble("coordy")));
					fermate.add(f);
			 }
			 st.close();
			 return fermate;
		  
		 } catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	/**
	 * MOTODO 3)
	 */
	
	public List<CoppiaId> getAllFermateConnesse	(){
		String sql = "SELECT DISTINCT id_stazP, id_stazA "
				+ "FROM connessione ";
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			List<CoppiaId> result = new ArrayList<CoppiaId>();
			while(res.next()) {
				result.add(new CoppiaId(res.getInt("id_stazP"), res.getInt("id_stazA")));
			}
			conn.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}

}
