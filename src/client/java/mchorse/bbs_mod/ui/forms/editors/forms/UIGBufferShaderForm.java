package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.GBufferShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UIGBufferShaderFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UIGBufferShaderForm extends UIForm<GBufferShaderForm> {
    public UIGBufferShaderForm() {
        super();

        this.defaultPanel = new UIGBufferShaderFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITOR_SHADER_GBUFFER_TITLE, Icons.SPRAY);
        this.registerDefaultPanels();
    }
}
