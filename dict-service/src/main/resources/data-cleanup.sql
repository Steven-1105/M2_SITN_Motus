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

-- ================================================================
--  1. Mots BIZARRES signales par les joueurs (conjugaisons archaiques,
--     mots litteraires rares, formes qui n'ont pas de definition Larousse).
--     A enrichir au fil des bugs remontes.
-- ================================================================
DELETE FROM mot WHERE mot IN (
  'POINS','POIGNIS','POIGNIT','OIGNIS','OIGNIT',   -- conjugaisons de POINDRE / OINDRE (rares)
  'ABIMONS','ABIMEZ','ABIMAT',                      -- conjugaisons d'ABIMER (formes rares)
  'ABATTIS','ABATTIT',                              -- conjugaisons d'ABATTRE (passe simple)
  'ACHOPPAS','ACHOPPAT',                            -- passe simple d'ACHOPPER
  'AILEZ','AILAIT',                                 -- verbe AILER (rare)
  'OYEZ','OYIONS','OYAIT'                           -- OUIR (litteraire)
);

-- ================================================================
--  2. PLURIELS : on supprime les mots en -S dont le SINGULIER existe
--     deja dans le dictionnaire. Ex : MAISONS supprime (MAISON existe),
--     CHIENS supprime (CHIEN existe). Les mots singuliers en -S
--     (MAIS, PAYS, TEMPS, CORPS, POIDS...) sont preserves car leur
--     "singulier" (MAI, PAY, TEMP, CORP, POID) n'existe pas en base.
--     Cette regle intelligente evite d'ecrire une whitelist manuelle.
-- ================================================================
DELETE m1 FROM mot m1
JOIN mot m2 ON m2.mot = SUBSTRING(m1.mot, 1, LENGTH(m1.mot) - 1)
WHERE m1.mot LIKE '%S'
  AND m1.longueur >= 5;

-- ================================================================
--  3. PRENOMS courants (garcons + filles + prenoms internationaux)
-- ================================================================
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
