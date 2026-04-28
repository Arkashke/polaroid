package polaroid.client.modules.api;

import com.google.common.eventbus.Subscribe;
import polaroid.client.Polaroid;
import polaroid.client.events.EventKey;
import polaroid.client.modules.impl.combat.*;
import polaroid.client.modules.impl.misc.*;
import polaroid.client.modules.impl.movement.*;
import polaroid.client.modules.impl.player.*;
import polaroid.client.modules.impl.render.AspectRatio;
import polaroid.client.modules.impl.render.BetterMinecraft;
import polaroid.client.modules.impl.render.BlockOverlay;
import polaroid.client.modules.impl.render.ChinaHat;
import polaroid.client.modules.impl.render.ClickGui;
import polaroid.client.modules.impl.render.ClientSounds;
import polaroid.client.modules.impl.render.Crosshair;
import polaroid.client.modules.impl.render.CustomWorld;
import polaroid.client.modules.impl.render.DeathEffect;
import polaroid.client.modules.impl.render.ESP;
import polaroid.client.modules.impl.render.Fullbright;
import polaroid.client.modules.impl.render.GlassHand;
import polaroid.client.modules.impl.render.HandChams;
import polaroid.client.modules.impl.render.InterFace;
import polaroid.client.modules.impl.render.ItemPhysic;
import polaroid.client.modules.impl.render.JumpCircle;
import polaroid.client.modules.impl.render.NameTags;
import polaroid.client.modules.impl.render.NoHandShake;
import polaroid.client.modules.impl.render.NoRender;
import polaroid.client.modules.impl.render.Particles;
import polaroid.client.modules.impl.render.Pointers;
import polaroid.client.modules.impl.render.Predictions;
import polaroid.client.modules.impl.render.Scoreboard;
import polaroid.client.modules.impl.render.ScoreboardHealth;
import polaroid.client.modules.impl.render.SeeInvisibles;
import polaroid.client.modules.impl.render.Snow;
import polaroid.client.modules.impl.render.StorageESP;
import polaroid.client.modules.impl.render.SwingAnimation;
import polaroid.client.modules.impl.render.TargetESP;
import polaroid.client.modules.impl.render.Theme;
import polaroid.client.modules.impl.render.Tracers;
import polaroid.client.modules.impl.render.Trails;
import polaroid.client.modules.impl.render.ViewModel;
import polaroid.client.modules.impl.render.VisualHitbox;
import polaroid.client.modules.impl.render.XrayBypass;
import polaroid.client.utils.render.font.Font;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ModuleRegistry {
    private final List<Module> functions = new CopyOnWriteArrayList<>();

    private SwingAnimation swingAnimation;
    private InterFace InterFace;
    private AutoGapple autoGapple;
    private AutoSprint autoSprint;
    private NoRender noRender;
    private ItemSwapFix itemswapfix;
    private AutoPotion autopotion;
    private TriggerBot triggerbot;
    private NoJumpDelay nojumpdelay;
    private ClickFriend clickfriend;
    private InventoryMove inventoryMove;
    private VisualHitbox visualHitbox;
    private AutoTransfer autoTransfer;
    private ItemCooldown itemCooldown;
    private ClickPearl clickPearl;
    private AutoSwap autoSwap;
    private Hitbox hitbox;
    private AntiPush antiPush;
    private FreeCam freeCam;
    private ChestStealer chestStealer;
    private AutoLeave autoLeave;
    private AutoAccept autoAccept;
    private AutoRespawn autoRespawn;
    private ClientSounds clientSounds;
    private AutoTotem autoTotem;
    private NoSlow noSlow;
    private Pointers pointers;
    private AutoExplosion autoExplosion;
    private NoRotate noRotate;
    private Aura aura;
    private AntiBot antiBot;
    private Trails trails;
    private Crosshair crosshair;
    private ViewModel viewModel;
    private ChinaHat chinaHat;
    private Snow snow;
    private Particles particles;
    private TargetESP targetESP;
    private JumpCircle jumpCircle;
    private ItemPhysic itemPhysic;
    private Predictions predictions;
    private NoEntityTrace noEntityTrace;
    private ItemScroller itemScroller;
    private StorageESP storageESP;
    private NameProtect nameProtect;
    private NoInteract noInteract;
    private GlassHand glassHand;
    private HandChams handChams;
    private BlockOverlay blockOverlay;
    private AntiAFK antiAFK;
    private BetterMinecraft betterMinecraft;
    private SeeInvisibles seeInvisibles;
    private Theme theme;
    private SelfDestruct selfDestruct;
    private AutoTool autoTool;
    private ClickGui clickGui;
    private CustomModels customModels;
    private WaterSpeed waterSpeed;
    private GriefHelper griefHelper;
    private FastEXP fastEXP;
    private AuctionHelper auctionHelper;
    private AutoDuel autoDuel;
    private AutoPilot autoPilot;
    private AirStuck airStuck;
    private AspectRatio aspectRatio;
    private AntiAim antiAim;
    private AutoDodge autoDodge;
    private Strafe strafe;
    private Speed speed;
    private Spider spider;
    private CustomWorld customWorld;
    private NoFriendDamage noFriendDamage;
    private ElytraTarget elytraTarget;
    private NoClip noClip;
    private ClanUpgrade clanUpgrade;
    private ESP esp;
    
    // Новые модули
    private VelocitySync velocitySync;
    private NoPlayerInt noPlayerInt;
    private BackTrack backTrack;
    private DragonFly dragonFly;
    private ElytraPredict elytraPredict;
    private NoWeb noWeb;
    private ElytraBooster elytraBooster;
    private ElytraJump elytraJump;
    private ElytraMotion elytraMotion;
    private NoFall noFall;
    private CatFly catFly;
    private FastBreak fastBreak;
    private AutoContract autoContract;
    private Tape tape;
    private Mouse mouse;
    private Scoreboard scoreboard;
    private ScoreboardHealth scoreboardHealth;
    private NoHandShake noHandShake;
    private UseTracker useTracker;
    private Timer timer;
    private ElytraHelper elytraHelper;
    private NameTags nameTags;
    private Fullbright fullbright;
    private TargetStrafe targetStrafe;
    private SexAura sexAura;
    private RussianRoulette russianRoulette;
    private Criticals criticals;
    private ShiftTap shiftTap;

    public void init() {
        registerAll(InterFace = new InterFace(),
                autoGapple = new AutoGapple(),
                autoSprint = new AutoSprint(),
                noRender = new NoRender(),
                seeInvisibles = new SeeInvisibles(),
                itemswapfix = new ItemSwapFix(),
                autopotion = new AutoPotion(),
                triggerbot = new TriggerBot(),
                nojumpdelay = new NoJumpDelay(),
                clickfriend = new ClickFriend(),
                inventoryMove = new InventoryMove(),
                visualHitbox = new VisualHitbox(),
                hitbox = new Hitbox(),
                antiPush = new AntiPush(),
                freeCam = new FreeCam(),
                chestStealer = new ChestStealer(),
                autoLeave = new AutoLeave(),
                autoAccept = new AutoAccept(),
                autoRespawn = new AutoRespawn(),
                clientSounds = new ClientSounds(),
                noSlow = new NoSlow(),
                pointers = new Pointers(),
                autoExplosion = new AutoExplosion(),
                antiBot = new AntiBot(),
                trails = new Trails(),
                crosshair = new Crosshair(),
                autoTotem = new AutoTotem(),
                aura = new Aura(autopotion),
                clickPearl = new ClickPearl(),
                autoSwap = new AutoSwap(),
                swingAnimation = new SwingAnimation(aura),
                targetESP = new TargetESP(aura),
                viewModel = new ViewModel(),
                chinaHat = new ChinaHat(),
                snow = new Snow(),
                particles = new Particles(),
                jumpCircle = new JumpCircle(),
                itemPhysic = new ItemPhysic(),
                predictions = new Predictions(),
                noEntityTrace = new NoEntityTrace(),
                itemScroller = new ItemScroller(),
                storageESP = new StorageESP(),
                nameProtect = new NameProtect(),
                noInteract = new NoInteract(),
                glassHand = new GlassHand(),
                handChams = new HandChams(),
                blockOverlay = new BlockOverlay(),
                antiAFK = new AntiAFK(),
                betterMinecraft = new BetterMinecraft(),
                theme = new Theme(),
                noRotate = new NoRotate(),
                itemCooldown = new ItemCooldown(),
                selfDestruct = new SelfDestruct(),
                clickGui = new ClickGui(),
                customModels = new CustomModels(),
                waterSpeed = new WaterSpeed(),
                griefHelper = new GriefHelper(),
                fastEXP = new FastEXP(),
                auctionHelper = new AuctionHelper(),
                autoDuel = new AutoDuel(),
                autoTool = new AutoTool(),
                autoPilot = new AutoPilot(),
                airStuck = new AirStuck(),
                aspectRatio = new AspectRatio(),
                antiAim = new AntiAim(),
                autoDodge = new AutoDodge(),
                strafe = new Strafe(),
                speed = new Speed(),
                spider = new Spider(),
                customWorld = new CustomWorld(),
                noFriendDamage = new NoFriendDamage(),
                elytraTarget = new ElytraTarget(),
                noClip = new NoClip(),
                clanUpgrade = new ClanUpgrade(),
                esp = new ESP(),
                
                // Новые модули
                velocitySync = new VelocitySync(),
                noPlayerInt = new NoPlayerInt(),
                backTrack = new BackTrack(),
                dragonFly = new DragonFly(),
                elytraPredict = new ElytraPredict(),
                noWeb = new NoWeb(),
                elytraBooster = new ElytraBooster(),
                elytraJump = new ElytraJump(),
                elytraMotion = new ElytraMotion(),
                noFall = new NoFall(),
                catFly = new CatFly(),
                fastBreak = new FastBreak(),
                autoContract = new AutoContract(),
                tape = new Tape(),
                mouse = new Mouse(),
                scoreboard = new Scoreboard(),
                scoreboardHealth = new ScoreboardHealth(),
                noHandShake = new NoHandShake(),
                useTracker = new UseTracker(),
                timer = new Timer(),
                elytraHelper = new ElytraHelper(),
                nameTags = new NameTags(),
                fullbright = new Fullbright(),
                targetStrafe = new TargetStrafe(),
                sexAura = new SexAura(),
                russianRoulette = new RussianRoulette(),
                criticals = new Criticals(),
                shiftTap = new ShiftTap()
        );

        Polaroid.getInstance().getEventBus().register(this);
    }

    private void registerAll(Module... Functions) {
        Arrays.sort(Functions, Comparator.comparing(Module::getName));

        functions.addAll(List.of(Functions));
    }

    public List<Module> getSorted(Font font, float size) {
        return functions.stream().sorted((f1, f2) -> Float.compare(font.getWidth(f2.getName(), size), font.getWidth(f1.getName(), size))).toList();
    }

    @Subscribe
    private void onKey(EventKey e) {
        for (Module Function : functions) {
            if (Function.getBind() == e.getKey()) {
                Function.toggle();
            }
        }
    }
}


