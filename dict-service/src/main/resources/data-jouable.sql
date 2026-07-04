-- ================================================================
--  Sous-dictionnaire des mots mysteres.
--
--  data.sql garde un dictionnaire large pour accepter les propositions
--  (conjugaisons, pluriels, formes derivees).
--  Ici, on marque seulement des mots francais courants et raisonnables
--  comme reponses possibles du jeu.
-- ================================================================

UPDATE mot SET jouable = TRUE WHERE mot IN (
  -- 5 lettres
  'AIMER','ALLER','AMOUR','ARBRE','AVOIR','BLANC','BRUIT','CALME','CARTE',
  'CHIEN','CHOSE','COEUR','ECOLE','FAIRE','FEMME','FLEUR','FORCE','FORME',
  'FRUIT','GRAND','HERBE','HOMME','JOUER','JEUNE','LAVER','LIVRE','MONDE',
  'NEIGE','NUAGE','ODEUR','PIECE','PLAGE','PLUIE','PORTE','ROUGE','ROUTE',
  'SALLE','SUCRE','TABLE','TERRE','VERRE','VILLE',

  -- 6 lettres
  'AMITIE','ANIMAL','BANANE','BEURRE','BUREAU','CAHIER','CHEVAL','CITRON',
  'COPAIN','COPINE','COURIR','CRAYON','DORMIR','ECRIRE','ENFANT','FACILE',
  'GARCON','GATEAU','JARDIN','LEGUME','MAISON','MANGER','MOUTON',
  'OISEAU','ORANGE','OUVRIR','PAPIER','PARLER','PARTIR','POULET','RAPIDE',
  'SAVOIR','SIMPLE','SOLEIL','SORTIR','TOMATE',

  -- 7 lettres
  'ACHETER','BALANCE','BOISSON','CHAMBRE','CHAPEAU','CHEMISE','CHOISIR',
  'COLLEGE','CUISINE','ECOUTER','FAMILLE','FENETRE','FROMAGE','GUERIR',
  'GUITARE','MANTEAU','MARCHER','MEDECIN','POMPIER','POUVOIR',
  'PRENDRE','REPONSE','TARTINE','TRAVAIL','TROUVER','VOITURE','VOULOIR',

  -- 8 lettres
  'ATTENDRE','BAGUETTE','CARTABLE','CHERCHER','CHOCOLAT','CUILLERE',
  'DENTISTE','ELEPHANT','ESCARGOT','FONTAINE','HISTOIRE','MONTAGNE',
  'PARTAGER','PREPARER','QUESTION','REGARDER','REPONDRE','TROTTOIR',

  -- 9 lettres
  'APPRENDRE','ASCENSEUR','BOULANGER','BOUTEILLE','CHAUSSURE','CONFITURE',
  'CONTINUER','CROCODILE','CROISSANT','DECOUVRIR','DINOSAURE','JARDINIER',
  'MANDARINE','PARAPLUIE','PRINTEMPS','SERVIETTE','TELEPHONE'
);
