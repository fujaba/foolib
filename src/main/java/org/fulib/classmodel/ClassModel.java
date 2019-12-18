package org.fulib.classmodel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Objects;

/**
 * <img src='doc-files/classDiagram.png' width='663' alt="doc-files/classDiagram.png">
 */
public class ClassModel
{
   // =============== Constants ===============

   public static final java.util.ArrayList<Clazz> EMPTY_classes = new java.util.ArrayList<Clazz>()
   { @Override public boolean add(Clazz value){ throw new UnsupportedOperationException("No direct add! Use xy.withClasses(obj)"); }};
   public static final String PROPERTY_packageName = "packageName";
   public static final String PROPERTY_mainJavaDir = "mainJavaDir";
   public static final String PROPERTY_defaultRoleType = "defaultRoleType";
   public static final String PROPERTY_defaultPropertyStyle = "defaultPropertyStyle";
   public static final String PROPERTY_classes = "classes";

   // =============== Fields ===============

   protected PropertyChangeSupport listeners = null;

   private java.util.ArrayList<Clazz> classes = null;

   private String mainJavaDir;
   private String packageName;
   private String defaultRoleType;
   private String defaultPropertyStyle = "POJO";

   // =============== Properties ===============

   public String getPackageSrcFolder()
   {
      return this.getMainJavaDir() + "/" + this.getPackageName().replaceAll("\\.", "/");
   }

   public String getMainJavaDir()
   {
      return this.mainJavaDir;
   }

   public ClassModel setMainJavaDir(String value)
   {
      if (Objects.equals(value, this.mainJavaDir))
      {
         return this;
      }

      final String oldValue = this.mainJavaDir;
      this.mainJavaDir = value;
      this.firePropertyChange("mainJavaDir", oldValue, value);
      return this;
   }

   public String getPackageName()
   {
      return this.packageName;
   }

   public ClassModel setPackageName(String value)
   {
      if (Objects.equals(value, this.packageName))
      {
         return this;
      }

      final String oldValue = this.packageName;
      this.packageName = value;
      this.firePropertyChange("packageName", oldValue, value);
      return this;
   }

   public String getDefaultRoleType()
   {
      return this.defaultRoleType;
   }

   public ClassModel setDefaultRoleType(String value)
   {
      if (Objects.equals(value, this.defaultRoleType))
      {
         return this;
      }

      final String oldValue = this.defaultRoleType;
      this.defaultRoleType = value;
      this.firePropertyChange("defaultRoleType", oldValue, value);
      return this;
   }

   public String getDefaultPropertyStyle()
   {
      return this.defaultPropertyStyle;
   }

   public ClassModel setDefaultPropertyStyle(String value)
   {
      if (Objects.equals(value, this.defaultPropertyStyle))
      {
         return this;
      }

      final String oldValue = this.defaultPropertyStyle;
      this.defaultPropertyStyle = value;
      this.firePropertyChange("defaultPropertyStyle", oldValue, value);
      return this;
   }

   public Clazz getClazz(String name)
   {
      for (Clazz clazz : this.getClasses())
      {
         if (Objects.equals(clazz.getName(), name))
         {
            return clazz;
         }
      }
      return null;
   }

   public java.util.ArrayList<Clazz> getClasses()
   {
      return this.classes != null ? this.classes : EMPTY_classes;
   }

   public ClassModel withClasses(Object... value)
   {
      if (value == null)
      {
         return this;
      }
      for (Object item : value)
      {
         if (item == null)
         {
            continue;
         }
         if (item instanceof Collection)
         {
            this.withClasses(((Collection<?>) item).toArray());
         }
         else if (item instanceof Clazz)
         {
            if (this.classes == null)
            {
               this.classes = new java.util.ArrayList<Clazz>();
            }
            if (!this.classes.contains(item))
            {
               this.classes.add((Clazz)item);
               ((Clazz)item).setModel(this);
               this.firePropertyChange("classes", null, item);
            }
         }
         else
         {
            throw new IllegalArgumentException();
         }
      }
      return this;
   }

   public ClassModel withoutClasses(Object... value)
   {
      if (this.classes == null || value == null)
      {
         return this;
      }
      for (Object item : value)
      {
         if (item == null)
         {
            continue;
         }
         if (item instanceof Collection)
         {
            this.withoutClasses(((Collection<?>) item).toArray());
         }
         else if (item instanceof Clazz)
         {
            if (this.classes.remove(item))
            {
               ((Clazz)item).setModel(null);
               this.firePropertyChange("classes", item, null);
            }
         }
      }
      return this;
   }

   // =============== Methods ===============

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

   public void removeYou()
   {
      this.withoutClasses(this.getClasses().clone());
   }

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getPackageName());
      result.append(' ').append(this.getMainJavaDir());
      result.append(' ').append(this.getDefaultRoleType());
      result.append(' ').append(this.getDefaultPropertyStyle());
      return result.substring(1);
   }
}
