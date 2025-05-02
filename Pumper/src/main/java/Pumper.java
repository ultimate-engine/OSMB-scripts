
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.scene.RSObject;
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;
import com.osmb.api.shape.Polygon;
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.timing.Timer;

@ScriptDefinition(
        name = "Pumper",
        author = "Ultimate",
        version = 1.0,
        description = "Blast Furnace pump operator",
        skillCategory = SkillCategory.COMBAT
)
public class Pumper extends Script {
    private static final int BF_REGION_ID = 7757;
    private static final double POLY_SCALE = 0.75;
    private static final double PIXEL_THRESHOLD = 0.25;
    private static final int ANIM_MIN = 3200;
    private static final int ANIM_MAX = 5200;
    private final Timer animatingTimer = new Timer();
    private int animationTimeout = random(ANIM_MIN, ANIM_MAX);
    public Pumper(Object core) { super(core); }

    @Override
    public int poll() {
        WorldPosition pos = getWorldPosition();
        if (pos == null || pos.getRegionID() != BF_REGION_ID) {
            return 1000;
        }

        if (!isAnimationIdle()) {
            return 200; // still pumping
        }

        RSObject pump = getObjectManager().getClosestObject("Pump");
        if (pump != null && pump.isInteractableOnScreen() && pump.interact("Operate")) {
            animatingTimer.reset();
            animationTimeout = random(ANIM_MIN, ANIM_MAX);
            return RandomUtils.uniformRandom(5000, 8000);
        }
        return 300;
    }
    private boolean isAnimationIdle() {
        return submitHumanTask(() -> {
            WorldPosition myPos = getWorldPosition();
            if (myPos == null) return false;

            Polygon poly = getSceneProjector().getTileCube(myPos, 100);
            if (poly == null) return false;

            // Detect movement in the player cube.
            if (getPixelAnalyzer().isAnimating(PIXEL_THRESHOLD, poly.getResized(POLY_SCALE))) {
                animatingTimer.reset();         // refresh idle timer
                return false;                   // still animating
            }

            return animatingTimer.timeElapsed() > animationTimeout;
        }, 0); // evaluate instantly each poll
    }

    @Override
    public int[] regionsToPrioritise() { return new int[]{ BF_REGION_ID }; }
}
