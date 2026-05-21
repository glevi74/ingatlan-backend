-- Mintaadatok fejlesztési módhoz
-- Quarkus automatikusan futtatja induláskor (ha hibernate.sql-load-script=import.sql)

INSERT INTO ugyfelek (id, nev, email, telefon, szerep, gdpr_beleegyezes, letrehozva, modositva)
VALUES
  ('a1000000-0000-0000-0000-000000000001', 'Kovács János', 'kovacs.janos@example.hu', '+36301234567', 'ELADO', '2024-01-10', NOW(), NOW()),
  ('a1000000-0000-0000-0000-000000000002', 'Nagy Mária', 'nagy.maria@example.hu', '+36209876543', 'VEVO', '2024-02-15', NOW(), NOW()),
  ('a1000000-0000-0000-0000-000000000003', 'Szabó Péter', 'szabo.peter@example.hu', '+36701112233', 'BERLETI_ADO', '2024-03-01', NOW(), NOW());

INSERT INTO ingatlanok (id, cim, helyrajzi_szam, tipus, alapterulet, szobaszam, emelet, allapot, energetikai_osztaly, leiras, letrehozva, modositva)
VALUES
  ('b2000000-0000-0000-0000-000000000001', 'Budapest XIII., Váci út 100.', '29234/1/A/12', 'LAKAS', 68.5, 3, 4, 'FELUJITOTT', 'B', 'Felújított, napfényes lakás a XIII. kerületben.', NOW(), NOW()),
  ('b2000000-0000-0000-0000-000000000002', 'Budapest II., Rózsadomb, Fekete Sas utca 5.', '14567/A/2', 'HAZ', 220.0, 6, 0, 'JO', 'C', 'Csendes, zöldövezeti ház kerttel.', NOW(), NOW());

INSERT INTO megbizasok (id, ugyfel_id, ingatlan_id, tipus, kezdete, vege, jutalek_szazalek, status, letrehozva)
VALUES
  ('c3000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'b2000000-0000-0000-0000-000000000001', 'KIZAROLAGOS', '2024-04-01', '2024-10-01', 3.0, 'AKTIV', NOW()),
  ('c3000000-0000-0000-0000-000000000002', 'a1000000-0000-0000-0000-000000000001', 'b2000000-0000-0000-0000-000000000002', 'NEM_KIZAROLAGOS', '2024-05-01', '2024-11-01', 2.5, 'AKTIV', NOW());

INSERT INTO hirdetesek (id, megbizas_id, ker_ar, portal, status, indulas, megtekintesek, letrehozva)
VALUES
  ('d4000000-0000-0000-0000-000000000001', 'c3000000-0000-0000-0000-000000000001', 42900000, 'ingatlan.com', 'AKTIV', '2024-04-02', 127, NOW()),
  ('d4000000-0000-0000-0000-000000000002', 'c3000000-0000-0000-0000-000000000002', 189000000, 'ingatlan.com', 'AKTIV', '2024-05-03', 54, NOW());
