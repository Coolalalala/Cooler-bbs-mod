package mchorse.bbs_mod.ui.forms.editors.forms;

import mchorse.bbs_mod.forms.forms.GLVertexForm;
import mchorse.bbs_mod.ui.UIKeys;
import mchorse.bbs_mod.ui.forms.editors.panels.UIGLVertexFormPanel;
import mchorse.bbs_mod.ui.utils.icons.Icons;

public class UIGLVertexForm extends UIForm<GLVertexForm> {
    public UIGLVertexForm() {
        super();

        this.defaultPanel = new UIGLVertexFormPanel(this);

        this.registerPanel(this.defaultPanel, UIKeys.FORMS_EDITOR_VERTEX_TITLE, Icons.ONION_SKIN);
        this.registerDefaultPanels();
    }
}
