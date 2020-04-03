package wrsn;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.jbotsim.core.Link;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import io.jbotsim.ui.icons.Icons;

public class BaseStation extends Node {

	int nbEnfant = 0;
	int enfantVisite = 0;
	Sensor meilleurNoeud;
	Robot r1;
	Robot r2;
	int nbNoeuds = 0;
	int maxNoeudSuccesseur = 0;
	int envoie = 0;

	@Override
	public List<Link> getLinks() {
		// TODO Auto-generated method stub
		return super.getLinks();
	}

	@Override
	public void onStart() {
		setIcon(Icons.STATION);
		setIconSize(16);
		for (Node n : this.getNeighbors()) {
			if (n instanceof Sensor) {
				nbEnfant++;
			}
		}
		// Initiates tree construction with an empty message
		sendAll(new Message(null, "INIT"));
	}

	List<MemoireBattery> destinations;
	Queue<MemoireBattery> destinations0 = new LinkedList<>();
	Queue<MemoireBattery> destinations1 = new LinkedList<>();
	Queue<MemoireBattery> destinations2 = new LinkedList<>();

	@Override
	public void onMessage(Message message) {
		if (message.getFlag().equals("idNbSuccesseur")) {
			if (maxNoeudSuccesseur < (int) message.getContent()) {
				maxNoeudSuccesseur = (int) message.getContent();
			}
			nbNoeuds += (int) message.getContent();
			enfantVisite++;
			if (enfantVisite == nbEnfant) {
				for (Node n0 : getNeighbors()) {
					if (n0 instanceof Sensor && ((Sensor) n0).parent.equals(this)) {
						send(n0, new Message(new MemoireMaxNbNoeud(nbNoeuds, maxNoeudSuccesseur),
								"infoNbNoeudsMaxNoeudSucc"));
					}
				}
				for (Node n : getNeighbors()) {
					if (n instanceof Sensor && ((Sensor) n).parent.equals(this)) {
						enfantVisite++;
						if (((Sensor) n).idNbSuccesseur > nbNoeuds / 2.) {
							send(n, new Message(0, "numRobotAprendretoEnfant"));
							return;
						}
					}
				}
			}
		}

		if (message.getFlag().equals("mem")) {
			if (((MemoireBattery) message.getContent()).getZone() == 0) {
				destinations0.add((MemoireBattery) message.getContent());
			}
			if (((MemoireBattery) message.getContent()).getZone() == 1) {
				destinations1.add((MemoireBattery) message.getContent());
			}
		}
	}

	@Override
	public void onClock() {
		for (Node n : getNeighbors()) {
			if (n instanceof Robot) {
				if (this.getCommonLinkWith(n) != null) {
					((Robot) n).getItineraire().remove(this.getLocation());
					destinations = new LinkedList<>();
					if (((Robot) n).getIdZone() == 1 && !destinations1.isEmpty()) {
						destinations.clear();
						destinations.addAll(destinations1);
						destinations1 = new LinkedList<>();
						Algorithm algo = new Algorithm((Robot) n, destinations);
						((Robot) n).setItineraire(algo.itineraireProcheVoisins(((Robot) n).getItineraire()));
					}
					if (((Robot) n).getIdZone() == 1 && !destinations0.isEmpty()) {
						destinations.addAll(destinations0);
						destinations0 = new LinkedList<>();
						for (MemoireBattery mem : destinations) {
							((Robot) n).getItineraireSecondaire().add(mem.getPt());
						}
					}

					if (((Robot) n).getIdZone() == 0 && !destinations0.isEmpty()) {
						destinations.addAll(destinations0);
						destinations0 = new LinkedList<>();
						Algorithm algo = new Algorithm((Robot) n, destinations);
						((Robot) n).setItineraire(algo.itineraireProcheVoisins(((Robot) n).getItineraire()));

					}
				}
			}
		}
	}
}