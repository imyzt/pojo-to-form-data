package top.imyzt.plugins;

import com.google.common.base.Joiner;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NonNls;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Pojo To FormData
 * @author imyzt
 */
public class PojoToFormData extends AnAction {

    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("pojo2formData.NotificationGroup", NotificationDisplayType.BALLOON, true);

    @NonNls
    private static final Map<String, Object> PRIMITIVE_TYPE_MAP = new HashMap<>();

    static {

        LocalDateTime now = LocalDateTime.now();

        PRIMITIVE_TYPE_MAP.put("Short", 0);
        PRIMITIVE_TYPE_MAP.put("short", 0);
        PRIMITIVE_TYPE_MAP.put("Integer", 0);
        PRIMITIVE_TYPE_MAP.put("int", 0);
        PRIMITIVE_TYPE_MAP.put("Long", 0);
        PRIMITIVE_TYPE_MAP.put("long", 0);
        PRIMITIVE_TYPE_MAP.put("Float", 0.0);
        PRIMITIVE_TYPE_MAP.put("float", 0.0);
        PRIMITIVE_TYPE_MAP.put("Double", 0.0);
        PRIMITIVE_TYPE_MAP.put("double", 0.0);
        PRIMITIVE_TYPE_MAP.put("Boolean", false);
        PRIMITIVE_TYPE_MAP.put("boolean", false);
        PRIMITIVE_TYPE_MAP.put("Byte", 0);
        PRIMITIVE_TYPE_MAP.put("byte", 0);
        PRIMITIVE_TYPE_MAP.put("Character", "c");
        PRIMITIVE_TYPE_MAP.put("char", "c");
        PRIMITIVE_TYPE_MAP.put("String", "demoData");
        PRIMITIVE_TYPE_MAP.put("Date", DateTimeEnum.DATE_TIME.format(now));
        PRIMITIVE_TYPE_MAP.put("LocalDateTime", DateTimeEnum.DATE_TIME.format(now));
        PRIMITIVE_TYPE_MAP.put("LocalTime", DateTimeEnum.TIME.format(now));
        PRIMITIVE_TYPE_MAP.put("LocalDate", DateTimeEnum.DATE.format(now));

    }

    @Override
    public void actionPerformed(AnActionEvent e) {


        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = PsiTreeUtil.getContextOfType(elementAt, PsiClass.class);


        try {
            Map<String, Object> fields = getFields(selectedClass);
            String join = Joiner.on("\n").withKeyValueSeparator(":").join(fields);
            StringSelection selection = new StringSelection(join);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);

            showMsg("已拷贝至粘贴板", project, NotificationType.INFORMATION);

        } catch (ToFormDataException ex) {
            showMsg(ex.getMsg(), project, NotificationType.ERROR);
        }
    }

    private static Map<String, Object> getFields(PsiClass psiClass) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (psiClass == null) {
            return map;
        }

        for (PsiField field : psiClass.getAllFields()) {
            map.put(field.getName(), typeResolve(field.getType()));
        }

        return map;
    }

    private static Object typeResolve(PsiType type) {

        // 原始类型
        if (type instanceof PsiPrimitiveType) {

            return PRIMITIVE_TYPE_MAP.get(type.getCanonicalText());

            // 数组类型
        } else if (type instanceof PsiArrayType) {

            throw new ToFormDataException("包含数组类型, 建议使用JSON");

            // 引用类型
        } else {

            // 处理字符引用
            String className = ((PsiClassReferenceType) type).getClassName();

            // 处理支持的引用类型
            Object returnDefaultValue;
            if (Objects.nonNull((returnDefaultValue =  PRIMITIVE_TYPE_MAP.get(className)))) {
                return returnDefaultValue;
            } else {
                throw new ToFormDataException("包含引用类型, 建议使用JSON");
            }
        }
    }

    private static void showMsg(String msg, Project project, NotificationType notificationType) {
        Notification error = NOTIFICATION_GROUP.createNotification(msg, notificationType);
        Notifications.Bus.notify(error, project);
    }
}
