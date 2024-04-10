package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.ItemForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UIItemFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UIItemForm extends UIForm<ItemForm>
{
    public UIItemForm()
    {
        super();

        this.defaultPanel = new UIItemFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITORS_ITEM_TITLE, Icons.LINE);
        this.registerDefaultPanels();
    }
}