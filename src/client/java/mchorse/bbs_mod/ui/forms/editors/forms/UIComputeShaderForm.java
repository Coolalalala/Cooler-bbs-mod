package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.CompositeShaderForm;
import mchorse.bbs_mod.forms.forms.ComputeShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UICompositeShaderFormPanel;
import mchorse.bbs_mod.ui.forms.editors.panels.UIComputeShaderFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UIComputeShaderForm extends UIForm<ComputeShaderForm> {
    public UIComputeShaderForm() {
        super();

        this.defaultPanel = new UIComputeShaderFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITOR_SHADER_COMPUTE_TITLE, Icons.SPRAY);
        this.registerDefaultPanels();
    }
}
