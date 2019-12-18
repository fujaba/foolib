package org.fulib.classmodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileFragmentMap
{
   // =============== Constants ===============

   public static final String CLASS       = "class";
   public static final String PACKAGE     = "package";
   public static final String CONSTRUCTOR = "constructor";
   public static final String ATTRIBUTE   = "attribute";
   public static final String METHOD      = "method";
   public static final String IMPORT      = "import";
   public static final String CLASS_BODY  = "classBody";
   public static final String CLASS_END   = "classEnd";
   public static final String GAP         = "gap:";

   public static final String PROPERTY_fileName = "fileName";

   // =============== Fields ===============

   protected PropertyChangeSupport listeners = null;

   private String fileName;

   private ArrayList<CodeFragment> fragmentList = new ArrayList<>();

   private LinkedHashMap<String, CodeFragment> codeMap = new LinkedHashMap<>();

   // =============== Constructors ===============

   public FileFragmentMap()
   {
      CodeFragment startFragment = new CodeFragment().setKey("start:").setText("");
      this.fragmentList.add(startFragment);
   }

   public FileFragmentMap(String fileName)
   {
      this.setFileName(fileName);
   }

   // =============== Properties ===============

   public String getFileName()
   {
      return this.fileName;
   }

   public FileFragmentMap setFileName(String value)
   {
      if (Objects.equals(value, this.fileName))
      {
         return this;
      }

      final String oldValue = this.fileName;
      this.fileName = value;
      this.firePropertyChange("fileName", oldValue, value);
      return this;
   }

   public ArrayList<CodeFragment> getFragmentList()
   {
      return this.fragmentList;
   }

   public CodeFragment getFragment(String key)
   {
      return this.codeMap.get(key);
   }

   // =============== Static Methods ===============

   public static String mergeClassDecl(String oldText, String newText)
   {
      // keep annotations and implements clause "\\s*public\\s+class\\s+(\\w+)(\\.+)\\{"
      final Pattern pattern = Pattern.compile("class\\s+(\\w+)\\s*(extends\\s+[^\\s]+)?");
      final Matcher match = pattern.matcher(newText);

      if (!match.find())
      {
         // TODO error?
         return newText;
      }

      final String className = match.group(1);
      final String extendsClause = match.group(2);

      final int oldClassNamePos = oldText.indexOf("class " + className);
      if (oldClassNamePos < 0)
      {
         // TODO error?
         return newText;
      }

      final StringBuilder newTextBuilder = new StringBuilder();

      // prefix
      newTextBuilder.append(oldText, 0, oldClassNamePos);

      // middle
      newTextBuilder.append("class ").append(className);
      if (extendsClause != null)
      {
         newTextBuilder.append(" ").append(extendsClause);
      }

      // suffix
      final int implementsPos = oldText.indexOf("implements");
      if (implementsPos >= 0)
      {
         newTextBuilder.append(" ").append(oldText, implementsPos, oldText.length());
      }
      else
      {
         newTextBuilder.append("\n{");
      }

      return newTextBuilder.toString();
   }

   // =============== Methods ===============

   public CodeFragment add(String key, String newText, int newLines)
   {
      return this.add(key, newText, newLines, false);
   }

   public CodeFragment add(String key, String newText, int newLines, boolean removeFragment)
   {

      CodeFragment result = this.codeMap.get(key);

      if (result != null)
      {
         // TODO this also inspects method bodies. Perhaps we don't want that?
         if (result.getText().contains("// no"))
         {
            // do not overwrite
            return result;
         }

         if (removeFragment)
         {
            this.codeMap.remove(key);
            int pos = this.fragmentList.indexOf(result);
            this.fragmentList.remove(pos);
            CodeFragment gap = this.fragmentList.get(pos - 1);
            if (Objects.equals(gap.getKey(), GAP))
            {
               this.fragmentList.remove(pos - 1);
            }
            return result;
         }

         // keep annotations and modifiers
         if (newText.contains("@"))
         {
            // newtext contains annotations, thus it overrides annotations in the code
            // do not modify newtext
         }
         else if (key.equals(CLASS))
         {
            newText = mergeClassDecl(result.getText(), newText);
         }
         else if (key.startsWith(ATTRIBUTE))
         {
            // keep everything before public
            int newTextPublicPos = newText.indexOf("public");
            int resultPublicPos = result.getText().indexOf("public");
            if (newTextPublicPos >= 0 && resultPublicPos >= 0)
            {
               newText = result.getText().substring(0, resultPublicPos) + newText.substring(newTextPublicPos);
            }
         }
         else if (key.startsWith(ATTRIBUTE)) // ToDo: this looks wrong, remove it?
         {
            // keep everything before private
            int newTextPrivatePos = newText.indexOf("private");
            int resultPrivatePos = result.getText().indexOf("private");
            if (newTextPrivatePos >= 0 && resultPrivatePos >= 0)
            {
               newText = result.getText().substring(0, resultPrivatePos) + newText.substring(newTextPrivatePos);
            }
         }

         result.setText(newText.trim());

         return result;
      }

      if (removeFragment)
      {
         return null;
      }

      result = new CodeFragment().setKey(key).setText(newText);
      this.codeMap.put(key, result);
      CodeFragment gap = this.getNewLineGapFragment(newLines);

      if (key.startsWith(ATTRIBUTE) || key.startsWith(METHOD) || key.startsWith(CONSTRUCTOR))
      {
         this.add(result, CLASS_END);

         this.add(gap, CLASS_END);

         return result;
      }

      if (key.startsWith(IMPORT))
      {
         CodeFragment oldFragment = this.codeMap.get(CLASS);
         int pos = this.fragmentList.indexOf(oldFragment);

         // go to the gap before this
         pos--;

         pos = Math.max(0, pos);

         this.fragmentList.add(pos, gap);
         pos++;
         //         fragmentList.add(pos, gap);
         //         pos++;
         this.fragmentList.add(pos, result);

         return result;
      }

      this.add(result);
      this.add(gap, CLASS_END);

      return result;
   }

   private CodeFragment getNewLineGapFragment(int newLines)
   {
      CodeFragment gap = new CodeFragment().setKey("gap:");

      StringBuilder text = new StringBuilder();
      for (int i = 0; i < newLines; i++)
      {
         text.append("\n");
      }

      gap.setText(text.toString());
      return gap;
   }

   private void add(CodeFragment result, String posKey)
   {
      CodeFragment oldFragment = this.codeMap.get(posKey);
      int pos = this.fragmentList.indexOf(oldFragment);
      if (pos == -1)
      {
         this.fragmentList.add(result);
      }
      else
      {
         this.fragmentList.add(pos, result);
      }

      this.codeMap.put(result.getKey(), result);
   }

   public void add(CodeFragment fragment)
   {
      this.fragmentList.add(fragment);
      this.codeMap.put(fragment.getKey(), fragment);
   }

   public void writeFile()
   {
      final Path path = Paths.get(this.fileName);
      try
      {
         Files.createDirectories(path.getParent());

         try (final Writer writer = Files
            .newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
         {
            this.write(writer);
         }
      }
      catch (IOException e)
      {
         // TODO better error handling
         e.printStackTrace();
      }
   }

   public void write(Writer writer) throws IOException
   {
      for (final CodeFragment fragment : this.fragmentList)
      {
         writer.write(fragment.getText());
      }
   }

   @Deprecated
   @SuppressWarnings("unused")
   public boolean classBodyIsEmpty(FileFragmentMap fragmentMap)
   {
      return this.isClassBodyEmpty();
   }

   public boolean isClassBodyEmpty()
   {
      final CodeFragment startFragment = this.codeMap.get(CLASS);
      final CodeFragment endFragment = this.codeMap.get(CLASS_END);

      if (startFragment == null || endFragment == null)
      {
         return true;
      }

      final int startPos = this.fragmentList.indexOf(startFragment) + 1;
      final int endPos = this.fragmentList.lastIndexOf(endFragment);

      for (int i = startPos; i < endPos; i++)
      {
         CodeFragment fragment = this.fragmentList.get(i);
         if (!Objects.equals(fragment.getKey(), GAP))
         {
            return false;
         }
      }

      return true;
   }

   // --------------- Property Change Support ---------------

   public boolean addPropertyChangeListener(PropertyChangeListener listener)
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      this.listeners.addPropertyChangeListener(listener);
      return true;
   }

   public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      this.listeners.addPropertyChangeListener(propertyName, listener);
      return true;
   }

   public boolean removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (this.listeners != null)
      {
         this.listeners.removePropertyChangeListener(listener);
      }
      return true;
   }

   public boolean removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
   {
      if (this.listeners != null)
      {
         this.listeners.removePropertyChangeListener(propertyName, listener);
      }
      return true;
   }

   public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

   // --------------- Misc. ---------------

   public void removeYou()
   {
   }

   @Override // no fulib
   public String toString()
   {
      final StringBuilder result = new StringBuilder();

      result.append(this.getFileName()).append('\n');
      for (final CodeFragment fragment : this.fragmentList)
      {
         result.append(fragment.getText());
      }

      return result.toString();
   }
}
