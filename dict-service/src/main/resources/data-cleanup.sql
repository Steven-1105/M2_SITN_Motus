-- ================================================================
--  Nettoyage du dictionnaire au demarrage (execute apres data.sql).
--  But : supprimer les mots qui polluent le jeu (bug remonte par
--  Liya : le tirage aleatoire donnait "TOMAS", un prenom).
--
--  Strategie (specifique MySQL, cf. REGEXP) :
--   1) Prenoms courants (liste noire manuelle)
--   2) Terminaisons de conjugaison SANS faux positif connu
--      (futur, conditionnel, subjonctif imparfait, imparfait 3P)
--   3) -AIS / -AIT : whitelist courte des noms/adjectifs frequents,
--      suppression du reste (presque toujours des conjugaisons).
--
--  NB : on ne touche pas a -ONS, -ENT (trop de noms communs
--       finiraient supprimes : "canons", "argent", "vent"...).
-- ================================================================

-- 1) PRENOMS courants (garcons + filles + prenoms internationaux frequents)
DELETE FROM mot WHERE mot IN (
  'TOMAS','THOMAS','JULIEN','MARIE','PIERRE','JEAN','JACQUES','PAUL','LOUIS','ANDRE',
  'ROBERT','HENRI','MICHEL','DENIS','MARCEL','ALBERT','MAURICE','ROGER','CAMILLE','PATRICK',
  'DIDIER','NICOLAS','JEROME','PASCAL','LAURENT','THIERRY','FREDERIC','PHILIPPE','CHRISTIAN','ARNAUD',
  'MARTIN','ADAM','LEON','OSCAR','VICTOR','AXEL','BENJAMIN','ANTOINE','LUCAS','HUGO',
  'NATHAN','GABRIEL','ADRIEN','ROMEO','LEO','MATHIS','MATHIAS','ROBIN','TRISTAN','VINCENT',
  'ARTHUR','ETIENNE','GASPARD','RAPHAEL','SIMON','SEBASTIEN','SAMUEL','THEO','DAVID','ALEXIS',
  'JULIETTE','ALICE','JULIE','LOUISE','CLAIRE','CECILE','LAURA','HELENE','ANNE','PAULINE',
  'VIRGINIE','VALERIE','SYLVIE','ISABELLE','VERONIQUE','MICHELE','CAROLE','PATRICIA','MARTINE','AGNES',
  'CHLOE','EMMA','JULIA','EMILIE','DELPHINE','LEA','INES','MAYA','LUNA','EVA',
  'JADE','ZOE','LOLA','ROMANE','ROSE','IRIS','CLARA','LOUISE','ANAIS','MELISSA',
  'SARAH','MANON','AMELIE','MARINE','MARGAUX','SOPHIE','ELISE','CELINE','NATHALIE','CHRISTINE',
  'STEVEN','LIYA','KEVIN','BRIAN','ALEX','ERIC','FRANK','GARY','HARRY','MARIA'
);

-- 2) TERMINAISONS DE CONJUGAISON SANS FAUX POSITIF CONNU
--    (futur, conditionnel, imparfait 3e pers pluriel, subjonctif imparfait)
DELETE FROM mot WHERE mot REGEXP
  '(ERAI|ERAS|ERONS|ERONT|ERAIS|ERAIT|ERIONS|ERAIENT|AIENT|ASSE|ASSES|ASSENT|ASSIONS|ISSE|ISSES|ISSENT|ISSIONS|USSE|USSES|USSENT|USSIONS)$';

-- 3) -AIS avec whitelist (noms + adjectifs frequents)
DELETE FROM mot WHERE mot REGEXP 'AIS$' AND mot NOT IN (
  'DAIS','JAIS','LAIS','MAIS','GAIS','BAIS','FRAIS','VRAIS','LAIS','ANGLAIS','MARAIS','PALAIS','RELAIS','QUAIS','MAUVAIS','BIAIS','BALAIS','PORTUGAIS','JAMAIS','DESORMAIS','BENGALAIS','SAIS','JAPONAIS','LIBANAIS','SENEGALAIS','TAIS','FAIS','VAIS','SIAMOIS','DAIS','JURASSIQUE'
);

-- 4) -AIT avec whitelist
DELETE FROM mot WHERE mot REGEXP 'AIT$' AND mot NOT IN (
  'LAIT','FAIT','TRAIT','EXTRAIT','ATTRAIT','PORTRAIT','BIENFAIT','SOUHAIT','FORFAIT','ABSTRAIT','MEFAIT','RETRAIT','METHAIT','SATISFAIT','PARFAIT','STRAIT','LAIT','PORTRAITS','SURFAIT'
);

-- 5) SUISSE et cas connus a re-inserer (au cas ou la regex les a mangés)
INSERT IGNORE INTO mot (mot, longueur) VALUES
  ('SUISSE', 6), ('CAISSE', 6), ('ANANAS', 6), ('LAISSE', 6), ('BAISSE', 6),
  ('BROSSE', 6), ('BOSSE', 5), ('POISSE', 6), ('FESSE', 5), ('MASSE', 5),
  ('CASSE', 5), ('TASSE', 5), ('PASSE', 5), ('LASSE', 5), ('RUSSE', 5),
  ('MOUSSE', 6), ('CROSSE', 6), ('CROUSSE', 7);
