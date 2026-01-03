package mchorse.bbs_mod.forms.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import mchorse.bbs_mod.cubic.ModelInstance;
import mchorse.bbs_mod.cubic.data.model.Model;
import mchorse.bbs_mod.cubic.data.model.ModelGroup;
import mchorse.bbs_mod.cubic.render.ICubicRenderer;
import mchorse.bbs_mod.cubic.render.vao.ModelVAO;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.ShaderManager;
import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.forms.entities.IEntity;
import mchorse.bbs_mod.forms.forms.*;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import mchorse.bbs_mod.utils.MatrixStackUtils;
import mchorse.bbs_mod.utils.pose.Transform;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.Map;

public class GBufferShaderFormRenderer extends FormRenderer<GBufferShaderForm> {
    public static final Link ICON_TEXTURE = Link.assets("textures/gbuffer_shader.png");



    public GBufferShaderFormRenderer(GBufferShaderForm form) {
        super(form);
    }

    @Override
    protected void renderInUI(UIContext context, int x1, int y1, int x2, int y2) {
        Texture texture = context.render.getTextures().getTexture(ICON_TEXTURE);

        int w = texture.width;
        int h = texture.height;
        int x = (x1 + x2) / 2;
        int y = (y1 + y2) / 2;

        context.batcher.fullTexturedBox(texture, x - w / 2f, y - h / 2f, w, h);
    }

    protected void render3D(FormRenderingContext context) {
        ShaderManager.register(this.form, this.form.renderStage.get());
    }

    @Override
    protected void renderBodyPart(BodyPart part, FormRenderingContext context)
    {
        IEntity oldEntity = context.entity;

        context.entity = part.useTarget.get() ? oldEntity : part.getEntity();

        Form childrenForm  = part.getForm();
        if (childrenForm != null)
        {
            context.stack.push();
            MatrixStackUtils.applyTransform(context.stack, part.transform.get());

            // Register for custom rendering
            if (childrenForm instanceof ModelForm modelForm) {
                ModelInstance modelInstance = BBSModClient.getModels().getModel(modelForm.model.get());
                if  (modelInstance != null && modelInstance.getModel() instanceof Model model) {
                    Map<ModelGroup, ModelVAO> vaos = modelInstance.getVaos();

                    // Track the model's texture
                    Texture texture = null;
                    Link textureLink = modelForm.texture.get();
                    if (textureLink == null) {
                        textureLink = modelInstance.texture;
                    }
                    if (textureLink != null) {
                        texture = BBSModClient.getTextures().getTexture(textureLink);
                    }

                    if (texture != null) {
                        for (ModelGroup topGroup : model.topGroups) {
                            registerVAORecursive(context.stack, vaos, topGroup, RenderSystem.getProjectionMatrix(), context, texture);
                        }
                    }
                }
            } else if (childrenForm instanceof GLVertexForm vertexForm) {
                Link textureLink = vertexForm.texture.get();
                Texture texture = null;
                if (textureLink != null) {
                    texture = BBSModClient.getTextures().getTexture(textureLink);
                }
                if  (texture != null) {
                    ShaderManager.addGLVertex(this.form, vertexForm, RenderSystem.getProjectionMatrix().get(new Matrix4f()).mul(context.stack.peek().getPositionMatrix()).scale(-1, 1, -1), texture, context.light, context.overlay);
                }
            }

            if (this.form.renderChildren.get()) {
                // Regular rendering as usual
                FormUtilsClient.render(childrenForm, context);
            }

            context.stack.pop();
        }

        context.entity = oldEntity;
    }

    /**
     * Register VAO data of all children of a group to the ShaderManager recursively
     */
    private void registerVAORecursive(MatrixStack stack, Map<ModelGroup, ModelVAO> vaos, ModelGroup group, Matrix4f projMatrix, FormRenderingContext context, Texture texture)
    {
        if  (!group.visible) return;

        stack.push();
        applyGroupTransformations(stack, group);
        ModelVAO vao = vaos.get(group);
        if (vao != null) ShaderManager.addModelGroup(this.form, vao, projMatrix.get(new Matrix4f()).mul(context.stack.peek().getPositionMatrix()).scale(-1,1,-1), texture, group.color, context.light, context.overlay);

        for (ModelGroup childGroup : group.children) {
            registerVAORecursive(stack, vaos, childGroup, projMatrix, context, texture);
        }

        stack.pop();
    }

    private static void applyGroupTransformations(MatrixStack stack, ModelGroup groupIn)
    {
        ModelGroup group = new ModelGroup("");
        group.initial.copy(groupIn.initial);
        transformTransform(group.initial);
        group.current.copy(groupIn.current);
        transformTransform(group.current);
        ICubicRenderer.translateGroup(stack, group);
        ICubicRenderer.moveToGroupPivot(stack, group);
        ICubicRenderer.rotateGroup(stack, group);
        ICubicRenderer.scaleGroup(stack, group);
        ICubicRenderer.moveBackFromGroupPivot(stack, group);
    }

    private static void transformTransform(Transform transform) {
        transform.translate.mul(-1,1,-1);
        transform.rotate.mul(-1,1,-1);
        transform.rotate2.mul(-1,1,-1);
    }
}
