package mchorse.bbs_mod.forms.sections;

import mchorse.bbs_mod.forms.FormCategories;
import mchorse.bbs_mod.forms.categories.FormCategory;
import mchorse.bbs_mod.forms.forms.*;
import mchorse.bbs_mod.ui.UIKeys;

import java.util.Arrays;
import java.util.List;

public class ShaderFormSection extends FormSection {
    private List<FormCategory> categories;

    public ShaderFormSection(FormCategories parent) {
        super(parent);
    }

    @Override
    public void initiate() {
        FormCategory shaderForm = new FormCategory(UIKeys.FORMS_CATEGORIES_SHADER, this.parent.visibility.get("shader"));

        LightForm lightForm = new LightForm();
        shaderForm.addForm(lightForm);
        CompositeShaderForm compositeShaderForm = new CompositeShaderForm();
        shaderForm.addForm(compositeShaderForm);
        GBufferShaderForm gBufferShaderForm = new GBufferShaderForm();
        shaderForm.addForm(gBufferShaderForm);
        ComputeShaderForm computeShaderForm = new ComputeShaderForm();
        shaderForm.addForm(computeShaderForm);
        BufferFlipperForm bufferFlipperForm = new BufferFlipperForm();
        shaderForm.addForm(bufferFlipperForm);
        SSBOForm ssboForm = new SSBOForm();
        shaderForm.addForm(ssboForm);

        this.categories = Arrays.asList(shaderForm);
    }

    @Override
    public List<FormCategory> getCategories() {
        return this.categories;
    }
}
