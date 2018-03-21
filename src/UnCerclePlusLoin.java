/*************************************************
 *	      Projet S1 - Un Cercle Plus Loin        *
 *	@author Jeremy Watrelos - Jeremy Steenkiste  *
 *  @date 25/04/2015                             *
 ************************************************/

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import extensions.CSVFile;
import extensions.Sound;
import extensions.TransparentImage;

class UnCerclePlusLoin extends Program {

	///configVar : Tableau contenant toutes les variables récupérées dans le config.csv
	String[][] configVar;
	///Synchronise les étapes du jeu pur limiter les tests de condition
	int step = 0;
	///Récupère le numéro de la question contenu dans la case qui est cliqué
	int numQuestClicked = 0;
	///Savoir si nous sommes entrain de répondre à une question
	boolean alreadyQuest = false;
	///Variable récupérant les actions effectuées dans la fonction mouseChanged()
	String clicked = "";
	///Défini l'image des règles
	TransparentImage regles = newTransparentImage(507, 563);
	///Récupère les coordonnées des numQuest (pour mettre la X ou le V).
	int[][] finishQuest;
	///Tableau contenant toutes les matières dans le désordre
	TransparentImage[] matieresRandom = new TransparentImage [36];
	///update des zones d'affichage (style: scores, questions restantes, questions/réponses)
	TransparentImage[] updateImage = new TransparentImage [5];
	///Statistiques de la partie et du joueur
	int nbrJoueurs = 0;
	int[] scoreJoueur;
	int[] questRestantesJoueur;
	///Les réponses de la question posée.
	String[][] actualQuest = new String [4][4];
	///Savoir si on a répondu à la question
	boolean repQuest = false;
	///Récupère le temps restant pour répondre à la question
	long timerQuest = 0;
	int ptsBonus = 0;
	///Savoir c'est à qui le tour. #loto
	int laps = 0;
	///Savoir si le jeu est fini.
	String[] vainqueur = new String [2];
	boolean endGame = false;
	/// 2 Thread, l'un pour la musique et l'autre pour le minuteur des questions.
	Thread musicThread;
	Thread timerQuestThread;
	
	/**
	 * Lance mon programme.
	 */
	void algorithm(){
		///Initialisation des principales variables pour le lancement du jeu
		TransparentImage window = newTransparentImage(1280, 720);
		TransparentImage background = newTransparentImage("./images/background.png");
		TransparentImage boutonQuitter = newTransparentImage("./images/mainMenu/bouton_quitter.png");
		TransparentImage logo = newTransparentImage("./images/mainMenu/logo.png");
		TransparentImage boutonJouer = newTransparentImage("./images/mainMenu/bouton_jouer.png");
		TransparentImage boutonNbrJoueur1 = newTransparentImage("./images/mainMenu/bouton_1Joueur.png");
		TransparentImage boutonNbrJoueur2 = newTransparentImage("./images/mainMenu/bouton_2Joueurs.png");
		TransparentImage boutonNbrJoueur3 = newTransparentImage("./images/mainMenu/bouton_3Joueurs.png");
		TransparentImage boutonNbrJoueur4 = newTransparentImage("./images/mainMenu/bouton_4Joueurs.png");
		TransparentImage zoneQuestion = newTransparentImage("./images/game/zoneQuestion.png");
		TransparentImage boutonRegles = newTransparentImage("regles","./images/mainMenu/bouton_rules.png");
		Sound bouton = newSound("./son/bouton_pressed.mp3");
		///Initilise les variables du jeu
		initalisationVar();
		///Défini les polices du jeu.
		setPolice();
		///Ouvre le fichier title.txt et l'affiche dans la console
		openFileTxt("./ressources/title.txt");
		///Musique principale du jeu (Thread pour la faire tourner en boucle)
		if(configVar[10][1].equals("oui")){
			playMusicForever();
		}
		///Initialisation des mécaniques du jeu
		getMatiere();
		setMatiere();
		setQuestion();
		///Sauvegarde les 36 questions nécessaire au jeu (pour cette partie), dans un seul CSV (parmi 18 .csv)
		saveCSV(matEtQuest, "./ressources/questions.csv", ';');
        
		menuPrincipal(window, logo, background, boutonQuitter, boutonRegles);
		window.drawImage(boutonJouer, (640-311), 420);
		show(window);
		
		while(step == 0){
			delay(1);
			if (clicked.equals("Jouer")){
				play(bouton, true);
				boutonPressed(window);
				delay(200);
				menuPrincipal(window, logo, background, boutonQuitter, boutonRegles);
				drawImage(window, boutonNbrJoueur1, window.getWidth()/2-311, 420);
				drawImage(window, boutonNbrJoueur2, window.getWidth()/2+11, 420);
				drawImage(window, boutonNbrJoueur3, window.getWidth()/2-311, 420+95);
				drawImage(window, boutonNbrJoueur4, window.getWidth()/2+11, 420+95);
				println("Choix : 1");
				println("-~-~-");
				println("(1) - 1 Joueur");
				println("(2) - 2 Joueurs");
				println("(3) - 3 Joueurs");
				println("(4) - 4 Joueurs");
				println("(5) - Quitter");
				println("-~-~-");
				clicked = "";
				step = 1;
			}else if (clicked.equals("Quitter")){
				play(bouton, true);
				boutonPressed(window);
				println("Choix : 2");
				delay(200);
				System.exit(0);
			}else if (clicked.equals("openRules")){
				play(bouton, true);
				boutonPressed(window);
				menuRegles();
			}
		}
		
		while(step == 1){
			delay(1);
			if (clicked.equals("Quitter")){
				play(bouton, true);
				boutonPressed(window);
				println("Choix : 5");
				delay(200);
				System.exit(0);
			}
			for(int i=1; i <= 4; ++i){
				if (clicked.equals(i+"J")){
					delay(50);
					play(bouton, true);
					boutonPressed(window);
					delay(200);
					nbrJoueurs = i;
					questRestantesJoueur = new int[nbrJoueurs];
					gameStart(window, background);
					step = 2;
					clicked = "";
				}
			}
			if (clicked.equals("openRules")){
				play(bouton, true);
				boutonPressed(window);
				menuRegles();
			}
		}
		while(step == 2){
			delay(1);
			for(int i=1;i<=36;++i){
				if(clicked.equals("case"+i) && !alreadyQuest){
					play(bouton, true);
					window.drawImage(zoneQuestion, 0, 565);
					if(laps == (nbrJoueurs)){
						laps = 0;
					}
					
					window.drawImage(difficulty(numQuestClicked), 4, 595);
					window.setColor(Color.BLACK);
					window.setNewFont(police[1], "PLAIN", tabTaillePolice[2]);
					String numeroQuest = "Q"+numQuestClicked;
					window.drawString(numeroQuest, 6, 590);
					
					window.setColor(Color.WHITE);
					window.setNewFont(police[1], "PLAIN", tabTaillePolice[4]);
					timer(window);
					boolean find = false;
					for(int j=0; j<length(matEtQuest,1) && !find;++j){
						if(matEtQuest[j][0].equals("case"+i)){
							find = true;
							window.drawString(matEtQuest[j][2], 85, 600);
							//info terminal
							println("Question "+numQuestClicked+" : "+matEtQuest[j][2]);
							println("-~-~-");
							for(int x=0; x<length(actualQuest,1);++x){
								char reponses = (char)('A' + x);
								actualQuest[x][0] = "reponse"+reponses;
								actualQuest[x][1] = matEtQuest[j][x+4];
								//info terminal
								println(actualQuest[x][0] + " | " + actualQuest[x][1]);
							}
							println("-~-~-");
							caseReponse(window, j, false);
						}
					}
					//Erreur si la question n'existe pas, ferme le programme.
					if(!find){
						println("Erreur 404 : Questions Not Found");
						System.exit(0);
					}
					clicked = "";
					alreadyQuest = true;
				}
			}
			if (alreadyQuest){
				if(getTime() >= (timerQuest)){
					println("Temps écoulé, la bonne réponse était : "+matEtQuest[numQuestClicked-1][3]);
					failQuest(window, true);
					endQuest(window);
				}
				if(numQuestClicked >= 1 && repQuest){
					play(bouton, true);
					if (clicked.equals(matEtQuest[numQuestClicked-1][3])){
						println("Bonne réponse, vous avez gagné : "+ptsActualQuest()+"pts (Bonus Temps : +"+bonusTimer()+")");
						successQuest(window);
					}else{
						println("Vous avez perdu, la bonne réponse était : "+matEtQuest[numQuestClicked-1][3]);
						failQuest(window, false);
					}
					endQuest(window);
				}
			}
			if (clicked.equals("QuitterIG")){
				play(bouton, true);
				boutonPressed(window);
				delay(200);
				System.exit(0);
			}
			if (clicked.equals("openRulesIG")){
				play(bouton, true);
				boutonPressed(window);
				menuRegles();
			}
		}
		drawFirst(window);
		while(step == 3 && !endGame){
			delay(1);
			if (clicked.equals("QuitterIG")){
				play(bouton, true);
				boutonPressed(window);
				delay(200);
				System.exit(0);
			}
		}
		
	}

	/**
	 * Affiche le contenu du menu principal (sans le bouton Jouer)
	 * 
	 * @param window : la fenêtre principale du jeu
	 * @param logo : l'image du logo
	 * @param background : le fond d'écran du jeu
	 * @param boutonQuitter : le bouton quitter du menu
	 * @param boutonRegles : le bouton règles
	 */
	void menuPrincipal(TransparentImage window, TransparentImage logo, TransparentImage background, TransparentImage boutonQuitter, TransparentImage boutonRegles){
		window.drawImage(background, 0, 0);
		window.drawImage(logo, 640-480, 60);
		window.drawImage(boutonQuitter, (640-151), 611);
		window.drawImage(boutonRegles, 1280 - boutonRegles.getWidth() - 15, 720 - boutonRegles.getHeight() - 15);
		window.setColor(Color.WHITE);
		window.setNewFont(police[0], "PLAIN", tabTaillePolice[0]);
		window.drawString("Auteur : Jeremy W. & Jeremy S.", 2, 685);
		window.drawString("Projet S1 - IUT Informatique", 2, 700);
		window.drawString("Version "+configVar[41][1], 2, 715);
	}

	/**
	 * Affiche l'image d'un bouton pressé, suivant le bouton cliqué
	 * 
	 * @param window : récupere la fenêtre principale du jeu
	 */
	void boutonPressed(TransparentImage window){
		TransparentImage boutonJouerPressed = newTransparentImage("./images/mainMenu/bouton_jouer_pressed.png");
		TransparentImage boutonQuitterPressed = newTransparentImage("./images/mainMenu/bouton_quitter_pressed.png");
		TransparentImage boutonNbrJoueur1Pressed = newTransparentImage("./images/mainMenu/bouton_1Joueur_pressed.png");
		TransparentImage boutonNbrJoueur2Pressed = newTransparentImage("./images/mainMenu/bouton_2Joueurs_pressed.png");
		TransparentImage boutonNbrJoueur3Pressed = newTransparentImage("./images/mainMenu/bouton_3Joueurs_pressed.png");
		TransparentImage boutonNbrJoueur4Pressed = newTransparentImage("./images/mainMenu/bouton_4Joueurs_pressed.png");
		TransparentImage boutongameQuitterPressed = newTransparentImage("./images/game/bouton_quitter_pressed.png");
		TransparentImage boutonRegles = newTransparentImage("regles","./images/mainMenu/bouton_rules.png");
		TransparentImage boutonReglesPressed = newTransparentImage("./images/mainMenu/bouton_rules_pressed.png");
		
		if (clicked.contentEquals("Jouer")){
			window.drawImage(boutonJouerPressed, (640-311), 420);
		}else if (clicked.equals("Quitter")){
			window.drawImage(boutonQuitterPressed, (640-151), 611);
		}else if (clicked.equals("1J")){
			window.drawImage(boutonNbrJoueur1Pressed, (640-311), 420);
		}else if (clicked.equals("2J")){
			window.drawImage(boutonNbrJoueur2Pressed, (640+11), 420);
		}else if (clicked.equals("3J")){
			window.drawImage(boutonNbrJoueur3Pressed, (640-311), (420+95));
		}else if (clicked.equals("4J")){
			window.drawImage(boutonNbrJoueur4Pressed, (640+11), (420+95));
		}else if (clicked.equals("QuitterIG")){
			window.drawImage(boutongameQuitterPressed, 1280 - boutongameQuitterPressed.getWidth() - 15, 720 - boutongameQuitterPressed.getHeight() - 15);
		}else if (clicked.equals("openRules")){
			if (!regles.isShowing()){
				window.drawImage(boutonReglesPressed, 1280 - boutonReglesPressed.getWidth() - 15, 720 - boutonReglesPressed.getHeight() - 15);
			}else{
				window.drawImage(boutonRegles, 1280 - boutonRegles.getWidth() - 15, 720 - boutonRegles.getHeight() - 15);
			}
		}else if (clicked.equals("openRulesIG")){
			if (!regles.isShowing()){
				window.drawImage(boutonReglesPressed, 1280 - boutonReglesPressed.getWidth() - 15, 720 - (int)(boutonReglesPressed.getHeight()*2.2) - 15);
			}else{
				window.drawImage(boutonRegles, 1280 - boutonRegles.getWidth() - 15, 720 - (int)(boutonRegles.getHeight()*2.2) - 15);
			}
		}
	}
	
	/**
	 * gameStart lance le jeu (Generation du plateau de jeu)
	 * 
	 * @param window : la fenetre principale du jeu.
	 * @param background : le fond d'écran du jeu.
	 */
	void gameStart(TransparentImage window, TransparentImage background){
		//println(allMatieres);
		setTabMaxAnswer();
		TransparentImage gameLogo = newTransparentImage("gameLogo","./images/game/gameLogo.png");
		TransparentImage gameBoutonQuitter = newTransparentImage("gameBoutonQuitter","./images/game/bouton_quitter.png");
		TransparentImage boutonRegles = newTransparentImage("regles","./images/mainMenu/bouton_rules.png");
		TransparentImage tableauNoir = newTransparentImage("tableauScores","./images/game/tableauNoir.png");
		removeAllZone(window);
		drawImage(window, background, 0, 0);
		updateImage[3] = copySubset(window, "zoneQuestion", 0, 565, 1071, 720-565);
		updateImage[4] = copySubset(window, "zoneReponse", 0, 629, 1071, 91);
		window.drawImage(gameLogo, 15, 15);
		
		///Initialisation du tableau des scores
		window.drawImage(tableauNoir, 922, 0);
		initTableau(window, tableauNoir);
		
		window.drawImage(boutonRegles, 1280 - boutonRegles.getWidth() - 15, 720 - (int)(boutonRegles.getHeight()*2.2) - 15);
		window.drawImage(gameBoutonQuitter, background.getWidth() - gameBoutonQuitter.getWidth() - 15, background.getHeight() - gameBoutonQuitter.getHeight() - 15);
		int INITIALX = (background.getWidth()/2) - 200;
		int INITIALY = (background.getHeight()/2) - 105;
		plateauDeJeu(window,INITIALX, INITIALY, 3);
		clicked = "";
	}
	
	/**
	 * Initialise le tableau des scores.
	 * 
	 * @param window : la fenetre principale du jeu.
	 * @param tableauNoir : l'image du tableau des scores.
	 */
	void initTableau(TransparentImage window, TransparentImage tableauNoir){		
		int x = 922;
		int y = 169;
		updateImage[0] = copySubset(window, "circleALive", x, y-28, tableauNoir.getWidth(), 28*(nbrJoueurs));
		///Initialise la zone du tableau : Scores des joueurs
		drawScore(window, x, y);
		///Initialise la zone du tableau : Question(s) restante(s)
		window.setNewFont(police[1], "PLAIN", tabTaillePolice[3]);
		window.setColor(defCouleur(0));
		updateImage[1] = copySubset(window, "circleALive", x, 282, tableauNoir.getWidth(), 43);
		window.drawString("Questions restantes "+"J"+(int)(laps+1)+" : "+(int)(questRestantesJoueur[laps]-1), x + tableauNoir.getWidth()/2 - getStringLength(window, "Questions restantes "+"J"+(int)(laps+1)+" : "+(int)(questRestantesJoueur[laps]-1))/2, 312);
	}
	
	/**
	 * Met à jour le score des joueurs
	 * 
	 * @param window : la fenêtre principale du jeu
	 * @param x : position x
	 * @param y : position y
	 */
	void drawScore(TransparentImage window, int x, int y){
		TransparentImage tableauNoir = newTransparentImage("tableauScores","./images/game/tableauNoir.png");
		window.setNewFont(police[1], "PLAIN", tabTaillePolice[3]);
		for (int i=0; i<nbrJoueurs;++i){
			window.setColor(defCouleur(i+1));
			if (scoreJoueur[i]>1){
				window.drawString("J"+(i+1)+" : "+scoreJoueur[i] +" Points", x + tableauNoir.getWidth()/2 - getStringLength(window, "J"+(i+1)+" : "+scoreJoueur[i] +" Point")/2, y);
			}else{
				window.drawString("J"+(i+1)+" : "+scoreJoueur[i] +" Point", x + tableauNoir.getWidth()/2 - getStringLength(window, "J"+(i+1)+" : "+scoreJoueur[i] +" Point")/2, y);
			}
			y += 28;
		}
	}
	
	/**
	 * Update le nombre de questions restantes pour le joueur concerné.
	 * 
	 * @param window : la fenêtre principale du jeu
	 */
	void questRestPerPlayer(TransparentImage window){
		window.setColor(Color.WHITE);
		window.setNewFont(police[1], "PLAIN", tabTaillePolice[3]);
		window.drawImage(updateImage[1], 922, 282);
		if (questRestantesJoueur[(nbrJoueurs)-1] > 0){
			if(laps == (nbrJoueurs)){
				laps = 0;
				--questRestantesJoueur[laps];
				window.drawString("Questions restantes J"+(int)(laps+1)+" : "+(int)(questRestantesJoueur[laps]-1), 922 + 156 - getStringLength(window, "Questions restantes J"+(int)(laps+1)+" : "+(int)(questRestantesJoueur[laps]-1))/2, 312);
			}else{
				window.drawString("Questions restantes J"+(int)(laps+1)+" : "+(int)(questRestantesJoueur[laps]-1), 922 + 156 - getStringLength(window, "Questions restantes J"+(int)(laps+1)+" : "+(int)(questRestantesJoueur[laps]-1))/2, 312);
				--questRestantesJoueur[laps];
			}
		}else{
			window.drawString("Partie finie", 922 + 156 - getStringLength(window, "Partie finie")/2, 312);
			delay(2000);
			step = 3;
		}
	}
	
	final int HEXAXSIZE = 55;
	final int HEXAYSIZE = 77;
	int numQuest = 0;
	
	/**
	 * Generation des "cercles" hexagonaux, constituant une partie de la zone de jeu.
	 * 
	 * @param img : la fenetre principale du jeu.
	 * @param x : position x sur le plateau de jeu.
	 * @param y : position y sur le plateau de jeu.
	 * @param lvl : nombre de "cercle" hexagonal généré.
	 */
	void plateauDeJeu(TransparentImage img,int x, int y, int lvl){
		TransparentImage caseJaune = newTransparentImage("./images/game/caseJeu/caseJaune.png");
		TransparentImage caseBleu = newTransparentImage("./images/game/caseJeu/caseBleu.png");
		TransparentImage caseRouge = newTransparentImage("./images/game/caseJeu/caseRouge.png");
		TransparentImage numQuestion = newTransparentImage("./images/game/caseJeu/numQuestion.png");
		img.setNewFont(police[0], "PLAIN", tabTaillePolice[0]);
		img.setColor(Color.BLACK);
		finishQuest = new int [3*lvl*(lvl+1)+1][3];
		for (int i=1; i<=lvl ; ++i){
			x-= HEXAXSIZE;
			y-= HEXAYSIZE;
			if (i == 1){
				cercleHexa(img, caseJaune, numQuestion, x, y, i);
			}else if(i==2){
				cercleHexa(img, caseRouge, numQuestion, x, y, i);
			}else{
				cercleHexa(img, caseBleu, numQuestion, x, y, i);
			}
		}
	}
	
	/**
	 * Generation d'un "cercle" hexagonal.
	 * 
	 * @param img : la fenetre principale du jeu.
	 * @param caseJeu : image d'une case du plateau du jeu, forme hexagonale  (Couleur : jaune |ou| rouge |ou| bleu).
	 * @param numQuestion : image supperposée à l'image caseJeu permettant de savoir le numéro de la question.
	 * @param x : position x sur le plateau de jeu.
	 * @param y : position y sur le plateau de jeu.
	 * @param lvl : nombre de "cercle" hexadecimal généré.
	 */
	void cercleHexa(TransparentImage img, TransparentImage caseJeu, TransparentImage numQuestion, int x, int y, int lvl){
		for(int i = 0; i < lvl; ++i){
			x+= HEXAXSIZE*2;
			gameCase(img, caseJeu, numQuestion, x, y);
		}
		for(int i = 0; i < lvl; ++i){
			x+= HEXAXSIZE;
			y+= HEXAYSIZE;
			gameCase(img, caseJeu, numQuestion, x, y);
		}
		for(int i = 0; i < lvl; ++i){
			x-= HEXAXSIZE;
			y+= HEXAYSIZE;
			gameCase(img, caseJeu, numQuestion, x, y);
		}
		for(int i = 0; i < lvl; ++i){
			x-= HEXAXSIZE*2;
			gameCase(img, caseJeu, numQuestion, x, y);
		}
		for(int i = 0; i < lvl; ++i){
			x-= HEXAXSIZE;
			y-= HEXAYSIZE;
			gameCase(img, caseJeu, numQuestion, x, y);
		}
		for(int i = 0; i < lvl; ++i){
			x+= HEXAXSIZE;
			y-= HEXAYSIZE;
			gameCase(img, caseJeu, numQuestion, x, y);
		}
		drawImage(img, caseJeu, x, y);
		drawImage(img, matieresRandom[numQuest-1], x, y);
		drawImage(img, numQuestion, x + caseJeu.getWidth()/2 - numQuestion.getWidth()/2, y - caseJeu.getHeight()/2 + numQuestion.getHeight()/2);
		img.drawString(""+3*lvl*(lvl+1), x+caseJeu.getWidth()/2 - getStringLength(img, ""+numQuest)/2, y+caseJeu.getHeight()/2 + tabTaillePolice[0]/3 - 38);
	}
	
	/**
	 * Défini le contenu de la caseJeu et de numQuestion
	 * 
	 * @param img : la fenetre principale du jeu.
	 * @param caseJeu : image d'une case du plateau du jeu, forme hexagonale  (Couleur : jaune |ou| rouge |ou| bleu).
	 * @param numQuestion : image supperposée à l'image caseJeu permettant de savoir le numéro de la question.
	 * @param x : position x sur le plateau de jeu.
	 * @param y : position y sur le plateau de jeu.
	 */
	void gameCase(TransparentImage img, TransparentImage caseJeu, TransparentImage numQuestion, int x,int y){
		delay(20);
		drawImage(img, caseJeu, x, y);
		drawImage(img, numQuestion, x + caseJeu.getWidth()/2 - numQuestion.getWidth()/2, y - caseJeu.getHeight()/2 + numQuestion.getHeight()/2);
		numQuest += 1;
		img.drawString(""+numQuest, x+caseJeu.getWidth()/2 - getStringLength(img, ""+numQuest)/2, y+caseJeu.getHeight()/2 + tabTaillePolice[0]/3 - 38);
		addZone(img, "case"+numQuest, x, y, caseJeu.getWidth(), caseJeu.getHeight());
		drawImage(img, matieresRandom[numQuest-1], x, y);
		finishQuest[numQuest][0] = numQuest; 
		finishQuest[numQuest][1] = (int)(x + caseJeu.getWidth()/2 - numQuestion.getWidth()/2); 
		finishQuest[numQuest][2] = (int)(y - caseJeu.getHeight()/2 + numQuestion.getHeight()/2);
	}
	
	/**
	 * Défini le contenu de la caseQuest au fil du jeu.
	 * 
	 * @param window : la fenetre principale du jeu.
	 * @param caseQuest : image des cases réponses du plateau du jeu (forme octagonale).
	 * @param init : savoir si c'est la période d'initialisation ou non
	 */
	void caseReponse(TransparentImage window, int caseQuest, boolean init){
		int x = 150;
		int y = 629;
		tabTaillePolice[1] = 19;
		for(int i=0; i<4;++i){
			window.setNewFont(police[0], "PLAIN", tabTaillePolice[0]);
			window.setColor(defCouleur(0));
			char lettre = (char) ('A' + i);
			TransparentImage caseReponse = newTransparentImage("reponse"+lettre,"./images/game/caseJeu/caseReponse.png");
			drawImage(window, caseReponse,x, y);
			window.drawString(""+lettre, x+caseReponse.getWidth()/2 - getStringLength(window, ""+lettre)/2, y +caseReponse.getHeight()/2+ 15/3 - 34);
			if(!init){
				window.setNewFont(police[0], "PLAIN", tabTaillePolice[1]);
				window.setColor(defCouleur(5));
				while (getStringLength(window, matEtQuest[caseQuest][4+i]) > 130){
					--tabTaillePolice[1];
					window.setNewFont(police[0], "PLAIN", tabTaillePolice[1]);
				}
				window.drawString(matEtQuest[caseQuest][4+i], x+caseReponse.getWidth()/2 - getStringLength(window, matEtQuest[caseQuest][4+i])/2, y +caseReponse.getHeight()/2+ tabTaillePolice[1]/3);
				tabTaillePolice[1] = 19;
			}
			x += 180;
		}
	}
	
	/**
	 * Mise en place du décompte pour chaque question, temps initial : 30 secondes
	 * 
	 * @param window : la fenêtre principale du jeu
	 */
	void timer(final TransparentImage window) {
		timerQuestThread = new Thread(){
			public void run(){
				TransparentImage timer = newTransparentImage("./images/game/caseJeu/timer250ms.png");
				/// Récupère le temps actuel en millisecondes
				long temps = getTime();
				/// j = 30 secondes (en millisecondes)
				int j = Integer.parseInt(configVar[8][1])*1000;
				/// i = 250 millisecondes
				int i = 250;
				int h = 1000;
				/// Défini la position x et y, au niveau de l'image zoneQuestion
				int x = 51;
				int y = 614;
				/// Reinitialise les deux variables globales entre chaque question
				ptsBonus = j/2000;
				timerQuest = temps + j;
				while(getTime() <= (temps + j)){
					if(getTime() <= (temps + j/2) && getTime() >= (temps + h)){
						ptsBonus -= 1;
						h += 1000;
					}
					if(getTime() >= (temps + i)){
						window.drawImage(timer, x, y);
						x += 4;
						i += (int)(j/241);
					}
				}
			}
		};
		timerQuestThread.start();
	}
	
	/**
	 * Met des V de la couleur du joueur sur la case concerné, supprime la possibilité de re-répondre à la question et update les points des joueurs.
	 * 
	 * @param window : la fenêtre principale du jeu
	 * @param timerEnd : Savoir si le temps est écoulé ou non
	 */
	void successQuest(TransparentImage window){
		TransparentImage[] validQuest = new TransparentImage[]{newTransparentImage("./images/game/valideQuest/doneQuest_J1.png"), newTransparentImage("./images/game/valideQuest/doneQuest_J2.png"), newTransparentImage("./images/game/valideQuest/doneQuest_J3.png"), newTransparentImage("./images/game/valideQuest/doneQuest_J4.png")};
		window.drawImage(validQuest[laps],finishQuest[numQuestClicked][1], finishQuest[numQuestClicked][2]);
		removeZone(window, "case"+numQuestClicked);
		updateScore(window);
		window.drawImage(updateImage[4], 0, 629);
		goodRep(window);
		alreadyQuest = false;
		++laps;
	}
	
	/**
	 * Affiche la zone de texte, en dessous de la question, si c'est une bonne réponse (avec bonus temps s'il y en a un).
	 * 
	 * @param window : la fenêtre principale du jeu
	 */
	void goodRep(TransparentImage window){
		TransparentImage goodRep = newTransparentImage("./images/game/valideQuest/goodRep.png");
		window.drawImage(goodRep,147, 631);
		window.setColor(defCouleur(0));
		window.setNewFont(police[1], "PLAIN", tabTaillePolice[5]);
		window.drawString("BONNE REPONSE ! Vous avez gagné : "+ptsActualQuest()+"pts", 147+goodRep.getWidth()/2 - getStringLength(window, "BONNE REPONSE ! Vous avez gagné : "+ptsActualQuest())/2, 630 +goodRep.getHeight()/2+ tabTaillePolice[5]/3);
		if(bonusTimer() > 0){
			window.setColor(defCouleur(3));
			window.setNewFont(police[1], "PLAIN", tabTaillePolice[3]);
			window.drawString("BONUS TEMPS : +"+bonusTimer()+"pts", 832, 661 + tabTaillePolice[3]/3);
			window.setColor(defCouleur(0));
			window.setNewFont(police[1], "PLAIN", tabTaillePolice[4]);
			window.drawString("TOTAL : "+(int)(bonusTimer()+ptsActualQuest())+"pts", 807, 690 + tabTaillePolice[4]/3);
		}
	}
	
	/**
	 * Met des X de la couleur du joueur sur la case concerné et supprime la possibilité de re-répondre à la question.
	 * 
	 * @param window : la fenêtre principale du jeu
	 * @param timerEnd : Savoir si le temps est écoulé ou non
	 */
	void failQuest(TransparentImage window, boolean timerEnd){
		TransparentImage[] failQuest = new TransparentImage[]{newTransparentImage("./images/game/failQuest/failQuest_J1.png"), newTransparentImage("./images/game/failQuest/failQuest_J2.png"), newTransparentImage("./images/game/failQuest/failQuest_J3.png"), newTransparentImage("./images/game/failQuest/failQuest_J4.png")};
		window.drawImage(failQuest[laps],finishQuest[numQuestClicked][1], finishQuest[numQuestClicked][2]);
		removeZone(window, "case"+numQuestClicked);
		window.drawImage(updateImage[4], 0, 629);
		failRep(window, timerEnd);
		alreadyQuest = false;
		++laps;
	}
	
	/**
	 * Affiche la zone de texte, en dessous de la question, si c'est une mauvaise réponse ou si le temps est écoulé.
	 * 
	 * @param window : la fenêtre principale du jeu
	 * @param timerEnd : Savoir si le temps est écoulé ou non
	 */
	void failRep(TransparentImage window, boolean timerEnd){
		TransparentImage failRep = newTransparentImage("./images/game/failQuest/mauvaiseRep.png");
		window.drawImage(failRep,147, 631);
		window.setColor(defCouleur(0));
		window.setNewFont(police[1], "PLAIN", tabTaillePolice[5]);
		int fontSize = tabTaillePolice[5];
		if(timerEnd){
			while (getStringLength(window, "TEMPS ECOULE ! La bonne réponse était : "+matEtQuest[numQuestClicked-1][3]) > 548){
				--fontSize;
				window.setNewFont(police[1], "PLAIN", fontSize);
			}
			window.drawString("TEMPS ECOULE ! La bonne réponse était : "+matEtQuest[numQuestClicked-1][3], 147+failRep.getWidth()/2 - getStringLength(window, "TEMPS ECOULE ! La bonne réponse était : "+matEtQuest[numQuestClicked-1][3])/2, 630 +failRep.getHeight()/2+ tabTaillePolice[5]/3);
		}else{
			while (getStringLength(window, "PERDU ! La bonne réponse était : "+matEtQuest[numQuestClicked-1][3]) > 548){
				--fontSize;
				window.setNewFont(police[1], "PLAIN", fontSize);
			}
			window.drawString("PERDU ! La bonne réponse était : "+matEtQuest[numQuestClicked-1][3], 147+failRep.getWidth()/2 - getStringLength(window, "PERDU ! La bonne réponse était : "+matEtQuest[numQuestClicked-1][3])/2, 630 +failRep.getHeight()/2+ tabTaillePolice[5]/3);
		}
	}
	
	/**
	 * Update à chaque fin de question.
	 * 
	 * @param window : fenêtre principale du jeu
	 */
	void endQuest(TransparentImage window){
		println("-~-~-");
		timerQuestThread.stop();
		delay(4500);
		window.drawImage(updateImage[3], 0, 565);
		questRestPerPlayer(window);
		repQuest = false;
		clicked = " ";
	}
	
	/**
	 * Montant total du gain par question / par palier.
	 * 
	 * @return : le gain donné par la question suivant son palier
	 */
	int ptsActualQuest(){
		int pts = 0;
		if(numQuestClicked>=1 && numQuestClicked<=6){
			pts = Integer.parseInt(configVar[4][1]);
		}else if(numQuestClicked>=7 && numQuestClicked<=18){
			pts = Integer.parseInt(configVar[5][1]);
		}else{
			pts = Integer.parseInt(configVar[6][1]);
		}
		return pts;
	}
	
	/**
	 * Défini le nombre maximum de questions dans une partie (arrondi à l'entier inférieur)
	 */
	int setMaxAnswerPerPlayer(){
		if ((int)(Integer.parseInt(configVar[7][1])/(nbrJoueurs))>36/(nbrJoueurs)){
			println("ERREUR 201 : Out of Questions (Changez le nombre de questions dans le fichier config.csv)");
			System.exit(0);
		}
		return (int)(Integer.parseInt(configVar[7][1])/(nbrJoueurs));
		
	}
	
	/**
	 * Défini pour chaque joueur le même nombre de question suivant le nombre de questions totales dans la partie
	 */
	void setTabMaxAnswer(){
		for(int i=0; i<length(questRestantesJoueur);++i){
			questRestantesJoueur[i] = setMaxAnswerPerPlayer();
		}
	}
	
	/**
	 * Défini la difficulté entre chaque palier avec une nouvelle image pour chacunes d'entres elles
	 * 
	 * @param numQuest : numéro de la question (id)
	 * @return : l'image de la difficulté associé à l'id de la question
	 */
	TransparentImage difficulty(int numQuest){
		TransparentImage facileQuest = newTransparentImage("./images/game/difficulty/facileQuest.png");
		TransparentImage moyenQuest = newTransparentImage("./images/game/difficulty/moyenQuest.png");
		TransparentImage difficileQuest = newTransparentImage("./images/game/difficulty/difficileQuest.png");
		TransparentImage difficulty;
		if(numQuestClicked>=1 && numQuestClicked<=6){
			difficulty = facileQuest;
		}else if(numQuestClicked>=7 && numQuestClicked<=18){
			difficulty = moyenQuest;
		}else{
			difficulty = difficileQuest;
		}
		return difficulty;
	}
	
	/**
	 * Défini le bonus obtenu par le décompte
	 * 
	 * @return : la valeur bonus du décompte
	 */
	int bonusTimer(){
		return (int)((double)ptsBonus/(Integer.parseInt(configVar[8][1])/2)*Integer.parseInt(configVar[9][1]));
	}
	
	/**
	 * Ajoute les points aux joueurs à la fin de chaque bonne réponse.
	 * @param window : la fenêtre principale du jeu 
	 */
	void updateScore(TransparentImage window){
		int x = 922;
		int y = 169;
		if(numQuestClicked>=1 && numQuestClicked<=6){
			scoreJoueur[laps] += Integer.parseInt(configVar[4][1]) + bonusTimer();
		}else if(numQuestClicked>=7 && numQuestClicked<=18){
			scoreJoueur[laps] += Integer.parseInt(configVar[5][1]) + bonusTimer();
		}else{
			scoreJoueur[laps] += Integer.parseInt(configVar[6][1]) + bonusTimer();
		}
		window.drawImage(updateImage[0], x, y-26);
		drawScore(window, x, y);
	}
	
	/**
	 * Récupère qui est le premier de la partie et incrémente la variable globale vainqueur
	 */
	void whoIsFirst(){
		int score = 0;
		for(int j = 0; j<length(scoreJoueur);++j){
			int temp = scoreJoueur[j];;
			if(temp > score){
				score = temp;
				vainqueur[0] = "J"+(int)(j+1);
			}
		}
		vainqueur[1] = ""+score;
	}
	
	/**
	 * Fin du jeu, cette fonction affiche l'écran final avec le grand gagnant.
	 * 
	 * @param window : la fenêtre principale du jeu.
	 */
	void drawFirst(TransparentImage window){
		TransparentImage gameBoutonQuitter = newTransparentImage("gameBoutonQuitter","./images/game/bouton_quitter.png");
		whoIsFirst();
		TransparentImage finalBackground = newTransparentImage("./images/game/finalBackground.png");
		removeAllZone(window);
		window.drawImage(finalBackground, 0, 0);
		window.drawImage(gameBoutonQuitter, window.getWidth() - gameBoutonQuitter.getWidth() - 15, window.getHeight() - gameBoutonQuitter.getHeight() - 15);
		int couleur = Integer.parseInt(substring(vainqueur[0], 1, 2));
		window.setColor(defCouleur(couleur));
		window.setNewFont(police[1], "PLAIN", 70);
		window.drawString(vainqueur[0], window.getWidth()/2 - getStringLength(window, vainqueur[0])/2, window.getHeight()/2);
		window.setColor(defCouleur(5));
		window.setNewFont(police[1], "PLAIN", 30);
		window.drawString("Avec : "+vainqueur[1]+"pts", window.getWidth()/2 - getStringLength(window, "Avec : "+vainqueur[1]+"pts")/2, window.getHeight()/2+125);
	}
	
	///used for getMatiere
	String allMatieres = "";
	
	/**
	 * Initialise 3(variable initiale) séries de matières alétoirement (avec une même matière(palier 1), deux même matière(palier 2), 3 même matières(palier 3))
	 */
	void getMatiere(){
		String[] numMatieres = new String[]{randomizerInitialize(1,6,1),randomizerInitialize(1,6,2),randomizerInitialize(1,6,3)};
		for(int i = 0; i < length(numMatieres); i++){
			numMatieres[i] = ramdomizerNextInt(numMatieres[i]);
			allMatieres += numMatieres[i];
		}
	}
	
	/**
	 * Défini pour chaque case du plateau de jeu la matière lui correspondant.
	 */
	void setMatiere(){
		TransparentImage[] matieres = new TransparentImage []{newTransparentImage("./images/game/matieres/francais.png"), newTransparentImage("./images/game/matieres/cultureG.png"), newTransparentImage("./images/game/matieres/anglais.png"), newTransparentImage("./images/game/matieres/biologie.png"), newTransparentImage("./images/game/matieres/maths.png"), newTransparentImage("./images/game/matieres/geo.png")};
		for (int k = 0; k < 36; ++k){
			for (int j = 0; j < 6; ++j){
				String numMat = substring(allMatieres, k, k+1);
				int mat = Integer.parseInt(numMat);
				if((mat-1) == j){
					matieresRandom[k] = matieres[j];
				}
			}
		}
	}
	
	/// used for randomizerInitialize
	String rI;
	
	/**
	 * initialize the special randomizer
	 * @param x valeur minimale
	 * @param y valeur maximale (exclue)
	 * @param n repetition for each value
	 * @return rI chaine des matieres en fonction des tailles d'hexagones
	 */
	String randomizerInitialize(int x, int y, int n){
		rI = "";
		for(int z=x; z<=y; ++z){
			for(int i=0; i<n; ++i){
				rI += z;
			}
		}
		return rI;
	}
	
	/**
	 * Défini l'id de la matière
	 * @param numMatieres : Récupère une String contenant les id des matières dans un ordre aléatoire (taille de la string dépend du palier).
	 * @return s : une String de taille 1, contenant la matières dans un ordre aléatoire.
	 */
	String ramdomizerNextInt(String numMatieres){
		String s = "";
		int maxLenght = numMatieres.length();
		for(int j=0;j<maxLenght;j++){
			int i = (int) (numMatieres.length()*random());
			s += numMatieres.substring(i, i+1);
			numMatieres = numMatieres.substring(0,i) + numMatieres.substring(i+1, numMatieres.length());
		}
		return s;
	}
	
	String[][] matEtQuest = new String [36][8];
	int orderQuest = 0;
	
	/**
	 * Défini une question pour une case du plateau suivant la matière.
	 * 
	 * @param idMatiere : défini l'identifiant de la matière (1~6)
	 * @param palier : défini le palier de difficulté
	 */
	void setQuestionParMat(int idMatiere, int palier){
		CSVFile matiereCSV = loadCSV("./ressources/"+idMatiere+"/palier_"+palier+".csv", ';');
		int l1 = rowCount(matiereCSV);
		int l2 = columnCount(matiereCSV);
		int ran = (int)(random()*l1);
		matEtQuest[orderQuest][0] =  "case"+(int)(orderQuest+1);
		matEtQuest[orderQuest][1] =  ""+idMatiere;
		for(int i=0;i<l2;++i){
			matEtQuest[orderQuest][i+2] = getCell(matiereCSV,ran,i);
		}
		++orderQuest;
	}
	
	/**
	 * Défini toutes les questions du plateau.
	 */
	void setQuestion(){
		for(int i=0; i<6;++i){
			String idMatiere = substring(allMatieres,orderQuest,orderQuest+1);
			int id = Integer.parseInt(idMatiere);
			setQuestionParMat(id, 1);
		}
		for(int i=0; i<12;++i){
			String idMatiere = substring(allMatieres,orderQuest,orderQuest+1);
			int id = Integer.parseInt(idMatiere);
			setQuestionParMat(id, 2);
		}
		for(int i=0; i<18;++i){
			String idMatiere = substring(allMatieres,orderQuest,orderQuest+1);
			int id = Integer.parseInt(idMatiere);
			setQuestionParMat(id, 3);
		}
	}
	
	/**
	 * Ouvre un fichier .txt et écrit chaque ligne chaque ligne dans la console.
	 * 
	 * @param fichier : emplacement du fichier
	 */
	void openFileTxt(String fichier){
		String chaine="";
		
		//lecture du fichier texte	
		try{
			InputStream ips=new FileInputStream(fichier); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			String ligne;
			while ((ligne=br.readLine())!=null){
				System.out.println(ligne);
				chaine+=ligne+"\n";
			}
			br.close(); 
		}		
		catch (Exception e){
			System.out.println(e.toString());
		}
	}
	
	/**
	 * Declare les polices / taille utile au bon fonctionnement du jeu suivant l'OS.
	 */
	String[] police = new String[2];
	int[] tabTaillePolice = new int[6];
	
	void setPolice(){
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("win")){
			police[0] = configVar[19][1];
			tabTaillePolice[0] = Integer.parseInt(configVar[22][1]);
			tabTaillePolice[1] = Integer.parseInt(configVar[23][1]);
			police[1] = configVar[20][1];
			tabTaillePolice[2] = Integer.parseInt(configVar[25][1]);
			tabTaillePolice[3] = Integer.parseInt(configVar[26][1]);
			tabTaillePolice[4] = Integer.parseInt(configVar[27][1]);
			tabTaillePolice[5] = Integer.parseInt(configVar[28][1]);
		}else if(os.contains("linux")){
			police[0] = configVar[30][1];
			tabTaillePolice[0] = Integer.parseInt(configVar[33][1]);
			tabTaillePolice[1] = Integer.parseInt(configVar[34][1]);
			police[1] = configVar[31][1];
			tabTaillePolice[2] = Integer.parseInt(configVar[36][1]);
			tabTaillePolice[3] = Integer.parseInt(configVar[37][1]);
			tabTaillePolice[4] = Integer.parseInt(configVar[38][1]);
			tabTaillePolice[5] = Integer.parseInt(configVar[39][1]);
			/*String[] tab = getAllFontNames();
			for(int i = 0; i<length(tab);++i){
				println(tab[i]);
			}*/
		}
	}
	
	/**
	 * Défini une série de couleur, permettant un renvoie plus facile pour l'affichage des joueurs.
	 * 
	 * @param i : id-1 de la couleur
	 * @return couleur[i] : Renvoie la couleur associé, beaucoup utilisé pour les différentes couleurs des joueurs
	 */
	Color defCouleur(int i){
		Color[] couleur = new Color[]{Color.WHITE,Color.RED,Color.CYAN,Color.GREEN,Color.MAGENTA,Color.BLACK};
		return couleur[i];
	}
	
	/**
	 * Initialise toutes les variables du jeu à l'aide du fichier config.csv
	 */
	void initalisationVar(){
		CSVFile config = loadCSV("./ressources/config.csv", ';');
		int l1 = rowCount(config);
		int l2 = columnCount(config);
		configVar = new String[l1][l2];
		for(int i=0;i<l1;++i){
			for(int j=0; j<l2; ++j){
				configVar[i][j] = getCell(config, i, j);
			}
		}
		scoreJoueur = new int []{Integer.parseInt(configVar[11][1]),Integer.parseInt(configVar[12][1]),Integer.parseInt(configVar[13][1]),Integer.parseInt(configVar[14][1])};
	}
	
	/**
	 * Ouvre un menu contenant les règles du jeu.
	 */
	void menuRegles(){
		if (!regles.isShowing()){
			TransparentImage background = newTransparentImage("./images/regles.png");
			regles.drawImage(background, 0, 0);
			show(regles);
		}
		else{
			close(regles);
		}
		clicked = "";
	}
	
	/**
	 * Récupère les actions de la souris.
	 * 
	 * @param name : Récupère le nom de la zone
	 * @param x : position x
	 * @param y : position y
	 * @param button : bouton de la souris
	 * @param event : Si PRESSED / CLICKED / RELEASED
	 */
	void mouseChanged(String name, int x, int y, int button, String event){
		if(step == 0){
			/// Bouton Jouer
			if (x >= 329 && x<= 951 && y >= 420 && y<= 561 && button==1 && event.equals("PRESSED")){
				clicked = "Jouer";
			/// Bouton Quitter
			}else if (x >= 489 && x<= 792 && y >= 611 && y<= 689 && button==1 && event.equals("PRESSED")){
				println("Vous partez deja :( !?");
				clicked = "Quitter";
			/// Bouton Règles
			}else if (name.equals("regles") && button==1 && event.equals("PRESSED")){
				clicked = "openRules";
			}
		}
		if(step == 1){
			// Bouton Quitter
			if (x >= 489 && x<= 792 && y >= 611 && y<= 689 && button==1 && event.equals("PRESSED")){
				println("Vous auriez pu au moins commencer une partie :/ !");
				clicked = "Quitter";
			}
			// Mode de jeu : 1 Joueur
			else if (x >= 329 && x<= 648 && y >= 420 && y<= 498 && button==1 && event.equals("PRESSED")){
				println("Mode choisi : 1 Joueur");
				clicked = "1J";
			// Mode de jeu : 2 Joueurs
			}else if (x >= 651 && x<= 954 && y >= 420 && y<= 498 && button==1 && event.equals("PRESSED")){
				println("Mode choisi : 2 Joueurs");
				clicked = "2J";
			//  Mode de jeu : 3 Joueurs
			}else if (x >= 329 && x<= 648 && y >= 515 && y<= 593 && button==1 && event.equals("PRESSED")){
				println("Mode choisi : 3 Joueurs");
				clicked = "3J";
			//  Mode de jeu : 4 Joueurs
			}else if (x >= 651 && x<= 954 && y >= 515 && y<= 593 && button==1 && event.equals("PRESSED")){
				println("Mode choisi : 4 Joueurs");
				clicked = "4J";
			/// Bouton Règles
			}else if (name.equals("regles") && button==1 && event.equals("PRESSED")){
				clicked = "openRules";
			}
		}
		if(step == 2){
			/// Les cases du plateau de jeu
			for (int i = 1; i<=36;++i){
				if(name.equals("case"+i) && button==1 && event.equals("PRESSED") && !alreadyQuest){
					numQuestClicked = i;
					clicked = "case"+i;
				}
			}
			/// Les cases réponses du plateau de jeu
			for (int i = 0; i<4;++i){
				char lettre = (char)('A'+i);
				if(name.equals("reponse"+lettre) && button==1 && event.equals("PRESSED") && alreadyQuest){
					clicked = actualQuest[i][1];
					repQuest = true;
					println("Votre réponse : " + actualQuest[i][1]);
				}
			}
			/// Bouton Quitter
			if (name.equals("gameBoutonQuitter") && button==1 && event.equals("PRESSED")){
				println("Merci d'avoir testé notre jeu ! Bonne journée !");
				clicked = "QuitterIG";
			/// Bouton Règles
			}else if (name.equals("regles") && button==1 && event.equals("PRESSED")){
				clicked = "openRulesIG";
			}
		}
		/// Bouton Quitter
		if(step == 3 && name.equals("gameBoutonQuitter") && button==1 && event.equals("PRESSED")){
			println("Bravo "+vainqueur[0]+" ! Merci d'avoir testé notre jeu ! Bonne journée !");
			clicked = "QuitterIG";
		}
	}
	
	/**
	 * Permet de jouer en boucle la musique de fond du jeu en parallèle. (Possibilité de la désactivé donc le fichier config)
	 */
	void playMusicForever(){
		musicThread = new Thread(){
			public void run(){
				boolean firstSoundMusic = true;
				/// Defini le son principal du jeu.
				Sound windowMusic1 = newSound("./son/mainMusic_1.mp3");
				Sound windowMusic2 = newSound("./son/mainMusic_2.mp3");
				long temps = getTime();
				play(windowMusic1, true);
				while(true){
					if (getTime() == (temps + 219000)){
						if(firstSoundMusic){
							play(windowMusic2, true);
							firstSoundMusic = false;
						}else{
							play(windowMusic1, true);
							firstSoundMusic = true;
						}
						temps = getTime();
					}
				}
			}
		};
		musicThread.start();
	}

	/*
	 * Fonctions heritees 
	 */
	void keyChanged(char c, String event){
	}
	void mouseHasMoved(int x, int y){
	}
	void mouseIsDragged(int x,int y, int button, int clickCount){
	}
	void textEntered(String text){
	}
}