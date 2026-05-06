INSERT INTO trails (
    user_id,
    title,
    description,
    summary,
    content,
    category,
    premium,
    private_trail,
    active_journey,
    generated_by_ai,
    journey_key,
    source_style,
    media_links,
    created_at
)
SELECT
    'clara-rocha',
    'Mentoria para destravar a jornada',
    'Conteudo premium de mentoria para transformar um bloqueio atual em um proximo passo praticavel.',
    'Uma trilha de mentoria para clarear bloqueios, adaptar a jornada e seguir com menos peso.',
    E'# Mentoria para destravar a jornada\n\nUse esta trilha quando voce sentir que sabe onde quer chegar, mas precisa de uma ponte mais simples para continuar.\n\n## Passos\n- nomeie o bloqueio atual sem julgamento\n- escolha uma acao de ate 10 minutos\n- registre o que ficou mais claro depois da pratica',
    'mentoria',
    true,
    false,
    false,
    false,
    null,
    'mentor_exclusive',
    '[]',
    TIMESTAMPTZ '2026-05-06T12:00:00Z'
WHERE NOT EXISTS (
    SELECT 1 FROM trails WHERE title = 'Mentoria para destravar a jornada'
);
