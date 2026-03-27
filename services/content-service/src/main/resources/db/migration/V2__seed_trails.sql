DELETE FROM trails
WHERE user_id = 'seed-user'
  AND title = 'seed-title'
  AND description = 'seed-description'
  AND category = 'seed-category';

INSERT INTO trails (user_id, title, description, category, premium, created_at)
SELECT 'clara-rocha', 'Respirar antes de reagir', 'Uma trilha curta para desacelerar o corpo quando o dia comeca acelerado.', 'ansiedade', false, TIMESTAMPTZ '2026-03-15T07:00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE user_id = 'clara-rocha' AND title = 'Respirar antes de reagir'
);

INSERT INTO trails (user_id, title, description, category, premium, created_at)
SELECT 'clara-rocha', 'Sono que acolhe', 'Audios e pequenas praticas para desacoplar a mente e preparar a noite.', 'sono', true, TIMESTAMPTZ '2026-03-15T19:30:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE user_id = 'clara-rocha' AND title = 'Sono que acolhe'
);

INSERT INTO trails (user_id, title, description, category, premium, created_at)
SELECT 'clara-rocha', 'Foco gentil para dias intensos', 'Exercicios para organizar prioridade sem se cobrar alem do necessario.', 'foco', false, TIMESTAMPTZ '2026-03-16T09:00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE user_id = 'clara-rocha' AND title = 'Foco gentil para dias intensos'
);

INSERT INTO trails (user_id, title, description, category, premium, created_at)
SELECT 'clara-rocha', 'Rotina que sustenta', 'Passos simples para transformar intencao em consistencia com menos atrito.', 'rotina', false, TIMESTAMPTZ '2026-03-17T11:00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE user_id = 'clara-rocha' AND title = 'Rotina que sustenta'
);

INSERT INTO trails (user_id, title, description, category, premium, created_at)
SELECT 'clara-rocha', 'Autocuidado sem culpa', 'Uma jornada para recuperar pausas pequenas e mais respeito pelo proprio ritmo.', 'autocuidado', true, TIMESTAMPTZ '2026-03-18T13:00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE user_id = 'clara-rocha' AND title = 'Autocuidado sem culpa'
);

INSERT INTO trails (user_id, title, description, category, premium, created_at)
SELECT 'clara-rocha', 'Presenca em 10 minutos', 'Check-ins guiados para voltar ao agora quando a mente se dispersa.', 'presenca', false, TIMESTAMPTZ '2026-03-19T15:00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE user_id = 'clara-rocha' AND title = 'Presenca em 10 minutos'
);

INSERT INTO trails (user_id, title, description, category, premium, created_at)
SELECT 'clara-rocha', 'Recomecar depois de um dia dificil', 'Uma trilha para reorganizar energia e retomar a rotina sem rigidez.', 'recomeco', false, TIMESTAMPTZ '2026-03-20T18:00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE user_id = 'clara-rocha' AND title = 'Recomecar depois de um dia dificil'
);

INSERT INTO trails (user_id, title, description, category, premium, created_at)
SELECT 'clara-rocha', 'Mindfulness para quem vive correndo', 'Praticas acessiveis para trazer clareza no meio da agenda cheia.', 'mindfulness', true, TIMESTAMPTZ '2026-03-21T06:45:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE user_id = 'clara-rocha' AND title = 'Mindfulness para quem vive correndo'
);
