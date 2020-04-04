import io.jbotsim.core.Color;
import io.jbotsim.core.Message;
import io.jbotsim.core.Node;

public class Sensor extends Node {
	Node parent = null;
	int numChildren = -1;
	boolean pret = false;
	int battery = 255;
	int nbEnvoieBatterieFaible = 0;
	int nbSuccesseurs = 0;
	int nombreEnfantVisite = 0;
	int envoie = 0;
	int nbNoeudsTotal;
	int maxNoeudSuccesseur;

	@Override
	public void onMessage(Message message) {

		// "INIT" flag : construction of the spanning tree
		// "SENSING" flag : transmission of the sensed values
		// You can use other flags for your algorithms

		if (message.getFlag().equals("ERREUR")) {
			super.send(parent, new Message(message.getContent(), "ERREUR"));
		}

		if (message.getFlag().equals("location")) {
			send(parent, new Message(message.getContent(), "location"));
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

		if (message.getFlag().equals("INIT")) {
			// if not yet in the tree
			if (parent == null) {
				// enter the tree
				parent = message.getSender();
				send(parent, new Message(this.getLocation(), "location"));
				getCommonLinkWith(parent).setWidth(4);
				// propagate further
				sendAll(message);
			} else if (envoie == 0) {
				pret = true;
			}
		} else if (message.getFlag().equals("nbSuccesseurs")) {
			nbSuccesseurs += (int) message.getContent();
			nombreEnfantVisite++;
			if (nombreEnfantVisite == numChildren) {
				nbSuccesseurs++;
				send(parent, new Message(nbSuccesseurs, "nbSuccesseurs"));
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
		if (nbEnvoieBatterieFaible != 0 && battery > 250) {
			nbEnvoieBatterieFaible = 0;
		}
		if (pret && nbSuccesseurs == 0) { // calcul nombre d'enfants
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
			nbSuccesseurs = 1;
			envoie++;
			send(parent, new Message(nbSuccesseurs, "nbSuccesseurs"));
		}

		if (parent != null) { // if already in the tree
			if (nbSuccesseurs != 0 && maxNoeudSuccesseur != 0) {
				if (nbEnvoieBatterieFaible == 0 && battery < 120
						+ 100. * (1. - Math.exp(-5. * (nbSuccesseurs - 1) / (maxNoeudSuccesseur - 1)))) {
					send(parent, new Message(this.getLocation(), "mem"));
					nbEnvoieBatterieFaible++;
				}
			}
			if (Math.random() < 0.02) { // from time to time...
				double sensedValue = Math.random(); // sense a value
				send(parent, new Message(sensedValue, "SENSING")); // send it to parent
			}
		}
	}

	protected void updateColor() {
		if (battery <= 0) {
			super.send(parent, new Message("", "ERREUR"));
		}
		setColor(battery == 0 ? Color.red : new Color(255 - battery, 255 - battery, 255));
	}
}
