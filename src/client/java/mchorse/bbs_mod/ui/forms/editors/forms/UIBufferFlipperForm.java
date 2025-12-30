package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.BufferFlipperForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UIBufferFlipperFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UIBufferFlipperForm extends UIForm<BufferFlipperForm> {
    public UIBufferFlipperForm() {
        super();

        this.defaultPanel = new UIBufferFlipperFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITOR_SHADER_BUFFERFLIPPER_TITLE, Icons.SPRAY);
        this.registerDefaultPanels();
    }
}
