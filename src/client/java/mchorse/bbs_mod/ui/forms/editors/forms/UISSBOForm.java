package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.SSBOForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UISSBOFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UISSBOForm extends UIForm<SSBOForm>{
    public UISSBOForm() {
        super();

        this.defaultPanel = new UISSBOFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITOR_SSBO_TITLE, Icons.BUCKET);
        this.registerDefaultPanels();
    }
}
