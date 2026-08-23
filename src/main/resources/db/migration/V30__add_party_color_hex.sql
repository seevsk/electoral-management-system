-- Adds a stable brand color per party, used to color each candidate's bar
-- in the presidential results chart. Palette generated deterministically via
-- OKLCH golden-angle hue stepping (L=0.62, C=0.16, boosted to 0.20 in the
-- teal band 150-215deg to clear the gamut-clipped chroma floor there), so
-- each of the 38 parties gets a distinct, light/vivid hue within a validated
-- lightness/contrast band.
ALTER TABLE dbo.parties ADD color_hex VARCHAR(7) NULL;
GO

UPDATE dbo.parties SET color_hex = '#00a3bf' WHERE id = 1;
UPDATE dbo.parties SET color_hex = '#c559a0' WHERE id = 2;
UPDATE dbo.parties SET color_hex = '#7b9300' WHERE id = 3;
UPDATE dbo.parties SET color_hex = '#4185e4' WHERE id = 4;
UPDATE dbo.parties SET color_hex = '#d45b3d' WHERE id = 5;
UPDATE dbo.parties SET color_hex = '#00a979' WHERE id = 6;
UPDATE dbo.parties SET color_hex = '#a466cd' WHERE id = 7;
UPDATE dbo.parties SET color_hex = '#af7d00' WHERE id = 8;
UPDATE dbo.parties SET color_hex = '#0097ce' WHERE id = 9;
UPDATE dbo.parties SET color_hex = '#d1557e' WHERE id = 10;
UPDATE dbo.parties SET color_hex = '#4a9c37' WHERE id = 11;
UPDATE dbo.parties SET color_hex = '#7179e4' WHERE id = 12;
UPDATE dbo.parties SET color_hex = '#cc6600' WHERE id = 13;
UPDATE dbo.parties SET color_hex = '#00a7a6' WHERE id = 14;
UPDATE dbo.parties SET color_hex = '#ba5db3' WHERE id = 15;
UPDATE dbo.parties SET color_hex = '#918b00' WHERE id = 16;
UPDATE dbo.parties SET color_hex = '#008cdf' WHERE id = 17;
UPDATE dbo.parties SET color_hex = '#d55758' WHERE id = 18;
UPDATE dbo.parties SET color_hex = '#00a85a' WHERE id = 19;
UPDATE dbo.parties SET color_hex = '#936dd9' WHERE id = 20;
UPDATE dbo.parties SET color_hex = '#bc7400' WHERE id = 21;
UPDATE dbo.parties SET color_hex = '#00a0cc' WHERE id = 22;
UPDATE dbo.parties SET color_hex = '#ca5793' WHERE id = 23;
UPDATE dbo.parties SET color_hex = '#6a9705' WHERE id = 24;
UPDATE dbo.parties SET color_hex = '#5680e6' WHERE id = 25;
UPDATE dbo.parties SET color_hex = '#d25f2a' WHERE id = 26;
UPDATE dbo.parties SET color_hex = '#00a98b' WHERE id = 27;
UPDATE dbo.parties SET color_hex = '#ad63c4' WHERE id = 28;
UPDATE dbo.parties SET color_hex = '#a48300' WHERE id = 29;
UPDATE dbo.parties SET color_hex = '#0093d5' WHERE id = 30;
UPDATE dbo.parties SET color_hex = '#d3556f' WHERE id = 31;
UPDATE dbo.parties SET color_hex = '#2c9f49' WHERE id = 32;
UPDATE dbo.parties SET color_hex = '#7f74e1' WHERE id = 33;
UPDATE dbo.parties SET color_hex = '#c76b00' WHERE id = 34;
UPDATE dbo.parties SET color_hex = '#00a5b6' WHERE id = 35;
UPDATE dbo.parties SET color_hex = '#c15ba8' WHERE id = 36;
UPDATE dbo.parties SET color_hex = '#849000' WHERE id = 37;
UPDATE dbo.parties SET color_hex = '#3188e3' WHERE id = 38;
