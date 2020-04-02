package wrsn;

import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;

public class Sensor extends Node {
	Node parent = null;
	int numChildren = -1;
	boolean pret = false;
	int battery = 255;
	int nbEnvoie = 0;
	int idNbSuccesseur = 0;
	int nombreEnfantVisite = 0;
	int envoie = 0;
	int idZone = 1;
	int difference = 0;
	int nbenfvisi = 0;
	Node meilleurNoeud;
	int diffdeb;
	public int passage = 0;
	long TempsDep = System.currentTimeMillis();

	@Override
	public void onMessage(Message message) {

		// "INIT" flag : construction of the spanning tree
		// "SENSING" flag : transmission of the sensed values
		// You can use other flags for your algorithms
		if (message.getFlag().equals("idZone")) {
			idZone = 0;
			for (Node n : this.getNeighbors()) {
				if (n instanceof Sensor && ((Sensor) n).parent.equals(this)) {
					send(n, new Message(0, "idZone"));
				}
			}
		}
		if (message.getFlag().equals("numRobottoChildren")) {
			send(parent, new Message(idNbSuccesseur, "numRobotFromChildren"));
		}
		if (message.getFlag().equals("numRobotAprendretoEnfant")) {
			difference = (int) message.getContent();
			diffdeb = difference;
			for (Node n : this.getNeighbors()) {

				if (n instanceof Sensor && ((Sensor) n).parent.equals(this)) {
					send(n, new Message(difference, "numRobottoChildren"));
				}
			}
		}
		if (message.getFlag().equals("numRobotFromChildren")) {
			nbenfvisi++;
			if (0 < (int) message.getContent() - 15 && (int) message.getContent() - 15 < difference) {
				difference = (int) message.getContent() - 15;
				meilleurNoeud = message.getSender();
			} else {

			}
			if (numChildren == nbenfvisi && diffdeb != difference) {
				send(meilleurNoeud, new Message(difference, "numRobotAprendretoEnfant"));
			} else if (numChildren == nbenfvisi && diffdeb == difference) {
				idZone = 0;
				for (Node n : this.getNeighbors()) {
					if (n instanceof Sensor && ((Sensor) n).parent.equals(this)) {
						send(n, new Message(0, "idZone"));
					}
				}
			}
		}
		if (message.getFlag().equals("INIT")) {
			// if not yet in the tree
			if (parent == null) {
				// enter the tree
				parent = message.getSender();
				getCommonLinkWith(parent).setWidth(4);
				// propagate further
				sendAll(message);
			} else if (envoie == 0) {
				pret = true;
			}
		} else if (message.getFlag().equals("idNbSuccesseur")) {
			idNbSuccesseur += (int) message.getContent();
			nombreEnfantVisite++;
			if (nombreEnfantVisite == numChildren) {
				idNbSuccesseur++;
				send(parent, new Message(idNbSuccesseur, "idNbSuccesseur"));
			}
		} else if (message.getFlag().equals("SENSING")) {
			// retransmit up the tree
			send(parent, message);
		} else if (message.getFlag().equals("mem")) {
			// retransmit up the tree
			send(parent, message);
		}

	}

	@Override
	public void send(Node destination, Message message) {
		if (battery > 0) {
			super.send(destination, message);
			battery--;
			updateColor();
		}
	}

	@Override
	public void onClock() {
		if (nbEnvoie != 0 && battery > 250) {
			nbEnvoie = 0;
		}
		// this.setLabel(idNbSuccesseur + " " + idZone);
		if (pret && idNbSuccesseur == 0) {
			numChildren = 0;
			for (Node n : this.getNeighbors()) {

				if (n instanceof BaseStation) {

				} else if (((Sensor) n).parent == null) {
					numChildren = -1;
					pret = false;
				} else if (((Sensor) n).parent.equals(this)) {
					numChildren++;
				}
			}
		}
		if (pret && numChildren == 0) {
			pret = false;
			idNbSuccesseur = 1;
			envoie++;
			send(parent, new Message(idNbSuccesseur, "idNbSuccesseur"));
		}

		if (parent != null) { // if already in the tree
			// System.out.println(TempsDep * 1.0 / System.currentTimeMillis());
			if (nbEnvoie == 0 && battery < 80 + 150. * (-1.58 * (1. - Math.pow(1. * idNbSuccesseur, 1.0 / 6)))) {
				send(parent, new Message(
						new MemoireBattery(this.getLocation(), System.currentTimeMillis(), idZone, idNbSuccesseur),
						"mem"));
				nbEnvoie++;
			}
			if (Math.random() < 0.02) { // from time to time...
				double sensedValue = Math.random(); // sense a value
				send(parent, new Message(sensedValue, "SENSING")); // send it to parent
			}
		}
	}

	protected void updateColor() {
		setColor(battery == 0 ? Color.red : new Color(255 - battery, 255 - battery, 255));
	}
}