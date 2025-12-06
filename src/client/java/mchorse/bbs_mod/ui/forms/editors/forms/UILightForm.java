package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.LightForm;
import mchorse.bbs_mod.ui.Keys;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UILightFormPanel;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.input.color.UIColorPicker;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import mchorse.bbs_mod.utils.colors.Color;

public class UILightForm extends UIForm<LightForm> {
    public UILightForm() {
        super();

        this.defaultPanel = new UILightFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITORS_LIGHT_TITLE, Icons.BLOCK);
        this.registerDefaultPanels();
    }
}
