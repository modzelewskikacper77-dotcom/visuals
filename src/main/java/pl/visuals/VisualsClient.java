package pl.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

public class VisualsClient implements ClientModInitializer {
    public static KeyBinding openKey;

    public static AbstractClientPlayerEntity target;
    static long lastSeen;
    static final long HOLD_MS = 1000;

    public static AbstractClientPlayerEntity lastAttackedPlayer;
    public static long lastAttackTime;
    static LivingEntity lastAttackedLiving;
    static long lastAttackLivingTime;

    public static double speed;
    static boolean wasOnGround;

    @Override
    public void onInitializeClient() {
        Config.load();
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visuals.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.visuals"));

        ClientTickEvents.END_CLIENT_TICK.register(VisualsClient::tick);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient) onAttack(entity);
            return ActionResult.PASS;
        });
        HudRenderCallback.EVENT.register(Hud::render);
        WorldRenderEvents.AFTER_ENTITIES.register(WorldFx::render);
    }

    // ---------- tick ----------
    static void tick(MinecraftClient mc) {
        while (openKey.wasPressed()) mc.setScreen(new ClickGui());
        if (mc.player == null || mc.world == null) { target = null; return; }

        speed = Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ) * 20;

        if (Mods.TARGET_HUD.enabled || Mods.TARGET_RENDER.enabled) {
            AbstractClientPlayerEntity f = findTarget(mc, Mods.TH_RANGE.v);
            if (f != null) { target = f; lastSeen = System.currentTimeMillis(); }
        }

        boolean og = mc.player.isOnGround();
        if (Mods.JUMP_CIRCLES.enabled && wasOnGround && !og && mc.player.getVelocity().y > 0.1) {
            WorldFx.addCircle(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
        wasOnGround = og;

        if (Mods.KILL_EFFECT.enabled && lastAttackedLiving != null) {
            long now = System.currentTimeMillis();
            if (now - lastAttackLivingTime > 6000) {
                lastAttackedLiving = null;
            } else if (lastAttackedLiving.isDead()) {
                killEffect(mc, lastAttackedLiving);
                lastAttackedLiving = null;
            }
        }
    }

    static AbstractClientPlayerEntity findTarget(MinecraftClient mc, double range) {
        Entity cam = mc.getCameraEntity();
        if (cam == null) return null;
        Vec3d start = cam.getCameraPosVec(1f);
        Vec3d look = cam.getRotationVec(1f);
        Vec3d end = start.add(look.multiply(range));
        Box box = cam.getBoundingBox().stretch(look.multiply(range)).expand(1.0);
        EntityHitResult r = ProjectileUtil.raycast(cam, start, end, box,
                e -> !e.isSpectator() && e.canHit() && e instanceof AbstractClientPlayerEntity, range * range);
        return r == null ? null : (AbstractClientPlayerEntity) r.getEntity();
    }

    public static boolean targetActive() {
        return target != null && !target.isRemoved() && System.currentTimeMillis() - lastSeen < HOLD_MS;
    }

    /** Cel dla Target Render (zalezy od ustawienia Behavior). */
    public static Entity renderTarget() {
        if (Mods.TR_BEHAVIOR.is("Attack")) {
            if (lastAttackedPlayer != null && !lastAttackedPlayer.isRemoved()
                    && System.currentTimeMillis() - lastAttackTime < 5000) return lastAttackedPlayer;
            return null;
        }
        return targetActive() ? target : null;
    }

    // ---------- atak ----------
    static void onAttack(Entity e) {
        long now = System.currentTimeMillis();
        if (e instanceof AbstractClientPlayerEntity p) { lastAttackedPlayer = p; lastAttackTime = now; }
        if (e instanceof LivingEntity le) { lastAttackedLiving = le; lastAttackLivingTime = now; }
        if (Mods.HIT_SOUNDS.enabled) {
            SoundEvent s = switch (Mods.HS_TYPE.get()) {
                case "Levelup" -> SoundEvents.ENTITY_PLAYER_LEVELUP;
                case "Arrow" -> SoundEvents.ENTITY_ARROW_HIT_PLAYER;
                default -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            };
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(s, Mods.HS_PITCH.f(), Mods.HS_VOL.f()));
        }
    }

    static void killEffect(MinecraftClient mc, LivingEntity e) {
        ParticleEffect type = switch (Mods.KE_TYPE.get()) {
            case "Flame" -> ParticleTypes.FLAME;
            case "Soul" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "Heart" -> ParticleTypes.HEART;
            default -> ParticleTypes.TOTEM_OF_UNDYING;
        };
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < Mods.KE_COUNT.i(); i++) {
            mc.world.addParticle(type, e.getX(), e.getY() + e.getHeight() / 2, e.getZ(),
                    (r.nextDouble() - 0.5) * 0.6, r.nextDouble() * 0.5, (r.nextDouble() - 0.5) * 0.6);
        }
    }
}
