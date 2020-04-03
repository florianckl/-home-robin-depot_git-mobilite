
import io.jbotsim.core.Point;

public class MemoireBattery {

	private Point pt;
	private long time;
	private int zone;
	private int successeur;

	public Point getPt() {
		return pt;
	}

	public long getTime() {
		return time;
	}

	public MemoireBattery(Point pt, long l, int zone, int successeur) {
		this.pt = pt;
		this.time = l;
		this.zone = zone;
		this.setSuccesseur(successeur);
	}

	public int getZone() {
		return zone;
	}

	public void setZone(int zone) {
		this.zone = zone;
	}

	public int getSuccesseur() {
		return successeur;
	}

	public void setSuccesseur(int successeur) {
		this.successeur = successeur;
	}
}
