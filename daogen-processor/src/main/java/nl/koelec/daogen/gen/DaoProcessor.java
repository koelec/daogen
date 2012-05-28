package nl.koelec.daogen.gen;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import nl.koelec.daogen.core.DaoType;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.log.Logger;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.*;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.persistence.QueryHint;
import javax.tools.*;
import org.hybridlabs.source.beautifier.CharacterSequence;
import org.hybridlabs.source.beautifier.JavaImportBeautifierImpl;

@SupportedAnnotationTypes("nl.koelec.daogen.core.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({DaoProcessor.DAOFACTORY_TEMPLATE_OPTION, DaoProcessor.IDAOFACTORY_TEMPLATE_OPTION, DaoProcessor.ORM_TEMPLATE_OPTION, DaoProcessor.TEMPLATE_DIR_OPTION, DaoProcessor.DAO_TEMPLATE_OPTION})
public class DaoProcessor extends AbstractProcessor {

    public static final String TEMPLATE_DIR_OPTION = "templatedir";
    public static final String ORM_TEMPLATE_OPTION = "ormtemplate";
    public static final String ORM_TEMPLATE_DEFAULT = "orm-template.ftl";
    public static final String DAO_TEMPLATE_OPTION = "daotemplate";
    public static final String DAO_TEMPLATE_DEFAULT = "dao-template.ftl";
    public static final String DAOFACTORY_TEMPLATE_OPTION = "daofactorytemplate";
    public static final String DAOFACTORY_TEMPLATE_DEFAULT = "daofactory-template.ftl";
    public static final String IDAOFACTORY_TEMPLATE_OPTION = "idaofactorytemplate";
    public static final String IDAOFACTORY_TEMPLATE_DEFAULT = "idaofactory-template.ftl";
    private Configuration configuration;

    /**
     * PrintStackTrace wordt hier gebruikt, zodat er tijdens de build in ieder
     * geval feedback wordt gegeven over excepties die optreden. We zijn hier
     * verder niet in staat om een logger van of de java compiler of van Maven
     * te (her) gebruiken om dit op een nettere manier te doen.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        try {
            PackageElement packageElement = null;
            List<TypeModel> types = new ArrayList<TypeModel>();
            for (Element e : roundEnv.getElementsAnnotatedWith(DaoType.class)) {

                if (e.getKind() == ElementKind.INTERFACE) {
                    TypeElement typeElement = (TypeElement) e;
                    if (packageElement == null) {
                        packageElement = processingEnv.getElementUtils().getPackageOf(typeElement);
                    }
                    TypeModel typeModel = new TypeModel(typeElement, processingEnv);
                    types.add(typeModel);
                    generateSourceFile(Arrays.asList(typeModel), DAO_TEMPLATE_OPTION, DAO_TEMPLATE_DEFAULT, typeModel.getfqImplClassName());
                }
            }
            if (types.size() > 0) {
                generateSourceFile(types, IDAOFACTORY_TEMPLATE_OPTION, IDAOFACTORY_TEMPLATE_DEFAULT, packageElement.getQualifiedName() + ".IDaoFactory");
                generateSourceFile(types, DAOFACTORY_TEMPLATE_OPTION, DAOFACTORY_TEMPLATE_DEFAULT, packageElement.getQualifiedName() + ".DaoFactory");
                generateOrmFile(types, ORM_TEMPLATE_OPTION, ORM_TEMPLATE_DEFAULT, "queries-orm.xml");
            }
        } catch (Exception e) {
            e.printStackTrace();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    e.toString());
        }
        return true;
    }

    private String beautify(String input) {
        JavaImportBeautifierImpl javaImportBeautifierImpl = new JavaImportBeautifierImpl();
        javaImportBeautifierImpl.setFormat(true);
        javaImportBeautifierImpl.setOrganizeImports(true);
        CharacterSequence characterSequence = new CharacterSequence(input);
        javaImportBeautifierImpl.beautify(characterSequence);
        return characterSequence.getString();
    }

    private void generateOrmFile(List<TypeModel> typeModels, String templateOption, String templateDefault, String fileName) throws Exception {
        Template template = initTemplate(templateOption, templateDefault);
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("generator", this.getClass().getCanonicalName());
        root.put("currentTime", new Date().toString());
        root.put("types", typeModels);
        FileObject sourceFile = processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT, "", fileName);
        Writer out = new OutputStreamWriter(sourceFile.openOutputStream());
        template.process(root, out);
        out.close();
    }

    private void generateSourceFile(List<TypeModel> typeModels, String templateOption, String templateDefault, String fileName) throws Exception {
        Template template = initTemplate(templateOption, templateDefault);
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("generator", this.getClass().getCanonicalName());
        root.put("currentTime", new Date().toString());
        root.put("types", typeModels);
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(fileName);
        Writer out = new OutputStreamWriter(sourceFile.openOutputStream());
        StringWriter writer = new StringWriter();
        template.process(root, writer);
        out.write(beautify(writer.toString()));
        out.close();
    }

    private String getOptionValue(String option) {
        return processingEnv.getOptions().get(option);
    }

    private Configuration getConfiguration() throws Exception {
        if (configuration == null) {
            Logger.selectLoggerLibrary(Logger.LIBRARY_SLF4J);
            configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            String value = getOptionValue(TEMPLATE_DIR_OPTION);
            if (value == null) {
                value = ".";
            }
            File dir = new File(value);
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException(TEMPLATE_DIR_OPTION + " ("
                        + value + ") must be a valid directory.");
            }
            configuration.setDirectoryForTemplateLoading(dir);
            FileTemplateLoader ftl = new FileTemplateLoader(dir);
            ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "");
            TemplateLoader[] loaders = new TemplateLoader[]{ftl, ctl};
            MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
            configuration.setTemplateLoader(mtl);
        }
        return configuration;
    }
    
    private Template initTemplate(String templateOption, String templateDefault) throws Exception {
        String optionValue = getOptionValue(templateOption);
        return getConfiguration().getTemplate(optionValue != null ? optionValue : templateDefault);
    }
}
