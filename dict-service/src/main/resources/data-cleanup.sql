-- ================================================================
--  Nettoyage du dictionnaire au demarrage (execute apres data.sql).
--  Objectif : supprimer UNIQUEMENT les mots qui polluent le TIRAGE
--  (prenoms), sans casser la VALIDATION (le joueur doit pouvoir
--  taper une conjugaison ou une forme derivee comme proposition).
--
--  Les conjugaisons restent en BDD et sont acceptees a /words/validate,
--  mais elles sont exclues du tirage aleatoire par une regex dans
--  findRandomJouableByLongueur (cf. MotRepository).
-- ================================================================

-- PRENOMS courants (garcons + filles + prenoms internationaux)
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
  'JADE','ZOE','LOLA','ROMANE','ROSE','IRIS','CLARA','ANAIS','MELISSA',
  'SARAH','MANON','AMELIE','MARINE','MARGAUX','SOPHIE','ELISE','CELINE','NATHALIE','CHRISTINE',
  'STEVEN','LIYA','KEVIN','BRIAN','ALEX','ERIC','FRANK','GARY','HARRY','MARIA'
);
