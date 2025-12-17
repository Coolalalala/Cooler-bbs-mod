package mchorse.bbs_mod.forms.forms;

import java.util.HashSet;
import java.util.Set;

public class CompositeShaderForm extends ShaderForm {
    private int[] drawBuffers = new int[]{0};
    private Set<Integer> disableFlipBuffers = new HashSet<>();

    public CompositeShaderForm() {
        super();
    }

    @Override
    protected String getDefaultDisplayName() {
        return "Compute Shader Program";
    }
    
    public int[] getDrawBuffers() {
        return this.drawBuffers.clone();
    }
    
    public void setDrawBuffers(int[] drawBuffers) {
        this.drawBuffers = drawBuffers != null ? drawBuffers.clone() : new int[]{0};
    }
    
    public boolean shouldFlipBuffer(int buffer) {
        // Check if buffer is in the disabled flip list
        return !this.disableFlipBuffers.contains(buffer);
    }
    
    public void setDisableFlipBuffers(int[] buffers) {
        this.disableFlipBuffers.clear();
        if (buffers != null) {
            for (int buffer : buffers) {
                this.disableFlipBuffers.add(buffer);
            }
        }
    }
}