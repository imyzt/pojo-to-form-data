package top.imyzt.plugins;


import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.intellij.notification.*;
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

import static com.intellij.notification.NotificationType.*;

/**
 * @author imyzt
 * @date 2020/04/01
 * @description 参数工具
 */
public class ParamUtils {

    private static final NotificationGroup NOTIFICATION_GROUP =
            new NotificationGroup("pojo2formData.NotificationGroup", NotificationDisplayType.BALLOON, true);

    private static final String COPIED_SUCCESS = "Copied to pasteboard.";

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

    private static Object getValue(PsiType type) {
        // 原始类型
        if (type instanceof PsiPrimitiveType) {

            return PRIMITIVE_TYPE_MAP.get(type.getCanonicalText());

            // 引用类型
        } else if (type instanceof PsiClassReferenceType) {

            // 处理字符引用
            String className = ((PsiClassReferenceType) type).getClassName();

            // 处理支持的引用类型
            return PRIMITIVE_TYPE_MAP.get(className);
        }
        return null;
    }


    private static void notification(Project project, String msg, NotificationType notificationType) {
        Notification error = NOTIFICATION_GROUP.createNotification(msg, notificationType);
        Notifications.Bus.notify(error, project);
    }


    public static void paramToCopied (AnActionEvent e, Function<Map<String, Object>, String> function) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (Objects.isNull(psiFile) || Objects.isNull(editor)) {
            notification(project, "Not in editing area", ERROR);
            return;
        }

        PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = PsiTreeUtil.getContextOfType(elementAt, PsiClass.class);

        if (selectedClass == null) {
            notification(project, "Attribute is empty", ERROR);
            return;
        }

        Map<String, Object> fields = new LinkedHashMap<>();

        StringBuilder notSupportType = new StringBuilder();
        for (PsiField field : selectedClass.getAllFields()) {
            // 支持的数据类型
            Object zeroValue = getValue(field.getType());
            if (Objects.nonNull(zeroValue)) {
                fields.put(field.getName(), zeroValue);
            } else {
                notSupportType.append(",").append(field.getName());
            }
        }

        String apply = function.apply(fields);

        StringSelection selection = new StringSelection(apply);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        if (notSupportType.length() > 0) {
            notification(project, COPIED_SUCCESS + " Exclude fields " + notSupportType.substring(1), WARNING);
        } else {
            notification(project, COPIED_SUCCESS, INFORMATION);
        }
    }
}
