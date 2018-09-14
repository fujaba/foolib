package org.fulib;

import org.fulib.builder.ClassModelBuilder;


public class Fulib
{

   /**
    * ClassModelbuilder is used to create fulib class models that are input for
    * fulib code generation {@link Fulib#createGenerator()}.<br>
    * Typical usage:
    * <pre>
    * <!-- insert_code_fragment: ClassModelBuilder -->
      ClassModelBuilder mb = Fulib.createClassModelBuilder(packageName);

      ClassBuilder universitiy = mb.buildClass( "University").buildAttribute("name", mb.STRING);
    * <!-- end_code_fragment:  -->
    * </pre>
    * @param packagename
    * @return a class model builder for the given package name and with the default source folder "src/main/java"
    */
   public static ClassModelBuilder createClassModelBuilder(String packagename)
   {
      return new ClassModelBuilder(packagename);
   }


   /**
    * ClassModelbuilder is used to create fulib class models that are input for
    * fulib code generation {@link Fulib#createGenerator()}.<br>
    * Typical usage:
    * <pre>
    * <!-- insert_code_fragment: ClassModelBuilder.twoParams -->
      ClassModelBuilder mb = Fulib.createClassModelBuilder(packageName, "src/main/java");

      ClassBuilder universitiy = mb.buildClass( "University").buildAttribute("name", mb.STRING);
    * <!-- end_code_fragment:  -->
    * </pre>
    * @param packagename
    * @param sourceFolder
    * @return a class model builder for the given package name and source folder
    */
   public static ClassModelBuilder createClassModelBuilder(String packagename, String sourceFolder)
   {
      return new ClassModelBuilder(packagename, sourceFolder);
   }


   /**
    * The fulib Generator generates Java code from a class model
    * <pre>
    * <!-- insert_code_fragment: Fulib.createGenerator-->
      ClassModel model = mb.getClassModel();
      Fulib.createGenerator().generate(model);
    * <!-- end_code_fragment:  -->
    * </pre>
    * @return
    */
   public static Generator createGenerator()
   {
      return new Generator();
   }

}
