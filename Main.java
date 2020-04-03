import io.jbotsim.core.LinkResolver;
import io.jbotsim.core.Node;
import io.jbotsim.core.Topology;
import io.jbotsim.ui.JViewer;

public class Main {
	public static void main(String[] args) {
		// Create topology with clock not started
		Topology tp = new Topology();

		// Forbid communication between robots and sensors
		tp.setLinkResolver(new LinkResolver() {
			@Override
			public boolean isHeardBy(Node n1, Node n2) {
				if (n1 instanceof Robot && n2 instanceof Sensor || n1 instanceof Sensor && n2 instanceof Robot) {
					return false;
				} else {
					return super.isHeardBy(n1, n2);
				}
			}
		});

		// Add sensors
		tp.setDefaultNodeModel(Sensor.class);
		String filename = "./sensors.tp"; // to be adapted
		String data = tp.getFileManager().read(filename);
		tp.getSerializer().importFromString(tp, data);

		// Add base station
		BaseStation b = new BaseStation();
		tp.addNode(100, 80, b);

		// Add two robots
		tp.addNode(90, 40, new Robot(b, 0));
		tp.addNode(60, 80, new Robot(b, 1));

		new JViewer(tp);
		tp.start();
	}
}
