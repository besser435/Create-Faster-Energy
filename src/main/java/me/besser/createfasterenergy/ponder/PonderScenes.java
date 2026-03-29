package me.besser.createfasterenergy.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import me.besser.createfasterenergy.util.FEConfig;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class PonderScenes {
    public static void alternator(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);

        scene.title("alternator", "Generating FE from Rotational Force");
        scene.configureBasePlate(0, 0, 6);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.showBasePlate();
        scene.idle(10);


        // Blocks
        BlockPos shaftPos = util.grid().at(4, 1, 2);
        BlockPos speedometerPos = util.grid().at(3, 1, 2);
        BlockPos alternatorPos = util.grid().at(2, 1, 2);
        BlockPos feTargetBlockPos = util.grid().at(2, 1, 3);
        BlockPos largeCogPos = util.grid().at(5, 1, 2);

        Selection mainKineticNetwork = util.select().everywhere();
        Selection cogKineticNetwork = util.select().position(util.grid().at(5, 0, 3));
        scene.world().setKineticSpeed(mainKineticNetwork, 32);
        scene.world().setKineticSpeed(cogKineticNetwork, -32);

        // Build scene // TODO make less stupid, lots of duped code
        scene.world().showSection(util.select().position(largeCogPos), Direction.DOWN);
        scene.idle(2);

        scene.world().showSection(util.select().position(shaftPos), Direction.DOWN);
        scene.idle(2);

        scene.world().showSection(util.select().position(speedometerPos), Direction.DOWN);
        scene.idle(2);

        scene.world().showSection(util.select().position(alternatorPos), Direction.DOWN);
        scene.idle(2);


        // TODO: have better lang stuff. Text is repeated here and in the lang file.
        // When its not in the lang file and its just here, it just shows the translation key in game for some reason.


        // Explain alternator purpose
        scene.idle(20);
        scene.effects().rotationDirectionIndicator(alternatorPos);
        scene.overlay().showText(65)
                .text("The Alternator converts rotational force into FE.")
                .placeNearTarget()
                .pointAt(util.vector().topOf(alternatorPos));
        scene.idle(70);


        // Explain FE target
        scene.rotateCameraY(-90);
        scene.idle(10);

        scene.world().showSection(util.select().position(feTargetBlockPos), Direction.DOWN);
        scene.idle(20);

        scene.addKeyframe();
        scene.overlay().showText(65)
                .text("It will give FE to any blocks placed next to it.")
                .placeNearTarget()
                .pointAt(util.vector().topOf(feTargetBlockPos));
        scene.idle(70);

        scene.idle(10);
        scene.rotateCameraY(90);
        scene.world().hideSection(util.select().position(feTargetBlockPos), Direction.UP);
        scene.idle(20);


        // Explain minimum RPM
        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("Its input speed must meet the minimum RPM for it to start generating FE.")
                .placeNearTarget()
                .pointAt(util.vector().topOf(speedometerPos));
        scene.effects().rotationSpeedIndicator(speedometerPos);
        scene.idle(105);


        // Explain efficiency
        scene.world().setKineticSpeed(mainKineticNetwork, 128);
        scene.world().setKineticSpeed(cogKineticNetwork, -128);
        scene.idle(10);

        scene.addKeyframe();
        scene.overlay().showText(90)
                .text("It becomes less efficient when the input RPM is fast, but generates more FE.")
                .placeNearTarget()
                .pointAt(util.vector().topOf(alternatorPos));
        scene.effects().rotationSpeedIndicator(alternatorPos);
        scene.idle(95);
        scene.markAsFinished();
    }
}
