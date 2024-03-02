package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.LabelForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UILabelFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UILabelForm extends UIForm<LabelForm>
{
    public UILabelForm()
    {
        super();

        this.defaultPanel = new UILabelFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITORS_LABEL_TITLE, Icons.FONT);
        this.registerDefaultPanels();
    }
}