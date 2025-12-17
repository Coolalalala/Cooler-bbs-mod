package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.CompositeShaderForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UICompositeShaderFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UICompositeShaderForm extends UIForm<CompositeShaderForm>  {
    public UICompositeShaderForm() {
        super();

        this.defaultPanel = new UICompositeShaderFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITOR_SHADER_COMPOSITE_TITLE, Icons.SPRAY);
        this.registerDefaultPanels();
    }
}
