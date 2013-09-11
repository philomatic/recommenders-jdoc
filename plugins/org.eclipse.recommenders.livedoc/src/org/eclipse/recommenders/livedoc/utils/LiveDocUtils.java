package org.eclipse.recommenders.livedoc.utils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.Names;
import org.eclipse.recommenders.utils.names.VmMethodName;

import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;

public class LiveDocUtils {
    
    public static String methodSignature(IMethodName method) {

        StringBuilder sb = new StringBuilder();

        sb.append("<code>");
        sb.append(method.getName());
        sb.append("(");

            for (int i = 0; i < method.getParameterTypes().length; i++) {

                ITypeName parameter = method.getParameterTypes()[i];
                sb.append(Names.vm2srcQualifiedType(parameter));
                if (i < (method.getParameterTypes().length - 1)) {
                    sb.append(", ");
                }
            }
            
        sb.append(")");
        sb.append("</code>");

        return sb.toString();
    }
    
    public static IMethodName asIMethodName(MethodDoc methodDoc) {
        String srcDeclaringType = StringUtils.substringBeforeLast(methodDoc.qualifiedName(), ".");
        String methodName = StringUtils.substringAfterLast(methodDoc.qualifiedName(), ".");
        String[] parameters = asStringArray(methodDoc.parameters());

        return VmMethodName.get(Names.src2vmMethod(srcDeclaringType, methodName, parameters, methodDoc.returnType()
                .qualifiedTypeName()));
    }
    
    public static String[] asStringArray(Parameter[] parameters) {

        String[] result = new String[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            result[i] = parameter.type().qualifiedTypeName();
        }

        return result;
    }
    
    public static String extractTypeName(Doc holder) {
        if (holder.isMethod()) {
            String typeName = StringUtils.substringBefore(holder.toString(), "(");
            typeName = StringUtils.substringBeforeLast(typeName, ".");
            return Names.src2vmType(typeName);
        } else {
            return Names.src2vmType(holder.toString());
        }
    }
    
    public static void highlight(StringBuilder sb) {
        sb.insert(0, "<div style=\"background-color:#FFFFD5;\">");
        sb.append("</div>");
    }

}
