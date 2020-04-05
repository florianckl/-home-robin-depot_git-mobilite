import java.util.LinkedList;
import java.util.List;

import io.jbotsim.core.Link;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import io.jbotsim.core.Point;
import io.jbotsim.ui.icons.Icons;

public class Robot extends WaypointNode {
	private int idZone;
	BaseStation baseStation;
	int recharge = 0;

	public Robot() {
	}

	public void setBaseStation(BaseStation baseStation) {
		this.baseStation = baseStation;
	}

	public int getIdZone() {
		return idZone;
	}

	@Override
	public void onStart() {
		setSensingRange(30);
		setIcon(Icons.ROBOT);
		setIconSize(29);
	}

	@Override
	public void onMessage(Message message) {

		if (message.getFlag().equals("position")) {
			Point p = (Point) message.getContent();
			this.getItineraire().add(p);
		}
	}

	@Override
	public void onSensingIn(Node node) {
		if (node instanceof Sensor) {
			recharge += 255 - ((Sensor) node).battery;
			this.getItineraire().remove(node.getLocation());
			retourBase();
			((Sensor) node).battery = 255;
		}
	}

	@Override
	public void onSensingOut(Node node) {
		if (node instanceof Sensor) {
			recharge += 255 - ((Sensor) node).battery;
			((Sensor) node).battery = 255;
		}
	}

	@Override
	public void onLinkAdded(Link link) {// permet au Robot 1(proche de la baseStation) de transmettre un itineraire à
										// robot 2( couche profonde)
		this.setLabel(idZone);
		for (Node n : getNeighbors()) {
			if (n instanceof Robot) {
				if (getCommonLinkWith(n) != null) {
					if (this.idZone == 1) {

						if (this.getItineraireSecondaire().size() > 0) {
							if (((Robot) n).getItineraire().peek() != null
									&& ((Robot) n).getItineraire().peek().equals(baseStation.getLocation())) {
								((Robot) n).getItineraire().remove(baseStation.getLocation());
							}
							List<Point> dest = new LinkedList<>();
							for (Point pt : this.getItineraireSecondaire()) {
								dest.add(pt);
							}
							Algorithm algo = new Algorithm((Robot) n, dest);
							((Robot) n).setItineraire(algo.itineraireProcheVoisins(((Robot) n).getItineraire()));
							this.getItineraireSecondaire().clear();
						}
					}
				}
			}
		}
	}

	@Override
	public void onArrival() {
		retourBase();
	}

	private void retourBase() {// quand l'itineraire est accomplie il rentre à la base
		if (this.getItineraire().isEmpty() && this.getCommonLinkWith(baseStation) == null) {
			this.getItineraire().add(baseStation.getLocation());
		}
	}

	public void setIdZone(int idZone) {
		this.idZone = idZone;
	}
}
