<script lang="ts">
    interface Blade {
        left: number;
        height: number;
        width: number;
        delay: number;
        dur: number;
        tilt: number;
        color: string;
        layer: number;
    }

    interface Firefly {
        left: number;
        top: number;
        delay: number;
        dur: number;
        size: number;
        drift: number;
        phase: number;
    }

    const rand = (seed: number) => {
        const x = Math.sin(seed * 999.7) * 43758.5453;
        return x - Math.floor(x);
    };

    let blades: Blade[] = [];
    for (let i = 0; i < 96; i++) {
        const seed = i * 1.71 + 3;
        const layer = i % 3;
        const base =
            layer === 0 ? 26 : layer === 1 ? 42 : 58;
        const spread =
            layer === 0 ? 20 : layer === 1 ? 22 : 34;
        const lightness = 22 + layer * 7 + rand(seed) * 10;
        blades.push({
            left: rand(seed * 1.1) * 100,
            height: base + rand(seed) * spread,
            width: 4 + rand(seed * 2.3) * 6,
            delay: rand(seed * 3.7) * -7,
            dur: 2.4 + rand(seed * 5.1) * 3.6,
            tilt: (rand(seed * 7.3) - 0.5) * 38,
            color: `hsl(135 42% ${lightness}%)`,
            layer,
        });
    }

    let fireflies: Firefly[] = [];
    for (let i = 0; i < 14; i++) {
        const seed = i * 13.7 + 5;
        fireflies.push({
            left: rand(seed * 1.3) * 100,
            top: 52 + rand(seed * 2.1) * 46,
            delay: rand(seed * 3.9) * -9,
            dur: 6 + rand(seed) * 7,
            size: 2 + rand(seed * 5.7) * 3,
            drift: (rand(seed * 9.1) - 0.5) * 90,
            phase: rand(seed * 11.3) * 4,
        });
    }
</script>

<div class="grass-plains" aria-hidden="true">
    <div class="sky"></div>

    <div class="horizon-glow"></div>
    <div class="lantern-sun"></div>

    <div class="hill hill-1"></div>
    <div class="hill hill-2"></div>
    <div class="hill hill-3"></div>

    <div class="ground"></div>

    <div class="blades">
        {#each blades as blade, i}
            <span
                class="blade layer-{blade.layer}"
                style="
                    left:{blade.left}%;
                    height:{blade.height}px;
                    width:{blade.width}px;
                    --tilt:{blade.tilt}deg;
                    animation-duration:{blade.dur}s;
                    animation-delay:{blade.delay}s;
                    background:{blade.color};
                "
            ></span>
        {/each}
    </div>

    <div class="fireflies">
        {#each fireflies as f, i}
            <span
                class="firefly"
                style="
                    left:{f.left}%;
                    top:{f.top}%;
                    --size:{f.size}px;
                    --drift:{f.drift}px;
                    --phase:{f.phase}s;
                    animation-duration:{f.dur}s;
                    animation-delay:{f.delay}s;
                "
            ></span>
        {/each}
    </div>
</div>

<style>
    .grass-plains {
        position: absolute;
        inset: 0;
        overflow: hidden;
        z-index: 0;
        pointer-events: none;
    }

    /* --- Dusk Sky --- */
    .sky {
        position: absolute;
        inset: 0;
        background: linear-gradient(
            180deg,
            #0b1026 0%,
            #12294a 28%,
            #16404a 50%,
            #3d3a2c 66%,
            #7a4a1e 74%,
            #1d3a24 78%,
            #0e2a1a 100%
        );
    }

    /* --- Warm lantern horizon --- */
    .horizon-glow {
        position: absolute;
        left: 50%;
        top: 60%;
        transform: translate(-50%, -50%);
        width: 140vw;
        height: 55vh;
        background: radial-gradient(
            ellipse at center,
            rgba(255, 190, 90, 0.5) 0%,
            rgba(255, 150, 60, 0.22) 35%,
            transparent 70%
        );
        filter: blur(18px);
    }

    .lantern-sun {
        position: absolute;
        left: 50%;
        top: 62%;
        transform: translate(-50%, -50%);
        width: 150px;
        height: 150px;
        border-radius: 50%;
        background: radial-gradient(
            circle,
            #fff4cf 0%,
            #ffd88a 38%,
            #ffb84d 60%,
            transparent 74%
        );
        opacity: 0.92;
        box-shadow: 0 0 80px 30px rgba(255, 184, 77, 0.35);
    }

    /* --- Rolling hills --- */
    .hill {
        position: absolute;
        left: -10%;
        width: 120%;
        border-radius: 100% 100% 0 0;
    }
    .hill-1 {
        height: 30%;
        bottom: 2%;
        background: linear-gradient(180deg, #17392a 0%, #12301f 100%);
        opacity: 0.9;
    }
    .hill-2 {
        height: 20%;
        bottom: 14%;
        background: linear-gradient(180deg, #122d20 0%, #0e281a 100%);
        opacity: 0.85;
    }
    .hill-3 {
        height: 13%;
        bottom: 26%;
        background: linear-gradient(180deg, #0e2519 0%, #0b1f15 100%);
        opacity: 0.8;
    }

    /* --- Ground bed the blades grow from --- */
    .ground {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 16vh;
        background: linear-gradient(
            180deg,
            #0d2416 0%,
            #0a1c12 60%,
            #081710 100%
        );
    }

    /* --- Waving grass blades --- */
    .blades {
        position: absolute;
        inset: 0;
    }

    .blade {
        position: absolute;
        bottom: 0;
        transform-origin: bottom center;
        border-radius: 50% 50% 0 0 / 100% 100% 0 0;
        animation: sway 3s ease-in-out infinite alternate;
        will-change: transform;
    }

    .blade.layer-0 {
        opacity: 0.8;
    }
    .blade.layer-1 {
        opacity: 0.95;
    }
    .blade.layer-2 {
        opacity: 1;
    }

    @keyframes sway {
        from {
            transform: rotate(calc(var(--tilt) - 5deg)) skewX(-4deg);
        }
        to {
            transform: rotate(calc(var(--tilt) + 5deg)) skewX(4deg);
        }
    }

    /* --- Fireflies --- */
    .fireflies {
        position: absolute;
        inset: 0;
    }

    .firefly {
        position: absolute;
        width: var(--size);
        height: var(--size);
        border-radius: 50%;
        background: radial-gradient(
            circle,
            #fffbe0 0%,
            #ffd166 55%,
            transparent 100%
        );
        box-shadow: 0 0 8px 2px rgba(255, 209, 102, 0.55);
        opacity: 0;
        animation:
            drift 7s ease-in-out infinite alternate,
            flicker 2.6s ease-in-out var(--phase) infinite;
    }

    @keyframes drift {
        0% {
            transform: translate(0, 0);
            opacity: 0;
        }
        18% {
            opacity: 0.9;
        }
        82% {
            opacity: 0.55;
        }
        100% {
            transform: translate(var(--drift), -46px);
            opacity: 0;
        }
    }

    @keyframes flicker {
        0%, 100% {
            filter: brightness(1);
        }
        45% {
            filter: brightness(1.7);
        }
        70% {
            filter: brightness(0.85);
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .blade {
            animation: none;
        }
        .firefly {
            animation: none;
            opacity: 0;
        }
    }
</style>
