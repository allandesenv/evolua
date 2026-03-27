DELETE FROM check_ins
WHERE user_id = 'seed-user'
  AND mood = 'seed-mood'
  AND reflection = 'seed-reflection'
  AND recommended_practice = 'seed-recommended_practice';

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'ansiosa', 'Acordei com a mente disparada, entao comecei o dia respirando antes de abrir as mensagens.', 2, 'Respiracao 4-6 por 3 minutos', TIMESTAMPTZ '2026-03-16T07:20:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-16T07:20:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'mais calma', 'Consegui fazer pausas pequenas entre reunioes e isso me deixou menos reativa.', 3, 'Pausa consciente entre blocos de foco', TIMESTAMPTZ '2026-03-17T18:10:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-17T18:10:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'cansada', 'O corpo pediu descanso cedo hoje. Percebi que preciso proteger melhor a noite.', 1, 'Desligar telas 30 minutos antes de dormir', TIMESTAMPTZ '2026-03-18T21:40:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-18T21:40:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'focada', 'Separei o trabalho em blocos menores e fiquei menos sobrecarregada.', 4, 'Planejar o proximo bloco em uma frase', TIMESTAMPTZ '2026-03-19T10:15:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-19T10:15:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'sensivel', 'Uma conversa mexeu comigo mais do que eu esperava, mas consegui nomear o que senti.', 2, 'Escrever tres linhas sobre o que ativou esse sentimento', TIMESTAMPTZ '2026-03-20T13:05:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-20T13:05:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'orgulhosa', 'Consegui dizer nao sem culpa e mantive minha agenda mais respiravel.', 4, 'Registrar um limite saudavel que funcionou', TIMESTAMPTZ '2026-03-21T17:50:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-21T17:50:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'insegura', 'Fiquei comparando meu ritmo com o de outras pessoas e isso drenou minha energia.', 2, 'Anotar uma evidencia concreta do proprio progresso', TIMESTAMPTZ '2026-03-22T08:25:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-22T08:25:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'estavel', 'O dia foi simples e isso ja foi uma boa noticia.', 3, 'Caminhada curta sem celular', TIMESTAMPTZ '2026-03-23T19:00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-23T19:00:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'motivada', 'Voltei para a trilha de foco e consegui encaixar duas entregas importantes.', 5, 'Fechar o dia com revisao do que avancou', TIMESTAMPTZ '2026-03-24T16:20:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-24T16:20:00Z'
);

INSERT INTO check_ins (user_id, mood, reflection, energy_level, recommended_practice, created_at)
SELECT 'clara-rocha', 'calma', 'Hoje senti menos pressa e consegui aproveitar melhor os espacos vazios do dia.', 4, 'Respirar fundo antes da proxima tarefa', TIMESTAMPTZ '2026-03-25T20:10:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM check_ins WHERE user_id = 'clara-rocha' AND created_at = TIMESTAMPTZ '2026-03-25T20:10:00Z'
);
