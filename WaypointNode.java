package wrsn;

import java.util.LinkedList;
import java.util.Queue;

import io.jbotsim.core.Node;
import io.jbotsim.core.Point;

public class WaypointNode extends Node {
	private Queue<Point> itineraire = new LinkedList<>();
	private Queue<Point> itineraireSecondaire = new LinkedList<>();

	public Queue<Point> getItineraire() {
		return itineraire;
	}

	double speed = 1;

	public void addDestination(double x, double y) {
		itineraire.add(new Point(x, y));
	}

	public void addDestination(Point p) {
		itineraire.add(p);
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	@Override
	public void onClock() {
		if (!itineraire.isEmpty()) {
			Point dest = itineraire.peek();
			if (distance(dest) > speed) {
				setDirection(dest);
				move(speed);
			} else {
				setLocation(dest);
				itineraire.poll();
				onArrival();
			}
		}
	}

	public void onArrival() { // to be overridden
	}

	public void setItineraire(Queue<Point> itineraire) {
		this.itineraire = itineraire;
	}

	public Queue<Point> getItineraireSecondaire() {
		return itineraireSecondaire;
	}

	public void setItineraireSecondaire(Queue<Point> itineraireSecondaire) {
		this.itineraireSecondaire = itineraireSecondaire;
	}
}