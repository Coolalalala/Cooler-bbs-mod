package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.ImageTextureForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UIImageTextureFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UIImageTextureForm extends UIForm<ImageTextureForm>{
    public UIImageTextureForm() {
        super();

        this.defaultPanel = new UIImageTextureFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITOR_IMAGETEXTURE_TITLE, Icons.BUCKET);
        this.registerDefaultPanels();
    }
}
