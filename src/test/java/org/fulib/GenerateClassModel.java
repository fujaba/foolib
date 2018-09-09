package org.fulib;

import org.fulib.builder.ClassBuilder;
import org.fulib.builder.ClassModelBuilder;
import org.junit.Test;

public class GenerateClassModel
{
   @Test
   public void testGenerateModel()
   {
      ClassModelBuilder mb = ClassModelBuilder.get("org.fulib.classmodel", "src/main/java");

      ClassBuilder classModel = mb.buildClass("ClassModel")
            .buildAttribute("packageName", mb.STRING)
            .buildAttribute("mainJavaDir", mb.STRING)
            .buildAttribute("defaultRoleType", mb.STRING);

      ClassBuilder fuClass = mb.buildClass("Clazz")
            .buildAttribute("name", mb.STRING)
            .buildAttribute("modified", mb.BOOLEAN, "false");

      ClassBuilder attribute = mb.buildClass("Attribute")
            .buildAttribute("name", mb.STRING)
            .buildAttribute("type", mb.STRING)
            .buildAttribute("initialization", mb.STRING)
            .buildAttribute("modified", mb.BOOLEAN, "false");

      ClassBuilder assocRole = mb.buildClass("AssocRole")
            .buildAttribute("name", mb.STRING)
            .buildAttribute("cardinality", mb.INT)
            .buildAttribute("roleType", mb.STRING)
            .buildAttribute("modified", mb.BOOLEAN, "false");

      classModel.buildAssociation(fuClass, "classes", mb.MANY, "model", mb.ONE);

      fuClass.buildAssociation(attribute, "attributes", mb.MANY, "clazz", mb.ONE);

      fuClass.buildAssociation(assocRole, "roles", mb.MANY, "clazz", mb.ONE);

      assocRole.buildAssociation(assocRole, "other", mb.ONE, "other", mb.ONE);

      ClassBuilder fileFragmentMap = mb.buildClass("FileFragmentMap")
            .buildAttribute("fileName", mb.STRING);

      ClassBuilder codeFragment = mb.buildClass("CodeFragment")
            .buildAttribute("key", mb.STRING)
            .buildAttribute("text", mb.STRING);

      Generator.generate(mb.getClassModel());

   }
}
