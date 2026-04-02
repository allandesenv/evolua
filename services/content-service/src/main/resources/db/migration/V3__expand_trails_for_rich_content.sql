ALTER TABLE trails
ADD COLUMN IF NOT EXISTS summary VARCHAR(400);

ALTER TABLE trails
ADD COLUMN IF NOT EXISTS content TEXT;

ALTER TABLE trails
ADD COLUMN IF NOT EXISTS media_links TEXT NOT NULL DEFAULT '[]';

UPDATE trails
SET summary = description
WHERE summary IS NULL;

UPDATE trails
SET content = description
WHERE content IS NULL;

UPDATE trails
SET content = E'# Respirar antes de reagir\n\nUma trilha curta para desacelerar o corpo quando o dia comeca acelerado.\n\n## Como usar\n- reserve 5 minutos\n- diminua o ritmo da respiracao\n- finalize com uma anotacao curta sobre o que mudou'
WHERE title = 'Respirar antes de reagir';

UPDATE trails
SET media_links = '[{"label":"Respiracao guiada de apoio","url":"https://www.youtube.com/watch?v=inpok4MKVLM","type":"youtube"}]'
WHERE title = 'Respirar antes de reagir';

UPDATE trails
SET content = E'# Sono que acolhe\n\nAudios e pequenas praticas para desacoplar a mente e preparar a noite.\n\n## Estrutura\n- desaceleracao do corpo\n- reducao de estimulos\n- fechamento do dia com mais maciez'
WHERE title = 'Sono que acolhe';

UPDATE trails
SET media_links = '[{"label":"Video de relaxamento para a noite","url":"https://www.youtube.com/watch?v=aEqlQvczMJQ","type":"youtube"},{"label":"Guia complementar","url":"https://example.com/sono-que-acolhe","type":"article"}]'
WHERE title = 'Sono que acolhe';

ALTER TABLE trails
ALTER COLUMN summary SET NOT NULL;

ALTER TABLE trails
ALTER COLUMN content SET NOT NULL;
