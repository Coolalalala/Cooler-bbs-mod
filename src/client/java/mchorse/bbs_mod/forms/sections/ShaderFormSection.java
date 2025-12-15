package mchorse.bbs_mod.forms.sections;

import mchorse.bbs_mod.forms.FormCategories;
import mchorse.bbs_mod.forms.categories.FormCategory;
import mchorse.bbs_mod.forms.forms.FinalShaderForm;
import mchorse.bbs_mod.forms.forms.LightForm;
import mchorse.bbs_mod.ui.UIKeys;

import java.util.Arrays;
import java.util.List;

public class ShaderFormSection extends FormSection {
    private List<FormCategory> categories;
    private FormCategory lights;

    public ShaderFormSection(FormCategories parent) {
        super(parent);
    }

    @Override
    public void initiate() {
        FormCategory shaderForm = new FormCategory(UIKeys.FORMS_CATEGORIES_SHADER, this.parent.visibility.get("shader"));

        LightForm lightForm = new LightForm();
        shaderForm.addForm(lightForm);

        FinalShaderForm finalShaderForm = new FinalShaderForm();
        shaderForm.addForm(finalShaderForm);

        this.categories = Arrays.asList(shaderForm);
    }

    @Override
    public List<FormCategory> getCategories() {
        return this.categories;
    }
}
