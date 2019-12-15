package org.fulib.util;

import org.fulib.Generator;
import org.fulib.Parser;
import org.fulib.StrUtil;
import org.fulib.builder.Type;
import org.fulib.classmodel.AssocRole;
import org.fulib.classmodel.Attribute;
import org.fulib.classmodel.Clazz;
import org.fulib.classmodel.FileFragmentMap;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Generator4TableClassFile extends AbstractGenerator
{
   // =============== Properties ===============

   @Override
   public Generator4TableClassFile setCustomTemplatesFile(String customTemplatesFile)
   {
      super.setCustomTemplatesFile(customTemplatesFile);
      return this;
   }

   // =============== Methods ===============

   public void generate(Clazz clazz)
   {
      String classFileName = clazz.getModel().getPackageSrcFolder() + "/tables/" + clazz.getName() + "Table.java";
      FileFragmentMap fragmentMap = Parser.parse(classFileName);

      // doGenerate code for class
      this.generatePackageDecl(clazz, fragmentMap);

      this.generateClassDecl(clazz, fragmentMap);

      this.generateConstructor(clazz, fragmentMap);

      this.generateStandardAttributes(clazz, fragmentMap);

      this.generateAttributes(clazz, fragmentMap);

      this.generateAssociations(clazz, fragmentMap);

      this.generateSelectColumns(clazz, fragmentMap);

      this.generateAddColumn(clazz, fragmentMap);

      this.generateFilter(clazz, fragmentMap);

      this.generateToSet(clazz, fragmentMap);

      this.generateToString(clazz, fragmentMap);

      fragmentMap.add(Parser.CLASS_END, "}", 1);

      if (clazz.getModified() && fragmentMap.classBodyIsEmpty(fragmentMap))
      {
         Path path = Paths.get(classFileName);
         try
         {
            Files.deleteIfExists(path);
            Logger.getLogger(Generator.class.getName()).info("\n   deleting empty file " + classFileName);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }
      else
      {
         fragmentMap.writeFile();
      }
   }

   private void generatePackageDecl(Clazz clazz, FileFragmentMap fragmentMap)
   {
      String result = String.format("package %s;", clazz.getModel().getPackageName() + ".tables");
      fragmentMap.add(Parser.PACKAGE, result, 2);
   }

   private void generateClassDecl(Clazz clazz, FileFragmentMap fragmentMap)
   {
      STGroup group = this.getSTGroup("templates/classDecl.stg");
      ST st = group.getInstanceOf("classDecl");
      st.add("name", clazz.getName() + "Table");
      st.add("superClass", clazz.getSuperClass() != null ? clazz.getSuperClass().getName() + "Table" : null);
      String result = st.render();
      fragmentMap.add(Parser.CLASS, result, 2);
   }

   private void generateConstructor(Clazz clazz, FileFragmentMap fragmentMap)
   {
      STGroup group = this.getSTGroup("templates/tableConstructor.stg");
      ST st = group.getInstanceOf("constructor");
      st.add("className", clazz.getName());
      String result = st.render();
      fragmentMap.add(Parser.CONSTRUCTOR + ":" + clazz.getName() + "Table(" + clazz.getName() + "...)", result, 2,
                      clazz.getModified());
   }

   private void generateStandardAttributes(Clazz clazz, FileFragmentMap fragmentMap)
   {
      STGroup group = this.getSTGroup("templates/attributes.stg");
      ST attrTemplate;
      String result;

      fragmentMap.add(Parser.IMPORT + ":java.util.ArrayList", "import java.util.ArrayList;", 1);
      fragmentMap.add(Parser.IMPORT + ":java.util.LinkedHashMap", "import java.util.LinkedHashMap;", 1);

      ArrayList<Attribute> standardAttributes = new ArrayList<>();

      standardAttributes.add(new Attribute().setName("table").setType("ArrayList<ArrayList<Object>>")
                                            .setInitialization("new ArrayList<>()"));

      standardAttributes.add(new Attribute().setName("columnName").setType("String").setInitialization("null"));

      standardAttributes.add(new Attribute().setName("columnMap").setType("LinkedHashMap<String, Integer>")
                                            .setInitialization("new LinkedHashMap<>()"));

      for (Attribute attr : standardAttributes)
      {
         attrTemplate = group.getInstanceOf("attrDecl");
         attrTemplate.add("type", attr.getType());
         attrTemplate.add("name", attr.getName());
         attrTemplate.add("value", attr.getInitialization());
         result = attrTemplate.render();

         fragmentMap.add(Parser.ATTRIBUTE + ":" + attr.getName(), result, 2, clazz.getModified());

         attrTemplate = group.getInstanceOf("attrGet");
         attrTemplate.add("type", attr.getType());
         attrTemplate.add("name", attr.getName());
         result = attrTemplate.render();

         fragmentMap.add(Parser.METHOD + ":get" + StrUtil.cap(attr.getName()) + "()", result, 2, attr.getModified());

         attrTemplate = group.getInstanceOf("simpleAttrSet");
         attrTemplate.add("class", clazz.getName() + "Table");
         attrTemplate.add("type", attr.getType());
         attrTemplate.add("name", attr.getName());
         result = attrTemplate.render();

         fragmentMap.add(Parser.METHOD + ":set" + StrUtil.cap(attr.getName()) + "(" + attr.getType() + ")", result, 3,
                         attr.getModified());
      }
   }

   private void generateAttributes(Clazz clazz, FileFragmentMap fragmentMap)
   {
      STGroup group = this.getSTGroup("templates/tablesAttributes.stg");
      ST attrTemplate;
      String result;

      for (Attribute attr : clazz.getAttributes())
      {
         attrTemplate = group.getInstanceOf("expandMethod");
         attrTemplate.add("roleName", attr.getName());
         attrTemplate.add("typeName", attr.getType());
         attrTemplate.add("className", clazz.getName());
         result = attrTemplate.render();

         fragmentMap.add(Parser.METHOD + ":expand" + StrUtil.cap(attr.getName()) + "(String...)", result, 2,
                         attr.getModified());
      }
   }

   private void generateAssociations(Clazz clazz, FileFragmentMap fragmentMap)
   {
      String fullClassName = clazz.getModel().getPackageName() + "." + clazz.getName();
      fragmentMap.add(Parser.IMPORT + ":" + fullClassName, "import " + fullClassName + ";", 1);

      STGroup group = this.getSTGroup("templates/tablesAssociations.stg");
      String result;
      ST st;
      for (AssocRole role : clazz.getRoles())
      {
         if (role.getName() == null)
         {
            continue; //===================================
         }

         String otherClassName = role.getOther().getClazz().getName();

         // getMethod(roleName,toMany,className,otherClassName) ::=
         st = group.getInstanceOf("expandMethod");
         st.add("roleName", role.getName());
         st.add("toMany", role.getCardinality() != Type.ONE);
         st.add("className", clazz.getName());
         st.add("otherClassName", otherClassName);
         result = st.render();
         fragmentMap.add(Parser.METHOD + ":expand" + StrUtil.cap(role.getName()) + "(String...)", result, 2,
                         role.getModified());

         // hasMethod(roleName,toMany,className,otherClassName) ::=
         st = group.getInstanceOf("hasMethod");
         st.add("roleName", role.getName());
         st.add("toMany", role.getCardinality() != Type.ONE);
         st.add("className", clazz.getName());
         st.add("otherClassName", otherClassName);
         result = st.render();
         fragmentMap
            .add(Parser.METHOD + ":has" + StrUtil.cap(role.getName()) + "(" + otherClassName + "Table)", result, 2,
                 role.getModified());

         fullClassName = clazz.getModel().getPackageName() + "." + otherClassName;
         fragmentMap.add(Parser.IMPORT + ":" + fullClassName, "import " + fullClassName + ";", 1);
      }
   }

   private void generateSelectColumns(Clazz clazz, FileFragmentMap fragmentMap)
   {
      fragmentMap.add(Parser.IMPORT + ":java.util.Arrays", "import java.util.Arrays;", 1);

      String result;
      STGroup group = this.getSTGroup("templates/tablesSelectColumns.stg");
      ST st = group.getInstanceOf("selectColumns");
      st.add("className", clazz.getName());
      result = st.render();

      fragmentMap.add(Parser.METHOD + ":selectColumns(String...)", result, 2, clazz.getModified());

      st = group.getInstanceOf("dropColumns");
      st.add("className", clazz.getName());
      result = st.render();

      fragmentMap.add(Parser.METHOD + ":dropColumns(String...)", result, 2, clazz.getModified());
   }

   private void generateAddColumn(Clazz clazz, FileFragmentMap fragmentMap)
   {
      String result;
      STGroup group = this.getSTGroup("templates/tablesSelectColumns.stg");
      ST st = group.getInstanceOf("addColumn");
      st.add("className", clazz.getName());
      result = st.render();

      fragmentMap.add(Parser.METHOD
                      + ":addColumn(String,java.util.function.Function<java.util.LinkedHashMap<String,Object>,Object>)",
                      result, 2, clazz.getModified());
   }

   private void generateFilter(Clazz clazz, FileFragmentMap fragmentMap)
   {
      fragmentMap.add(Parser.IMPORT + ":java.util.function.Predicate", "import java.util.function.Predicate;", 1);

      STGroup group = this.getSTGroup("templates/tablesFilter.stg");
      ST st = group.getInstanceOf("filter");
      st.add("className", clazz.getName());

      boolean modified = clazz.getModified();
      if (clazz.getSuperClass() != null)
      {
         // do not generate filter method
         modified = true;
      }
      fragmentMap.add(Parser.METHOD + ":filter(Predicate< " + clazz.getName() + " >)", st.render(), 2, modified);

      st = group.getInstanceOf("filterRow");
      st.add("className", clazz.getName());

      fragmentMap.add(Parser.METHOD + ":filterRow(Predicate<LinkedHashMap<String,Object> >)", st.render(), 2,
                      clazz.getModified());
   }

   private void generateToSet(Clazz clazz, FileFragmentMap fragmentMap)
   {
      fragmentMap.add(Parser.IMPORT + ":java.util.LinkedHashSet", "import java.util.LinkedHashSet;", 1);

      STGroup group = this.getSTGroup("templates/tablesToSet.stg");
      ST st = group.getInstanceOf("toSet");
      st.add("className", clazz.getName());

      boolean modified = clazz.getModified();
      if (clazz.getSuperClass() != null)
      {
         // do not generate toSet method
         modified = true;
      }

      fragmentMap.add(Parser.METHOD + ":toSet()", st.render(), 2, modified);
   }

   private void generateToString(Clazz clazz, FileFragmentMap fragmentMap)
   {
      STGroup group = this.getSTGroup("templates/tablesToString.stg");
      ST st = group.getInstanceOf("toString");

      fragmentMap.add(Parser.METHOD + ":toString()", st.render(), 2, clazz.getModified());
   }
}
