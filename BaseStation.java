import java.util.*;

import io.jbotsim.core.Link;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import io.jbotsim.core.Point;
import io.jbotsim.ui.icons.Icons;

public class BaseStation extends Node {

	int nbEnfant = 0;
	int enfantVisite = 0;
	Sensor meilleurNoeud;
	Robot r1;
	Robot r2;
	int nbNoeudsTotaux = 0;
	int maxNoeudSuccesseur = 0;
	int envoie = 0;
	boolean pret = false;
	int locationsReceived = 0;
	double quartile;
	double thirdtile;
	double median;
	double average;
	double max;
	boolean stop = false;


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

	ArrayList<Point> locations = new ArrayList<>();
	ArrayList<Double> distances = new ArrayList<Double>();
	List<MemoireBattery> destinations;
	Queue<MemoireBattery> destinations0 = new LinkedList<>();
	Queue<MemoireBattery> destinations1 = new LinkedList<>();
	Queue<MemoireBattery> destinations2 = new LinkedList<>();

	@Override
	public void onMessage(Message message) {

		if(message.getFlag().equals("erreur")){
			stop = true;
		}
		if(message.getFlag().equals("location"))
		{
			Point point = (Point)message.getContent();
			locations.add(point);
			distances.add(point.distance(this.getLocation()));
			//System.out.println("Je suis le point de coordonnees " + point.getX() + " " + point.getY() + "et je suis ajoute a la liste");
			locationsReceived++;
		}
		if (message.getFlag().equals("idNbSuccesseur")) {
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
				for (Node n : getNeighbors()) {
					if (n instanceof Sensor && ((Sensor) n).parent.equals(this)) {
						enfantVisite++;
						if (((Sensor) n).idNbSuccesseur > nbNoeudsTotaux / 2.) {
							send(n, new Message(0, "numRobotAprendretoEnfant"));
							return;
						}
					}
				}
			}
		}

		if (message.getFlag().equals("mem")) {
			MemoireBattery memSensor = (MemoireBattery)message.getContent();
			//System.out.println("le message est envoye depuis une distance de " + memSensor.getPt().distance(this.getLocation()));
			if(memSensor.getPt().distance(this.getLocation())<quartile) {
				destinations0.add((MemoireBattery) message.getContent());
			}
			else{
				destinations1.add((MemoireBattery) message.getContent());
			}
			//if (((MemoireBattery) message.getContent()).getZone() == 0) {
			//	destinations0.add((MemoireBattery) message.getContent());
			//}
			//if (((MemoireBattery) message.getContent()).getZone() == 1) {
			//	destinations1.add((MemoireBattery) message.getContent());
			//}
		}
	}

	@Override
	public void onClock() {

		if (stop) {
			double rechageTotal = 0;
			for (Node n : getNeighbors()) {// transmettre les itineraires aux robots
				if (n instanceof Robot) {
					if (this.getCommonLinkWith(n) != null) {
						rechageTotal += ((Robot) n).recharge / (255. * nbNoeudsTotaux);
					}
				}
			}
			System.out.println(rechageTotal);
			return;
		}
		if (pret && locationsReceived == nbNoeudsTotaux) {
			max = getMaxDistance();
			average = getAverageDistance();
			median = getMedianDistance();
			thirdtile = getThirdtileDistance();
			quartile = getQuartileDistance();
		}

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

	private Double getMaxDistance(){

		double max = 0;
		for (double d : distances){
			if (d > max){
				max = d;
			}
		}
		return max;
	}

	private Double getAverageDistance(){

		double sum = 0;
		int len = distances.size();
		for(double d : distances){
			sum = sum + d;
		}
		return sum/len;
	}

	private Double getMedianDistance(){

		int len = distances.size();
		Collections.sort(distances);
		if(len % 2 == 1) {
			return distances.get(len / 2);
		}
		else{
			return ( distances.get((len / 2) - 1) + distances.get(len / 2) ) / 2;
		}
	}

	private Double getThirdtileDistance(){

		int len = distances.size();
		Collections.sort(distances);

		return distances.get(len / 3);

	}

	private Double getQuartileDistance(){

		int len = distances.size();
		Collections.sort(distances);

		return distances.get(len / 4);

	}

}

