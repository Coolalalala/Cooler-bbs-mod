package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.BlockForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UIBlockFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UIBlockForm extends UIForm<BlockForm>
{
    public UIBlockForm()
    {
        super();

        this.defaultPanel = new UIBlockFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITORS_BLOCK_TITLE, Icons.BLOCK);
        this.registerDefaultPanels();
    }
}