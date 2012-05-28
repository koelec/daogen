/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.koelec.daogen.gen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.persistence.QueryHint;
import nl.koelec.daogen.core.JpaNamedQuery;
import nl.koelec.daogen.core.JpaQuery;
import nl.koelec.daogen.core.JpaTemporal;

/**
 *
 * @author Chris
 */
public class TypeModel {

    List<MethodModel> methods = new ArrayList<MethodModel>();
    private final String packageName;
    private final String implClassName;
    private final String ifcClassName;
    private final String baseName;
    private ProcessingEnvironment processingEnv;

    TypeModel(TypeElement typeElement, ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();

        String className = typeElement.getSimpleName().toString();
        packageName = packageElement.getQualifiedName().toString();
        ifcClassName = className;
        implClassName = getImplClassName(className);
        baseName = getBaseName(className);

        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                methods.add(new MethodModel((ExecutableElement) enclosedElement));
            }
        }
    }

    private String getImplClassName(String className) {
        if (className.length() > 1 && className.startsWith("I") && Character.isUpperCase(className.charAt(1))) {
            return className.substring(1);
        } else {
            return className + "Impl";
        }
    }

    private String getBaseName(String className) {
        if (className.length() > 1 && className.startsWith("I") && Character.isUpperCase(className.charAt(1))) {
            return className.substring(1);
        } else {
            return className;
        }
    }

    public class ParamModel {

        String type;
        String temporalType;
        String name;
        boolean last;

        public boolean isLast() {
            return last;
        }

        ParamModel(VariableElement param, boolean last) {
            this.type = param.asType().toString();
            this.name = param.getSimpleName().toString();
            this.last = last;
            JpaTemporal jpaTemporal = param.getAnnotation(JpaTemporal.class);
            this.temporalType = jpaTemporal != null ? jpaTemporal.value().getDeclaringClass().getName() + "." + jpaTemporal.value() : null;
        }

        public String getName() {
            return name;
        }

        public String getTemporalType() {
            return temporalType;
        }

        public String getType() {
            return type;
        }
    }

    public final class MethodModel {

        String name;
        String returnType;
        String queryString;
        boolean namedQuery;
        boolean collectionReturnType;
        boolean throwException;
        List<Hint> hints = new ArrayList<Hint>();
        List<ParamModel> params = new ArrayList<ParamModel>();

        public boolean isNamedQuery() {
            return namedQuery;
        }

        public boolean isCollectionReturnType() {
            return collectionReturnType;
        }

        public boolean isThrowException() {
            return throwException;
        }

        public List<Hint> getHints() {
            return hints;
        }

        public List<ParamModel> getParams() {
            return params;
        }

        public String getName() {
            return name;
        }

        public String getReturnType() {
            return returnType;
        }

        public String getQueryString() {
            return queryString;
        }

        MethodModel(ExecutableElement method) {
            this.name = method.getSimpleName().toString();
            JpaQuery anno = method.getAnnotation(JpaQuery.class);
            JpaNamedQuery namedQueryAnno = method.getAnnotation(JpaNamedQuery.class);
            if (anno != null && namedQueryAnno != null) {
                throw new RuntimeException("cannot have both JpaNamedQuery and JpaQuery annotation on same method: " + method.toString());
            }
            this.throwException = anno != null ? anno.throwException() : (namedQueryAnno != null ? namedQueryAnno.throwException() : false);
            this.queryString = anno != null ? anno.query() : (namedQueryAnno != null ? namedQueryAnno.query() : null);
            this.namedQuery = namedQueryAnno != null;
            this.returnType = method.getReturnType().toString();
            // see if returnType is a Collection
            TypeMirror erasedType = processingEnv.getTypeUtils().erasure(method.getReturnType());
            TypeElement typeElement = processingEnv.getElementUtils().getTypeElement("java.util.Collection");
            TypeMirror collectionType = typeElement.asType();
            this.collectionReturnType = processingEnv.getTypeUtils().isAssignable(erasedType, collectionType);
            Set<String> queryParams = getQueryParams(queryString);
            for (int i = 0; i < method.getParameters().size(); i++) {
                if (queryString != null) {
                    String paramName = method.getParameters().get(i).getSimpleName().toString();
                    validateIsTrue(queryParams.remove(paramName), "method param " + paramName + " not found in queryString" + ", method: " + toString());
                }
                ParamModel paramModel = new ParamModel(method.getParameters().get(i), i == method.getParameters().size() - 1);
                this.params.add(paramModel);
            }
            validateIsTrue(queryString == null || queryParams.isEmpty(), "no method param(s) found for query params: " + toString(queryParams) + ", method: " + toString());
            if (namedQueryAnno != null && namedQueryAnno.hints() != null) {
                for (QueryHint hint : namedQueryAnno.hints()) {
                    hints.add(new Hint(hint.name(), hint.value()));
                }
            }
            if (anno != null && anno.hints() != null) {
                for (QueryHint hint : anno.hints()) {
                    hints.add(new Hint(hint.name(), hint.value()));
                }
            }
        }

        private String toString(Set<String> set) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : set) {
                stringBuilder.append(s);
                stringBuilder.append(" ");
            }
            return stringBuilder.toString();
        }

        @Override
        public String toString() {
            return baseName + "." + name;
        }
    }

    public class Hint {

        String name;
        String value;

        public Hint(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    private Set<String> getQueryParams(String queryString) {
        Set<String> queryParams = new HashSet<String>();
        if (queryString != null) {
            String[] words = queryString.split(" ");
            for (String word : words) {
                if (word.startsWith(":")) {
                    queryParams.add(word.substring(1));
                }
            }
        }
        return queryParams;
    }

    private void validateIsTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public List<MethodModel> getMethods() {
        return methods;
    }

    public String getIfcClassName() {
        return ifcClassName;
    }

    public String getfqIfcClassName() {
        return packageName + "." + ifcClassName;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getImplClassName() {
        return implClassName;
    }

    public String getfqImplClassName() {
        return packageName + "." + implClassName;
    }
}
