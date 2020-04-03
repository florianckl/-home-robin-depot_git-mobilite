public class MemoireMaxNbNoeud {

	private int nbNoeuds;
	private int maxNoeudSuccesseur;

	public int getNbNoeuds() {
		return nbNoeuds;
	}

	public int getMaxNoeudSuccesseur() {
		return maxNoeudSuccesseur;
	}

	public MemoireMaxNbNoeud(int nbNoeuds, int maxNoeudSuccesseur) {
		super();
		this.nbNoeuds = nbNoeuds;
		this.maxNoeudSuccesseur = maxNoeudSuccesseur;
	}

}
