-- Petit dictionnaire pour les TESTS (le vrai dico de 132k mots ralentirait chaque test).
INSERT INTO mot (mot, longueur, jouable) VALUES ('PONT', 4, FALSE), ('CUBE', 4, FALSE), ('ROSE', 4, FALSE);
-- MARIE et TWIST ne sont PAS dans le dictionnaire (retires a la source dans data.sql).
INSERT INTO mot (mot, longueur, jouable) VALUES
  ('TABLE', 5, TRUE), ('CHIEN', 5, TRUE), ('FLEUR', 5, TRUE), ('ROUTE', 5, TRUE),
  ('ARBRE', 5, TRUE), ('ASSEZ', 5, TRUE), ('SALLE', 5, TRUE),
  ('PRIER', 5, FALSE), ('PRIEZ', 5, FALSE);
INSERT INTO mot (mot, longueur, jouable) VALUES
  ('MAISON', 6, TRUE), ('MOUTON', 6, TRUE), ('JARDIN', 6, TRUE), ('SOLEIL', 6, TRUE),
  ('BANANE', 6, TRUE), ('BRAQUA', 6, FALSE), ('SALLES', 6, FALSE);
INSERT INTO mot (mot, longueur, jouable) VALUES
  ('FROMAGE', 7, TRUE), ('VOITURE', 7, TRUE), ('CHAPEAU', 7, TRUE),
  ('GUITARE', 7, TRUE), ('BALANCE', 7, TRUE), ('BRAQUER', 7, FALSE);
INSERT INTO mot (mot, longueur, jouable) VALUES
  ('ELEPHANT', 8, TRUE), ('MONTAGNE', 8, TRUE), ('CHEVALET', 8, TRUE),
  ('BROUETTE', 8, TRUE), ('ESCARGOT', 8, TRUE);
INSERT INTO mot (mot, longueur, jouable) VALUES
  ('CROCODILE', 9, TRUE), ('MANDARINE', 9, TRUE), ('CAFARDEUX', 9, FALSE);
