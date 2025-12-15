package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.FinalShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UIFinalShaderPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UIFinalShaderForm extends UIForm<FinalShaderForm>  {
    public UIFinalShaderForm() {
        super();

        this.defaultPanel = new UIFinalShaderPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITORS_SHADER_FINAL_TITLE, Icons.SPRAY);
        this.registerDefaultPanels();
    }
}
