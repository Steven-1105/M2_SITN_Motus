-- Donnees de demonstration : 12 joueurs pour peupler le classement.
-- INSERT IGNORE = MySQL : ne recre pas les lignes deja presentes (idempotent au redemarrage).
-- Les IDs 1-12 sont reserves aux seeds ; les vrais joueurs commencent a partir de 100.

INSERT IGNORE INTO players (id, username, email, password, role) VALUES
  (1,  'Liya',    'liya@motus.local',    '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (2,  'Hongxiang',  'hongxiang@motus.local',  '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (3,  'Amelie',  'amelie@motus.local',  '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (4,  'Nathan',  'nathan@motus.local',  '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (5,  'Chloe',   'chloe@motus.local',   '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (6,  'Lucas',   'lucas@motus.local',   '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (7,  'Ines',    'ines@motus.local',    '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (8,  'Thomas',  'thomas@motus.local',  '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (9,  'Sarah',   'sarah@motus.local',   '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (10, 'Hugo',    'hugo@motus.local',    '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (11, 'Manon',   'manon@motus.local',   '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER'),
  (12, 'Julien',  'julien@motus.local',  '$2a$10$demoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDemoDeS', 'PLAYER');

-- Compte administrateur de demo (identifiant "admin", mot de passe "admin123").
-- Hash BCrypt reel (contrairement aux joueurs ci-dessus) pour permettre la connexion.
INSERT IGNORE INTO players (id, username, email, password, role) VALUES
  (13, 'admin', 'admin@motus.local', '$2b$10$lscMQ6NpUUd.hpEeVTJRjupTsybCvM.CVVWGcZTyiPPbUIvDaP.7a', 'ADMIN');

-- Les vrais joueurs (inscrits depuis le front) demarrent a l'ID 100.
ALTER TABLE players AUTO_INCREMENT = 100;
