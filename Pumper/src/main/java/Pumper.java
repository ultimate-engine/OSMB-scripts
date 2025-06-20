import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.timing.Timer;
@ScriptDefinition(
        name        = "Pumper",
        author      = "Ultimate",
        version     = 1.2,
        description = "Blast Furnace pump operator",
        skillCategory = SkillCategory.COMBAT)
public class Pumper extends Script {

    private static final int BF_REGION_ID        = 7757;
    private static final double POLY_SCALE       = 0.75;
    private static final double PIXEL_THRESHOLD  = 0.40;
    private static final int ANIM_MIN            = 8000;
    private static final int ANIM_MAX            = 10000;
    private static final int CLICK_IDLE_MIN      = 5_000;
    private static final int CLICK_IDLE_MAX      = 8_000;

    private final Timer idleTimer = new Timer();
    private int idleTimeout = random(ANIM_MIN, ANIM_MAX);

    public Pumper(Object core) { super(core); }

    @Override
    public int poll() {
        WorldPosition pos = getWorldPosition();
        if (pos == null || pos.getRegionID() != BF_REGION_ID) {
            return 1000;
        }

        if (!isAnimationIdle()) {
            return 0;
        }
        RSObject pump = getObjectManager().getClosestObject("Pump");
        if (pump != null && pump.isInteractableOnScreen() && pump.interact("Operate")) {
            idleTimer.reset();
            idleTimeout = random(ANIM_MIN, ANIM_MAX);

            submitHumanTask(() -> false, RandomUtils.uniformRandom(CLICK_IDLE_MIN, CLICK_IDLE_MAX));
        }
        return 0;
    }

    private boolean isAnimationIdle() {
        return submitHumanTask(() -> {
            WorldPosition me = getWorldPosition();
            if (me == null) return false;

            Polygon cube = getSceneProjector().getTileCube(me, 100);
            if (cube == null) return false;

            // If any movement detected – reset timer
            if (getPixelAnalyzer().isAnimating(PIXEL_THRESHOLD, cube.getResized(POLY_SCALE))) {
                idleTimer.reset();
                return false;
            }
            return idleTimer.timeElapsed() > idleTimeout;
        }, Integer.MAX_VALUE);
    }

    @Override
    public int[] regionsToPrioritise() { return new int[]{ BF_REGION_ID }; }
}