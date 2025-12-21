package mchorse.bbs_mod.forms.renderers;

import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.cubic.IModel;
import mchorse.bbs_mod.cubic.ModelInstance;
import mchorse.bbs_mod.cubic.data.model.Model;
import mchorse.bbs_mod.cubic.data.model.ModelGroup;
import mchorse.bbs_mod.cubic.model.bobj.BOBJModel;
import mchorse.bbs_mod.cubic.render.vao.BOBJModelVAO;
import mchorse.bbs_mod.cubic.render.vao.ModelVAO;
import mchorse.bbs_mod.cubic.render.vao.ModelVAOData;
import mchorse.bbs_mod.forms.FormUtilsClient;
import mchorse.bbs_mod.forms.ShaderManager;
import mchorse.bbs_mod.forms.forms.BodyPart;
import mchorse.bbs_mod.forms.forms.Form;
import mchorse.bbs_mod.forms.forms.GBufferShaderForm;
import mchorse.bbs_mod.graphics.texture.Texture;
import mchorse.bbs_mod.resources.Link;
import mchorse.bbs_mod.ui.framework.UIContext;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.ArrayList;
import java.util.List;
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
        if (!BBSRendering.isIrisShadersEnabled()) return;
        // Iterate through body parts (children)
        List<ModelVAOData> modelVaoData = new ArrayList<>();
        for (BodyPart part : form.parts.getAllTyped()) {
            Form childForm = part.getForm();
            if (childForm != null) {
                FormRenderer<?> childRenderer = FormUtilsClient.getRenderer(childForm);
                if (childRenderer instanceof ModelFormRenderer modelRenderer) {
                    ModelInstance childModelInstance = modelRenderer.getModel();

                    ModelVAOData data = extractVAOData(childModelInstance);
                    if (data != null) {
                        modelVaoData.add(data);
                    }
                }
            }
        }

        if (modelVaoData.isEmpty()) return;

        BufferBuilder bufferBuilder = new BufferBuilder(VertexFormats.POSITION_TEXTURE.getVertexSizeByte() * 4);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        // Populate the buffer
        for (ModelVAOData data : modelVaoData) {
            float[] verts = data.vertices();
            float[] texcoords = data.texCoords();
            float[] normals = data.normals();
            for (int i = 0; i < data.vertices().length/3; i++) {
                bufferBuilder.vertex(verts[3*i], verts[3*i + 1], verts[3*i + 2])
                        .texture(texcoords[2*i], texcoords[2*i + 1])
                        .normal(normals[3*i], normals[3*i + 1], normals[3*i + 2])
                        .next();
            }
        }

        BufferBuilder.BuiltBuffer renderedBuffer = bufferBuilder.end();
        VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vbo.bind();
        vbo.upload(renderedBuffer);
        VertexBuffer.unbind();

        ShaderManager.register(this.form, this.form.renderStage.get());
        ShaderManager.addVAO(this.form, vbo);
    }

    public ModelVAO processModelVAO(ModelInstance modelInstance) {
        // First check if the model uses VAO rendering
        if (!modelInstance.isVAORendered()) {
            System.out.println("Model does not use VAO rendering");
            return null;
        }

        IModel model = modelInstance.getModel();

        // Handle BOBJ models
        if (model instanceof BOBJModel bobjModel) {
            BOBJModelVAO vao = bobjModel.getVao();
            if (vao != null) {
                System.out.println("Got BOBJ VAO: " + vao);
                return null; // TODO: support bobj models
            }
        }
        // Handle regular cubic models
        else if (model instanceof Model) {
            Map<ModelGroup, ModelVAO> vaos = modelInstance.getVaos();
            System.out.println("Model has " + vaos.size() + " VAOs");

            // Process each group's VAO
            for (Map.Entry<ModelGroup, ModelVAO> entry : vaos.entrySet()) {
                ModelGroup group = entry.getKey();
                ModelVAO vao = entry.getValue();

                System.out.println("Group: " + group.id + ", VAO: " + vao);
                // Use the VAO for rendering or processing
                return vao;
            }
        }

        return null;
    }

    /**
     * Extract VAO data from a model instance for use in GBuffer shaders
     */
    public ModelVAOData extractVAOData(ModelInstance modelInstance) {
        // First check if the model uses VAO rendering
        if (!modelInstance.isVAORendered()) {
            System.out.println("Model does not use VAO rendering");
            return null;
        }

        IModel model = modelInstance.getModel();

        // Handle regular cubic models
        if (model instanceof Model) {
            Map<ModelGroup, ModelVAO> vaos = modelInstance.getVaos();
            System.out.println("Model has " + vaos.size() + " VAOs");

            // For now, just get the first VAO's data
            // In a full implementation, you might want to combine all VAO data
            for (Map.Entry<ModelGroup, ModelVAO> entry : vaos.entrySet()) {
                ModelGroup group = entry.getKey();
                ModelVAO vao = entry.getValue();
                
                // Extract the VAO data
                ModelVAOData data = vao.getData();
                if (data != null) {
                    System.out.println("Extracted VAO data for group: " + group.id);
                    System.out.println("Vertices: " + data.vertices().length);
                    System.out.println("Normals: " + data.normals().length);
                    System.out.println("TexCoords: " + data.texCoords().length);
                    System.out.println("Tangents: " + data.tangents().length);
                    return data;
                }
            }
        }
        
        // BOBJ models would need separate handling
        else if (model instanceof BOBJModel bobjModel) {
            BOBJModelVAO vao = bobjModel.getVao();
            if (vao != null) {
                // Extract data from BOBJModelVAO
                ModelVAOData data = vao.getModelVAOData();
                System.out.println("Processing BOBJ model");
                return data;
            }
        }

        return null;
    }
}