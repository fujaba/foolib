package org.fulib;

import org.fulib.classmodel.*;
import org.fulib.util.Generator4ClassFile;
import org.fulib.yaml.YamlIdMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The fulib Generator generates Java code from a class model
 * <pre>
 * <!-- insert_code_fragment: Fulib.createGenerator-->
      ClassModel model = mb.getClassModel();
      Fulib.generator().generate(model);
 * <!-- end_code_fragment:  -->
 * </pre>
 */
public class Generator
{
   // =============== Constants ===============

   private static final String MODEL_FILE_NAME = "classModel.yaml";

   // =============== Static Fields ===============

   private static Logger logger;

   static
   {
      logger = Logger.getLogger(Generator.class.getName());
      logger.setLevel(Level.SEVERE);
   }

   // =============== Fields ===============

   private String customTemplateFile;

   // =============== Properties ===============

   public String getCustomTemplateFile()
   {
      return this.customTemplateFile;
   }

   /**
    * You may overwrite code generation templates within some custom template file. <br>
    * Provide your templates for code generation as in:
    * <pre>
    * <!-- insert_code_fragment: testCustomTemplates -->
      Fulib.generator().setCustomTemplatesFile("templates/custom.stg").generate(model);
    * <!-- end_code_fragment: testCustomTemplates -->
    * </pre>
    *
    * @param customFileName
    *    the custom templates file name
    *
    * @return this instance, to allow call chaining
    */
   public Generator setCustomTemplatesFile(String customFileName)
   {
      this.customTemplateFile = customFileName;
      return this;
   }

   // =============== Methods ===============

   /**
    * The fulib Generator generates Java code from a class model
    * <pre>
    * <!-- insert_code_fragment: Fulib.createGenerator-->
      ClassModel model = mb.getClassModel();
      Fulib.generator().generate(model);
    * <!-- end_code_fragment:  -->
    * </pre>
    *
    * @param model
    *    providing classes to generate Java implementations for
    */
   public void generate(ClassModel model)
   {
      ClassModel oldModel = loadClassModel(model.getPackageSrcFolder(), MODEL_FILE_NAME);

      if (oldModel != null)
      {
         this.markModifiedElementsInOldModel(oldModel, model);

         // remove code of modfiedElements
         this.generateClasses(oldModel);
      }

      this.generateClasses(model);

      saveNewClassModel(model, MODEL_FILE_NAME);
   }

   private void generateClasses(ClassModel model)
   {
      final Generator4ClassFile generator = new Generator4ClassFile()
         .setCustomTemplatesFile(this.getCustomTemplateFile());

      for (Clazz clazz : model.getClasses())
      {
         generator.generate(clazz);
      }
   }

   static ClassModel loadClassModel(String modelFolder, String modelFileName)
   {
      String fileName = modelFolder + '/' + modelFileName;
      try
      {
         Path path = Paths.get(fileName);

         if (!Files.exists(path))
         {
            return null;
         }

         byte[] bytes = Files.readAllBytes(path);
         String yamlString = new String(bytes);

         YamlIdMap idMap = new YamlIdMap(ClassModel.class.getPackage().getName());
         return (ClassModel) idMap.decode(yamlString);
      }
      catch (IOException e)
      {
         Logger.getGlobal().log(Level.SEVERE, "\n   could not load " + fileName, e);
      }

      return null;
   }

   static void saveNewClassModel(ClassModel model, String modelFileName)
   {
      YamlIdMap idMap = new YamlIdMap(ClassModel.class.getPackage().getName());
      String yamlString = idMap.encode(model);
      try
      {
         String modelFolder = model.getPackageSrcFolder();
         String fileName = modelFolder + '/' + modelFileName;
         Files.createDirectories(Paths.get(modelFolder));
         Files.write(Paths.get(fileName), yamlString.getBytes(), StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void markModifiedElementsInOldModel(ClassModel oldModel, ClassModel newModel)
   {
      //  check for changed package name or target folder?

      for (Clazz oldClazz : oldModel.getClasses())
      {
         Clazz newClazz = newModel.getClazz(oldClazz.getName());

         this.markModifiedElementsInOldClazz(oldClazz, newClazz);
      }
   }

   private void markModifiedElementsInOldClazz(Clazz oldClazz, Clazz newClazz)
   {
      logger = Logger.getLogger(Generator.class.getName());
      if (newClazz == null)
      {
         oldClazz.markAsModified();
         logger.info("\n   markedAsModified: class " + oldClazz.getName());
      }

      for (Attribute oldAttr : oldClazz.getAttributes())
      {
         if (this.isModified(oldAttr, newClazz))
         {
            oldAttr.markAsModified();
            logger.info("\n   markedAsModified: attribute " + oldAttr.getName());
         }
      }

      for (AssocRole oldRole : oldClazz.getRoles())
      {
         if (this.isModified(oldRole, newClazz))
         {
            oldRole.markAsModified();
            logger.info("\n   markedAsModified: role " + oldRole.getName());
            if (oldRole.getOther() != null)
            {
               oldRole.getOther().markAsModified();
               logger.info("\n   markedAsModified: role " + oldRole.getOther().getName());
            }
         }
      }

      for (FMethod oldMethod : oldClazz.getMethods())
      {
         if (this.isModified(oldMethod, newClazz))
         {
            oldMethod.setModified(true);
            logger.info("\n   markedAsModified: method " + oldMethod.getDeclaration());
         }
      }
   }

   private boolean isModified(Attribute oldAttr, Clazz newClazz)
   {
      if (newClazz == null)
      {
         return true;
      }

      final Attribute newAttr = newClazz.getAttribute(oldAttr.getName());
      return newAttr == null || !Objects.equals(oldAttr.getType(), newAttr.getType()) || !Objects.equals(
         oldAttr.getPropertyStyle(), newAttr.getPropertyStyle());
   }

   private boolean isModified(AssocRole oldRole, Clazz newClazz)
   {
      if (newClazz == null)
      {
         return true;
      }

      final AssocRole newRole = newClazz.getRole(oldRole.getName());

      return newRole == null || oldRole.getCardinality() != newRole.getCardinality() || !Objects.equals(
         oldRole.getPropertyStyle(), oldRole.getPropertyStyle());
   }

   private boolean isModified(FMethod oldMethod, Clazz newClazz)
   {
      if (newClazz == null)
      {
         return true;
      }

      for (FMethod newMethod : newClazz.getMethods())
      {
         if (oldMethod.signatureMatches(newMethod))
         {
            return false;
         }
      }
      return true;
   }
}
