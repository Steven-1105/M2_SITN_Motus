-- ================================================================
--  Nettoyage du dictionnaire au demarrage (execute apres data.sql).
--  Objectif : supprimer UNIQUEMENT les mots qui ne sont pas souhaites
--  comme propositions (prenoms), sans casser la VALIDATION des vrais
--  mots francais (pluriels, conjugaisons, formes derivees).
--
--  Les conjugaisons restent en BDD et sont acceptees a /words/validate,
--  mais elles sont exclues du tirage aleatoire par findRandomJouableByLongueur
--  quand elles peuvent etre reliees a un infinitif connu (cf. MotRepository).
--  Meme principe pour les pluriels simples : SALLES reste une proposition
--  valide, mais le tirage prefere SALLE.
-- ================================================================

-- ================================================================
--  1. Mots BIZARRES signales par les joueurs.
--     On ne les supprime plus ici : certains existent vraiment et doivent
--     rester acceptes comme propositions. Ils sont seulement exclus du tirage
--     par MOTS_JAMAIS_JOUABLES dans MotRepository.
-- ================================================================

-- ================================================================
--  2. PLURIELS.
--     On ne les supprime plus ici : SALLES, ROUTES, MAISONS... sont de
--     vrais mots et doivent rester acceptes quand le joueur les propose.
--     Ils sont seulement exclus du tirage aleatoire par MotRepository si
--     leur singulier existe dans le dictionnaire.
-- ================================================================

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

-- ================================================================
--  4. Mots anglais / anglicismes que l'on refuse meme en validation.
--     Le sous-dictionnaire jouable evite deja qu'ils sortent comme reponses ;
--     cette liste garde aussi /words/validate coherent avec le choix du projet.
-- ================================================================
DELETE FROM mot
WHERE mot LIKE 'TWIST%'
   OR mot IN (
     'BABY','BABYS','BOSS','CASH','COOL','GIRL','JOBS','LOOK','LIVE','LOVE',
     'WEEK','WEEKS','WEEKEND','WEEKENDS'
   );
