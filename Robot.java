import java.util.LinkedList;
import java.util.Queue;

import io.jbotsim.core.Link;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;
import io.jbotsim.core.Point;
import io.jbotsim.ui.icons.Icons;

public class Robot extends WaypointNode {
	private int i;
	private int idZone;
	BaseStation baseStation;

	public Robot(BaseStation baseStation, int idZone) {
		this.baseStation = baseStation;
		this.idZone = idZone;
		i = 0;
	}

	public int getIdZone() {
		return idZone;
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
			retourBase();
			((Sensor) node).battery = 255;
		}
	}

	@Override
	public void onSensingOut(Node node) {
		if (node instanceof Sensor) {
			((Sensor) node).battery = 255;
		}
	}

	@Override
	public void onLinkAdded(Link link) {
		for (Node node : this.getNeighbors()) {
			if (node instanceof Robot) {
				Queue<Point> merge = new LinkedList();
				Queue<Point> tab1 = new LinkedList();
				Queue<Point> tab2 = new LinkedList();
				merge.addAll(this.getItineraire());
				merge.addAll(((Robot) node).getItineraire());
				if (!merge.isEmpty()) {
					Cluster c = new Cluster(merge, tab1, tab2);
					c.creation(this.getLocation(), node.getLocation());
					this.setItineraire(tab1);
					((Robot) node).setItineraire(tab2);
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
