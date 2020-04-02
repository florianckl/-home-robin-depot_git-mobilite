package wrsn;

import java.util.LinkedList;
import java.util.List;

import io.jbotsim.core.Link;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import io.jbotsim.core.Point;
import io.jbotsim.ui.icons.Icons;

public class Robot extends WaypointNode {
	int i = 0;
	int id;
	BaseStation baseStation;

	public Robot(BaseStation baseStation, int id) {
		this.baseStation = baseStation;
		this.id = id;
	}

	@Override
	public void onStart() {
		setSensingRange(30);
		setIcon(Icons.DRONE);
		setIconSize(29);
	}

	@Override
	public void onMessage(Message message) {

		if (message.getFlag().equals("position")) {
			i = 1;
			Point p = (Point) message.getContent();
			this.getItineraire().add(p);
		}
	}

	@Override
	public void onSensingIn(Node node) {
		if (node instanceof Sensor) {
			this.getItineraire().remove(node.getLocation());
			((Sensor) node).passage++;

			((Sensor) node).setLabel(((Sensor) node).idNbSuccesseur + " " + ((Sensor) node).passage);
			retourBase();
			((Sensor) node).battery = 255;
		}
	}

	@Override
	public void onLinkAdded(Link link) {
		this.setLabel(id);
		for (Node n : getNeighbors()) {
			if (n instanceof Robot) {
				if (getCommonLinkWith(n) != null) {
					if (this.id == 1) {

						if (this.getItineraireSecondaire().size() > 0) {
							if (((Robot) n).getItineraire().peek() != null
									&& ((Robot) n).getItineraire().peek().equals(baseStation.getLocation())) {
								((Robot) n).getItineraire().remove(baseStation.getLocation());
							}
							List<MemoireBattery> dest = new LinkedList<>();
							for (Point pt : this.getItineraireSecondaire()) {
								dest.add(new MemoireBattery(pt, 0, 0, 0));
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

	private void retourBase() {
		if (this.getItineraire().isEmpty() && this.getCommonLinkWith(baseStation) == null) {
			this.getItineraire().add(baseStation.getLocation());
			i = 0;
		}
	}
}