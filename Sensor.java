import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;

public class Sensor extends Node {
	Node parent = null;
	int numChildren = -1;
	boolean pret = false;
	int battery = 255;
	int nbEnvoieBatterieFaible = 0;
	int idNbSuccesseur = 0;
	int nombreEnfantVisite = 0;
	int envoie = 0;
	int idZone = 1;
	int nbNoeudsTotal;
	int maxNoeudSuccesseur;

	@Override
	public void onMessage(Message message) {

		// "INIT" flag : construction of the spanning tree
		// "SENSING" flag : transmission of the sensed values
		// You can use other flags for your algorithms
		if (message.getFlag().equals("erreur")) {
			send(parent, new Message(message.getContent(),"erreur"));
		}
		if (message.getFlag().equals("location")) {
			send(parent, new Message(message.getContent(),"location"));
		}
		if (message.getFlag().equals("idZone")) {
			idZone = 0;
			for (Node n : this.getNeighbors()) {
				if (n instanceof Sensor && ((Sensor) n).parent.equals(this)) {
					send(n, new Message(0, "idZone"));
				}
			}
		}
		if (message.getFlag().equals("infoNbNoeudsMaxNoeudSucc")) {
			nbNoeudsTotal = ((MemoireMaxNbNoeud) message.getContent()).getNbNoeuds();
			maxNoeudSuccesseur = ((MemoireMaxNbNoeud) message.getContent()).getMaxNoeudSuccesseur();
			for (Node n : this.getNeighbors()) {
				if (n instanceof Sensor && ((Sensor) n).parent.equals(this)) {
					send(n, new Message(new MemoireMaxNbNoeud(nbNoeudsTotal, maxNoeudSuccesseur),
							"infoNbNoeudsMaxNoeudSucc"));
				}
			}
		}

		if (message.getFlag().equals("numRobotAprendretoEnfant")) {
			int i = 0;
			int min = 100000;
			Node MeillNode = null;
			for (Node n : this.getNeighbors()) {
				if (n instanceof Sensor && ((Sensor) n).parent.equals(this)) {

					i++;
					if (nbNoeudsTotal / 2. - ((Sensor) n).idNbSuccesseur < min) {
						MeillNode = n;
					}
					if (((Sensor) n).idNbSuccesseur > nbNoeudsTotal / 2.) {
						send(MeillNode, new Message(new MemoireMaxNbNoeud(nbNoeudsTotal, maxNoeudSuccesseur),
								"numRobotAprendretoEnfant"));
						break;

					} else if (i == numChildren) {
						send(MeillNode, new Message(0, "idZone"));
						return;
					}
				}
			}
		}
		if (message.getFlag().equals("INIT"))
		{
			// if not yet in the tree
			if (parent == null) {
				// enter the tree
				parent = message.getSender();
				send(parent, new Message(this.getLocation(),"location"));
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
		if (nbEnvoieBatterieFaible != 0 && battery > 252 ) {
			nbEnvoieBatterieFaible = 0;
		}
		this.setLabel(idNbSuccesseur + " " + idZone);
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
			if (idNbSuccesseur != 0 && maxNoeudSuccesseur != 0) {
				if (nbEnvoieBatterieFaible == 0 && battery < 40 + 203. * (1. - Math.exp(-6. * (idNbSuccesseur - 1) / (maxNoeudSuccesseur - 1)))) {
					send(parent, new Message(
							new MemoireBattery(this.getLocation(), System.currentTimeMillis(), idZone, idNbSuccesseur),
							"mem"));
					nbEnvoieBatterieFaible++;
				}
			}
			if (Math.random() < 0.03) { // from time to time...
				double sensedValue = Math.random(); // sense a value
				send(parent, new Message(sensedValue, "SENSING")); // send it to parent
			}
		}
	}

	protected void updateColor() {

		setColor(battery == 0 ? Color.red : new Color(255 - battery, 255 - battery, 255));
		if(battery<=0){
			super.send(parent, new Message("","erreur"));
		}
	}
}