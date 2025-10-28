Morfogeneza - Neon Birth & Mature (Guide)

Scope
- Spec algorytmow wizualnych dla etapow: Narodziny (kropkapierscien) i Dojrzewanie (wypelnianie gradientem energii) w stylu bioluminescencyjnym.
- Implementacja w Compose Canvas (app) + parzystosc overlay.

Tokens (source of truth)
- animation.birth-ms, animation.mature-ms
- anim-params.birth-threshold (domyslnie 0.8)
- anim-params.ring-width-dp (domyslnie 3)
- anim-params.halo-exponent (domyslnie 2.2)
- glow.intensity, glow.blur-dp, glow.radius-dp
- palette.(bg-*, text-*, accent-cyan)

Birth (kropkapierscien)
- Param g  [0,1].
- jesli g < birth-threshold: rFill = g * R; drawCircle(fill=accent, alpha1)
- inaczej: t = remap(g, threshold..1  0..1); rRing = lerp(R*threshold, R, t)
  drawCircle(style=Stroke(width=ring-width), color=accent, alpha=0.850.45)
- Halo: radial falloff H(r) = clamp(1.2  (r/rBase)^haloExponent, 0, 1)
  render jako rozmyty overlay (blur glow.blur-dp) kolorem accent (alpha = glow.intensity)

Mature (gradient fill)
- rFill animowany 0(R  ring-width)
- Energia: E = mix(radialFalloff, noise) - w v1 uzyj radialFalloff (bez noise),
  kolor = accent z alpha rosnaca do 1.0
- Pierscien: staly Stroke na R (width=ring-width)
- Halo: jak w Birth

Background
- Radialny gradient od palette.bg-elevated (centrum) do bg-primary (krawedz) ze wzmocnieniem anim-params.bg-radial-strength.

Compose hints (pseudokod)
- val R = cellRadius
- val g by animateFloatAsState(gTarget, tween(birth-ms))
- if (g<th) fill; else Stroke
- Halo: narysuj wieksze kolo o promieniu R*1.0 w osobnym pass (blur)

Acceptance (Kai)
- 60 fps (Pixel 5) lub noty Nodus; brak aliasingu pierscienia.
- Parzystosc app/overlay (kolory/parametry).
- Birth: plynne przejscie kropkapierscien bez skokow promienia.
- Mature: wypelnienie dochodzi pod pierscien, nie przecieka".

Next
- v2: tekstura plasma/noise" (ShaderBrush/Bitmap) kalibrowana z tokenem seed/scale.
