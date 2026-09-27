package dev.liftmarker.compat;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the diagram mixins and client classes need from Simulated
 */
public class SimulatedCompatibility {

    private static final String DIAGRAM = "dev/simulated_team/simulated/content/entities/diagram/";
    private static final String DIAGRAM_SCREEN = DIAGRAM + "screen/DiagramScreen";
    private static final String DIAGRAM_BUTTON = DIAGRAM + "screen/DiagramButton";
    private static final String DIAGRAM_STICKY_NOTE = DIAGRAM + "screen/DiagramStickyNote";
    private static final String SIM_GUI_TEXTURES = "dev/simulated_team/simulated/index/SimGUITextures";
    private static final String SIM_LANG = "dev/simulated_team/simulated/data/SimLang";

    private static final String VECTOR3D = "Lorg/joml/Vector3d;";
    private static final String MATRIX4F = "Lorg/joml/Matrix4f;";
    private static final String QUATERNIONF = "Lorg/joml/Quaternionf;";
    private static final String COLOR = "Lnet/createmod/catnip/theme/Color;";
    private static final String GUI_GRAPHICS = "Lnet/minecraft/client/gui/GuiGraphics;";
    private static final String TEXTURE = "L" + SIM_GUI_TEXTURES + ";";

    public interface ClassSource {
        ClassNode load(String internalName) throws Exception;
    }

    /**
     * @param source reads a class by its internal name
     * @return what is missing, or an empty list if this Simulated version is compatible
     */
    public static List<String> findMissing(final ClassSource source) {
        final List<String> missing = new ArrayList<>();

        final ClassNode screen = load(source, DIAGRAM_SCREEN, missing);
        if (screen != null) {
            requireField(screen, "LOCAL_CAMERA_POSITION", VECTOR3D, missing);
            requireField(screen, "PROJECTION_MAT", MATRIX4F, missing);
            requireField(screen, "TOOLTIP_LABEL_COLOR", "I", missing);
            requireField(screen, "diagram", "L" + DIAGRAM + "DiagramEntity;", missing);
            requireField(screen, "LOCAL_ORIENTATION", QUATERNIONF, missing);
            requireField(screen, "DIAGRAM_TEXTURE", TEXTURE, missing);
            requireField(screen, "UPDATE_REQUEST_INTERVAL", "I", missing);
            requireField(screen, "BUTTON_COLOR", COLOR, missing);
            requireField(screen, "DULL_BUTTON_COLOR", COLOR, missing);
            requireMethod(screen, "init", "()V", missing);
            requireMethod(screen, "tick", "()V", missing);
            requireMethod(screen, "getScreenCoords", "(Lorg/joml/Vector3d;Lorg/joml/Quaternionfc;Lorg/joml/Vector3dc;Lorg/joml/Matrix4fc;II)Lorg/joml/Vector2d;", missing);
            requireMethod(screen, "renderTooltip", "(" + GUI_GRAPHICS + "IILjava/util/List;)V", missing);

            // the injection points themselves: the greeble keep-out corner and the last pop of the paper transform
            final MethodNode addGreebles = requireMethod(screen, "addGreebles", "(II)V", missing);
            if (addGreebles != null && countConstant(addGreebles, 66.0) != 1) {
                missing.add("DiagramScreen.addGreebles button region");
            }

            final MethodNode renderWindow = requireMethod(screen, "renderWindow", "(" + GUI_GRAPHICS + "IIF)V", missing);
            if (renderWindow != null && countCalls(renderWindow, "popPose") != 3) {
                missing.add("DiagramScreen.renderWindow pose layout");
            }
        }

        final ClassNode button = load(source, DIAGRAM_BUTTON, missing);
        if (button != null) {
            requireMethod(button, "<init>", "(" + TEXTURE + "IILnet/minecraft/network/chat/Component;Ljava/lang/Runnable;)V", missing);
            requireMethod(button, "getTexture", "()" + TEXTURE, missing);
        }

        final ClassNode textures = load(source, SIM_GUI_TEXTURES, missing);
        if (textures != null) {
            requireField(textures, "DIAGRAM_ICON_MASS", TEXTURE, missing);
            requireField(textures, "DIAGRAM_ICON_COM_TOGGLE", TEXTURE, missing);
            requireField(textures, "width", "I", missing);
            requireField(textures, "height", "I", missing);
        }

        final ClassNode lang = load(source, SIM_LANG, missing);
        if (lang != null) {
            requireMethod(lang, "translate", "(Ljava/lang/String;[Ljava/lang/Object;)Lnet/createmod/catnip/lang/LangBuilder;", missing);
        }

        final ClassNode note = load(source, DIAGRAM_STICKY_NOTE, missing);
        if (note != null) {
            requireField(note, "SUBLEVEL_RENDER_WIDTH_PIXELS", "I", missing);
            requireField(note, "SUBLEVEL_RENDER_HEIGHT_PIXELS", "I", missing);
            requireField(note, "NOTE_LOCAL_CAM_POS", VECTOR3D, missing);
            requireField(note, "NOTE_PROJ_MAT", MATRIX4F, missing);
            requireField(note, "NOTE_ORIENTATION", QUATERNIONF, missing);
            requireField(note, "parent", "L" + DIAGRAM_SCREEN + ";", missing);
            requireMethod(note, "renderCustomCOM", "(" + GUI_GRAPHICS + "Lcom/mojang/blaze3d/vertex/PoseStack;)V", missing);
        }

        return missing;
    }

    private static ClassNode load(final ClassSource source, final String name, final List<String> missing) {
        try {
            return source.load(name);
        } catch (final Exception e) {
            missing.add(name.substring(name.lastIndexOf('/') + 1));
            return null;
        }
    }

    private static void requireField(final ClassNode owner, final String name, final String desc, final List<String> missing) {
        if (owner.fields.stream().noneMatch(field -> field.name.equals(name) && field.desc.equals(desc))) {
            missing.add(simpleName(owner) + "." + name);
        }
    }

    private static MethodNode requireMethod(final ClassNode owner, final String name, final String desc, final List<String> missing) {
        for (final MethodNode method : owner.methods) {
            if (method.name.equals(name) && method.desc.equals(desc)) {
                return method;
            }
        }

        missing.add(simpleName(owner) + "." + name);
        return null;
    }

    private static int countConstant(final MethodNode method, final double value) {
        int count = 0;
        for (final AbstractInsnNode insn : method.instructions) {
            if (insn instanceof final LdcInsnNode ldc && ldc.cst instanceof final Double constant && constant == value) {
                count++;
            }
        }
        return count;
    }

    private static int countCalls(final MethodNode method, final String name) {
        int count = 0;
        for (final AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && insn instanceof final MethodInsnNode call && call.name.equals(name)) {
                count++;
            }
        }
        return count;
    }

    private static String simpleName(final ClassNode node) {
        return node.name.substring(node.name.lastIndexOf('/') + 1);
    }
}
