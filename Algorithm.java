package wrsn;

import java.util.List;
import java.util.Queue;

import io.jbotsim.core.Point;

public class Algorithm {

	List<MemoireBattery> destinations;
	Robot r;

	public Algorithm(Robot r, List<MemoireBattery> destinations) {
		this.destinations = destinations;
		this.r = r;
	}

	public Queue<Point> itineraireProcheVoisins(Queue<Point> pointsCourtChemin) {
		Point point;
		if (pointsCourtChemin.isEmpty()) {
			point = r.getLocation();
		} else {
			point = pointsCourtChemin.peek();
		}
		int size = destinations.size();
		for (int i = 0; i < size; i++) {
			int index = indexprocheVoisins(point, destinations);
			point = destinations.remove(index).getPt();
			pointsCourtChemin.add(point);

		}
		return pointsCourtChemin;
	}

	private int indexprocheVoisins(Point p, List<MemoireBattery> listePoint) {
		double distanceMin = 1000;
		int procheVoisin = 0;
		for (int i = 0; i < listePoint.size(); i++) {
			double distance = p.distance(listePoint.get(i).getPt());
			if (distance < distanceMin) {
				distanceMin = distance;
				procheVoisin = i;
			}
		}
		return procheVoisin;
	}
}
