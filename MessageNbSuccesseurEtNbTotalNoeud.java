package wrsn;

public class MessageNbSuccesseurEtNbTotalNoeud {

	private int nbSuccesseur;
	private int nbNoeudTotal;

	public MessageNbSuccesseurEtNbTotalNoeud(int nbSuccesseur, int nbNoeudTotal) {
		super();
		this.setNbSuccesseur(nbSuccesseur);
		this.setNbNoeudTotal(nbNoeudTotal);
	}

	public int getNbSuccesseur() {
		return nbSuccesseur;
	}

	public void setNbSuccesseur(int nbSuccesseur) {
		this.nbSuccesseur = nbSuccesseur;
	}

	public int getNbNoeudTotal() {
		return nbNoeudTotal;
	}

	public void setNbNoeudTotal(int nbNoeudTotal) {
		this.nbNoeudTotal = nbNoeudTotal;
	}

}
