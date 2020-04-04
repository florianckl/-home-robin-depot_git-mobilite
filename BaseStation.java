import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.jbotsim.core.Link;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import io.jbotsim.core.Point;
import io.jbotsim.ui.icons.Icons;

public class BaseStation extends Node {

	int nbEnfant = 0;
	int enfantVisite = 0;
	int nbNoeudsTotaux = 0;
	int maxNoeudSuccesseur = 0;
	int envoie = 0;
	boolean pret = false;
	int locationsReceived = 0;
	double thirdtile;
	double median;
	double average;
	double max;
	double tempsDep;
	boolean Stop = false;
	int nbRobot = 0;

	@Override
	public List<Link> getLinks() {
		// TODO Auto-generated method stub
		return super.getLinks();
	}

	@Override
	public void onStart() {
		// tempsDep = System.currentTimeMillis();
		setIcon(Icons.STATION);
		setIconSize(16);
		for (Node n : this.getNeighbors()) {
			if (n instanceof Robot) {
				((Robot) n).setBaseStation(this);
				((Robot) n).setIdZone(nbRobot);
				nbRobot++;
			}
		}
		for (Node n : this.getNeighbors()) {
			if (n instanceof Sensor) {
				nbEnfant++;
			}
		}
		// Initiates tree construction with an empty message
		sendAll(new Message(null, "INIT"));
	}

	ArrayList<Point> locations = new ArrayList<>();
	ArrayList<Double> distances = new ArrayList<>();
	List<Point> destinations;
	Queue<Point> destinations0 = new LinkedList<>();
	Queue<Point> destinations1 = new LinkedList<>();
	Queue<Point> destinations2 = new LinkedList<>();

	@Override
	public void onMessage(Message message) {
		if (message.getFlag().equals("ERREUR")) {
			Stop = true;
		}
		if (message.getFlag().equals("location")) {
			Point point = (Point) message.getContent();
			locations.add(point);
			distances.add(point.distance(this.getLocation()));
			locationsReceived++;
		}
		if (message.getFlag().equals("nbSuccesseurs")) {// permet de connaitre le nombre de noeuds dans le graphe
			if (maxNoeudSuccesseur < (int) message.getContent()) {
				maxNoeudSuccesseur = (int) message.getContent();
			}
			nbNoeudsTotaux += (int) message.getContent();
			enfantVisite++;
			if (enfantVisite == nbEnfant) {
				pret = true;
				for (Node n0 : getNeighbors()) {
					if (n0 instanceof Sensor && ((Sensor) n0).parent.equals(this)) {
						send(n0, new Message(new MemoireMaxNbNoeud(nbNoeudsTotaux, maxNoeudSuccesseur),
								"infoNbNoeudsMaxNoeudSucc"));
					}
				}
			}
		}

		if (message.getFlag().equals("mem")) {
			Point memSensor = (Point) message.getContent();
			if (memSensor.distance(this.getLocation()) < getMaxDistance() / 3) {
				destinations0.add((Point) message.getContent());
			} else {
				destinations1.add((Point) message.getContent());
			}
		}
	}

	@Override
	public void onClock() {
		if (Stop) {
			int i = 0;
			double rechargeTotal = 0;
			for (Node n : getNeighbors()) {// transmettre les itineraires aux robots
				if (n instanceof Robot) {
					if (this.getCommonLinkWith(n) != null) {
						rechargeTotal += ((Robot) n).recharge / (255. * nbNoeudsTotaux);
						i++;
					}
				}
			}
			if (i == nbRobot) {
				// System.out.println((System.currentTimeMillis() - tempsDep) / 1000);
				System.out.println(rechargeTotal);
				System.exit(0);
			}
		} else {
			if (pret && locationsReceived == nbNoeudsTotaux) {
				max = getMaxDistance();
				average = getAverageDistance();
				median = getMedianDistance();
				thirdtile = getThirdtileDistance();
			}

			for (Node n : getNeighbors()) {// transmettre les itineraires aux robots
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
							for (Point mem : destinations) {
								((Robot) n).getItineraireSecondaire().add(mem);
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

	private Double getMaxDistance() {

		double max = 0;
		for (double d : distances) {
			if (d > max) {
				max = d;
			}
		}
		return max;
	}

	private Double getAverageDistance() {

		double sum = 0;
		int len = distances.size();
		for (double d : distances) {
			sum = sum + d;
		}
		return sum / len;
	}

	private Double getMedianDistance() {

		int len = distances.size();
		Collections.sort(distances);
		if (len % 2 == 1) {
			return distances.get(len / 2);
		} else {
			return (distances.get(len / 2 - 1) + distances.get(len / 2)) / 2;
		}
	}

	private Double getThirdtileDistance() {

		int len = distances.size();
		Collections.sort(distances);

		return distances.get(len / 3);

	}

	private Double getQuardtileDistance() {

		int len = distances.size();
		Collections.sort(distances);

		return distances.get(len / 4 - 1);

	}

}
