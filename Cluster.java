package wrsn;

import java.util.LinkedList;
import java.util.Queue;

import io.jbotsim.core.Point;

public class Cluster {

	private Queue<Point> tabPt1;
	private Queue<Point> tabPt2;
	private Queue<Point> tabPt1_2;

	public Queue<Point> getTabPt1() {
		return tabPt1;
	}

	public Queue<Point> getTabPt2() {
		return tabPt2;
	}

	public Cluster(Queue<Point> merge, Queue<Point> tabPt1, Queue<Point> tabPt2) {
		this.tabPt1 = tabPt1;
		this.tabPt2 = tabPt2;
		this.tabPt1_2 = merge;

	}

	public void creation(Point robot1, Point robot2) {
		Queue<Point> t1 = new LinkedList();
		Queue<Point> t2 = new LinkedList();
		Point p1 = robot1;
		Point p2 = robot2;
		for (Point pt : tabPt1_2) {
			if (p1.distance(pt) < p2.distance(pt)) {
				t1.add(pt);
			} else {
				t2.add(pt);
			}
		}
		int longueur = t1.size();
		for (int i = 0; i < longueur; i++) {
			Point ptMin = null;
			double distanceMin = 10000;
			for (Point pt : t1) {
				double distance = p1.distance(pt);
				if (distance < distanceMin) {
					distanceMin = distance;
					ptMin = pt;
				}
			}
			t1.remove(ptMin);
			tabPt1.add(ptMin);
		}
		longueur = t2.size();
		for (int i = 0; i < longueur; i++) {
			Point ptMin = null;
			double distanceMin = 10000;
			for (Point pt : t2) {
				double distance = p1.distance(pt);
				if (distance < distanceMin) {
					distanceMin = distance;
					ptMin = pt;
				}
			}
			t2.remove(ptMin);
			tabPt2.add(ptMin);
		}
	}

	public void creationCluster(Point robot1, Point robot2) {
		Queue<Point> t1 = new LinkedList();
		Queue<Point> t2 = new LinkedList();
		Point p1 = robot1;
		Point p2 = robot2;
		int j = 0;
		int max1 = 0;
		int max2 = 0;
		while (j < 6) {
			Point p1Bis = new Point(0, 0);
			Point p2Bis = new Point(0, 0);
			max1 = 0;
			max2 = 0;
			for (Point pt : tabPt1_2) {
				if (p1.distance(pt) < p2.distance(pt)) {
					p1Bis.setLocation(p1Bis.getX() + pt.getX(), p1Bis.getY() + pt.getY());
					max1 += 1;
				} else {
					p2Bis.setLocation(p2Bis.getX() + pt.getX(), p2Bis.getY() + pt.getY());
					max2 += 1;
				}
			}
			if (max1 != 0) {
				p1 = new Point(p1Bis.getX() / max1, p1Bis.getY() / max1);
			}
			if (max2 != 0) {
				p2 = new Point(p2Bis.getX() / max2, p2Bis.getY() / max2);
			}
			j++;
			if (max1 == 0 || max2 == 0) {
				break;
			}
		}
		for (Point pt : tabPt1_2) {
			if (p1.distance(pt) < p2.distance(pt)) {
				t1.add(pt);
			} else {
				t2.add(pt);
			}
		}

		int longueur = t1.size();
		for (int i = 0; i < longueur; i++) {
			Point ptMin = null;
			double distanceMin = 10000;
			for (Point pt : t1) {
				double distance = p1.distance(pt);
				if (distance < distanceMin) {
					distanceMin = distance;
					ptMin = pt;
				}
			}
			t1.remove(ptMin);
			tabPt1.add(ptMin);
		}
		longueur = t2.size();
		for (int i = 0; i < longueur; i++) {
			Point ptMin = null;
			double distanceMin = 10000;
			for (Point pt : t2) {
				double distance = p1.distance(pt);
				if (distance < distanceMin) {
					distanceMin = distance;
					ptMin = pt;
				}
			}
			t2.remove(ptMin);
			tabPt2.add(ptMin);
		}
	}

}
