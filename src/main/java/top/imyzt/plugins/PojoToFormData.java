package top.imyzt.plugins;

import com.google.common.base.Joiner;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Pojo To FormData
 * @author imyzt
 */
public class PojoToFormData extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        ParamUtils.paramToCopied(e, (fields) -> Joiner.on("\n").withKeyValueSeparator(":").join(fields));

    }
}
