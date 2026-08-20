-- Fix candidate photo URLs with gap-aware Cloudinary folders.
-- Business mapping:
-- - candidate id 1..36 (existing presidential candidates)
-- - folders follow parties list positions 1..38 skipping 4 and 14

UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780324416/EMS/candidates/1/photo.png' WHERE id = 1;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780324418/EMS/candidates/2/photo.png' WHERE id = 2;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780324420/EMS/candidates/3/photo.png' WHERE id = 3;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330902/EMS/candidates/5/photo.png' WHERE id = 4;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330904/EMS/candidates/6/photo.png' WHERE id = 5;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330906/EMS/candidates/7/photo.png' WHERE id = 6;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330908/EMS/candidates/8/photo.png' WHERE id = 7;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330910/EMS/candidates/9/photo.png' WHERE id = 8;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330912/EMS/candidates/10/photo.png' WHERE id = 9;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330914/EMS/candidates/11/photo.png' WHERE id = 10;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330916/EMS/candidates/12/photo.png' WHERE id = 11;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330917/EMS/candidates/13/photo.png' WHERE id = 12;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330919/EMS/candidates/15/photo.png' WHERE id = 13;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330921/EMS/candidates/16/photo.png' WHERE id = 14;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330923/EMS/candidates/17/photo.png' WHERE id = 15;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330925/EMS/candidates/18/photo.png' WHERE id = 16;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330927/EMS/candidates/19/photo.png' WHERE id = 17;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330929/EMS/candidates/20/photo.png' WHERE id = 18;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330931/EMS/candidates/21/photo.png' WHERE id = 19;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330933/EMS/candidates/22/photo.png' WHERE id = 20;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330935/EMS/candidates/23/photo.png' WHERE id = 21;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330938/EMS/candidates/24/photo.png' WHERE id = 22;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330940/EMS/candidates/25/photo.png' WHERE id = 23;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330942/EMS/candidates/26/photo.png' WHERE id = 24;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330943/EMS/candidates/27/photo.png' WHERE id = 25;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330945/EMS/candidates/28/photo.png' WHERE id = 26;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330947/EMS/candidates/29/photo.png' WHERE id = 27;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330949/EMS/candidates/30/photo.png' WHERE id = 28;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330951/EMS/candidates/31/photo.png' WHERE id = 29;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330952/EMS/candidates/32/photo.png' WHERE id = 30;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330954/EMS/candidates/33/photo.png' WHERE id = 31;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330956/EMS/candidates/34/photo.png' WHERE id = 32;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330959/EMS/candidates/35/photo.png' WHERE id = 33;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330961/EMS/candidates/36/photo.png' WHERE id = 34;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330963/EMS/candidates/37/photo.png' WHERE id = 35;
UPDATE dbo.candidates SET photo_url = 'https://res.cloudinary.com/dvgangolz/image/upload/v1780330965/EMS/candidates/38/photo.png' WHERE id = 36;
