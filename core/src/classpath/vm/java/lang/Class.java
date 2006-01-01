/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package java.lang;

import gnu.java.lang.VMClassHelper;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URL;
import java.security.AllPermission;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.jnode.security.JNodePermission;
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.VmReflection;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * Class. If you change any fields in this class, also change
 * <code>emitClass</code> in <code>org.jnode.build.ObjectEmitter</code>.
 * 
 * @author epr
 */
public final class Class<T> implements AnnotatedElement, Serializable, Type,
        GenericDeclaration {

    /**
     * Compatible with JDK 1.0+.
     */
    private static final long serialVersionUID = 3206093459760846163L;

    /**
     * Permission used in {@link #getVmClass()}
     */
    private static final JNodePermission GETVMCLASS = new JNodePermission(
            "getVmClass");

    private final VmType<T> vmClass;

    private Constructor[] declaredConstructors;

    private Field[] declaredFields;

    private Method[] declaredMethods;

    private ArrayList<Field> fields;

    private ArrayList<Method> methods;

    private ArrayList<Class> interfaces;

    private ArrayList<Constructor> constructors;

    private VmMethod defaultConstructor;

    private String name;

    /** The unknown protection domain. */
    private static ProtectionDomain unknownProtectionDomain;

    /**
     * Create a new instance. This constructor can be public, because the
     * creation of VmClass instances of already protected.
     * 
     * @param vmClass
     */
    public Class(VmType<T> vmClass) {
        if (vmClass == null) {
            throw new IllegalArgumentException("vmClass cannot be null");
        }
        this.vmClass = vmClass;
    }

    public static Class forName(String className) throws ClassNotFoundException {
        // System.out.println("Class.forName [" + className + "]");

        return VmSystem.forName(className);
    }

    public static Class forName(String className, boolean initialize,
            ClassLoader loader) throws ClassNotFoundException {
        return VmSystem.forName(className);
    }

    /**
     * Converts this object to its String representation
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return (isInterface() ? "interface " : "class ") + getName();
    }

    /**
     * Returns the desired assertion status of this class, if it were to be
     * initialized at this moment. The class assertion status, if set, is
     * returned; the backup is the default package status; then if there is a
     * class loader, that default is returned; and finally the system default is
     * returned. This method seldom needs calling in user code, but exists for
     * compilers to implement the assert statement. Note that there is no
     * guarantee that the result of this method matches the class's actual
     * assertion status.
     * 
     * @return the desired assertion status
     * @see ClassLoader#setClassAssertionStatus(String, boolean)
     * @see ClassLoader#setPackageAssertionStatus(String, boolean)
     * @see ClassLoader#setDefaultAssertionStatus(boolean)
     * @since 1.4
     */
    public boolean desiredAssertionStatus() {
        ClassLoader c = getClassLoader();
        Object status;
        if (c == null)
            return VMClassLoader.defaultAssertionStatus();
        if (c.classAssertionStatus != null)
            synchronized (c) {
                status = c.classAssertionStatus.get(getName());
                if (status != null) {
                    return status.equals(Boolean.TRUE);
                }
            }
        else {
            if (ClassLoader.StaticData.systemClassAssertionStatus == null) {
                throw new Error("systClassAssertionStatus == null");
            }
            status = ClassLoader.StaticData.systemClassAssertionStatus
                    .get(getName());
            if (status != null) {
                return status.equals(Boolean.TRUE);
            }
        }
        if (c.packageAssertionStatus != null)
            synchronized (c) {
                String name = getPackagePortion(getName());
                if ("".equals(name))
                    status = c.packageAssertionStatus.get(null);
                else
                    do {
                        status = c.packageAssertionStatus.get(name);
                        name = getPackagePortion(name);
                    } while (!"".equals(name) && status == null);
                if (status != null)
                    return status.equals(Boolean.TRUE);
            }
        else {
            String name = getPackagePortion(getName());
            if ("".equals(name))
                status = ClassLoader.StaticData.systemPackageAssertionStatus
                        .get(null);
            else
                do {
                    status = ClassLoader.StaticData.systemPackageAssertionStatus
                            .get(name);
                    name = getPackagePortion(name);
                } while (!"".equals(name) && status == null);
            if (status != null)
                return status.equals(Boolean.TRUE);
        }
        return c.defaultAssertionStatus;
    }

    /**
     * Gets the name of this class
     * 
     * @return String
     */
    public String getName() {
        if (name == null) {
            name = vmClass.getName().replace('/', '.');
        }
        return name;
    }

    /**
     * Is this class an interface?
     * 
     * @return boolean
     */
    public boolean isInterface() {
        return vmClass.isInterface();
    }

    /**
     * Gets the Class this class extends, or null if this class is
     * <code>java.lang. Object</code>
     * 
     * @return Class
     */
    public final Class< ? super T> getSuperclass() {
        VmType< ? super T> superCls = getLinkedVmClass().getSuperClass();
        if (superCls != null) {
            return superCls.asClass();
        } else {
            return null;
        }
    }

    /**
     * Gets the signers of this class.
     * 
     * @return
     */
    public Object[] getSigners() {
        // TODO implement me
        return null;
    }

    /**
     * Determines the interfaces implemented by the class or interface
     * represented by this object.
     * 
     * @return Class[]
     */
    public final Class[] getInterfaces() {
        if (interfaces == null) {
            final ArrayList<Class> list = new ArrayList<Class>();
            final VmType<T> vmClass = getLinkedVmClass();
            final int cnt = vmClass.getNoInterfaces();
            for (int i = 0; i < cnt; i++) {
                list.add(vmClass.getInterface(i).asClass());
            }
            interfaces = list;
        }
        return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
    }

    /**
     * Is the given object instanceof this class.
     * 
     * @param object
     * @return boolean
     */
    public boolean isInstance(Object object) {
        return SoftByteCodes.isInstanceof(object, getLinkedVmClass());
    }

    /**
     * Discover whether an instance of the Class parameter would be an instance
     * of this Class as well. Think of doing
     * <code>isInstance(c.newInstance())</code> or even
     * <code>c.newInstance() instanceof (this class)</code>. While this
     * checks widening conversions for objects, it must be exact for primitive
     * types.
     * 
     * @param c
     *            the class to check
     * @return whether an instance of c would be an instance of this class as
     *         well
     * @throws NullPointerException
     *             if c is null
     * @since 1.1
     */
    public boolean isAssignableFrom(Class< ? > c) {
        return getLinkedVmClass().isAssignableFrom(c.getLinkedVmClass());
    }

    /**
     * Gets the runtime visible annotations of this class.
     */
    public Annotation[] getAnnotations() {
        return vmClass.getAnnotations();
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getAnnotation(java.lang.Class)
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return vmClass.getAnnotation(annotationClass);
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#getDeclaredAnnotations()
     */
    public Annotation[] getDeclaredAnnotations() {
        return vmClass.getDeclaredAnnotations();
    }

    /**
     * @see java.lang.reflect.AnnotatedElement#isAnnotationPresent(java.lang.Class)
     */
    public boolean isAnnotationPresent(
            Class< ? extends Annotation> annotationClass) {
        return vmClass.isAnnotationPresent(annotationClass);
    }

    /**
     * Gets the classloader used to load this class.
     * 
     * @return ClassLoader
     */
    public final ClassLoader getClassLoader() {
        return vmClass.getLoader().asClassLoader();
    }

    /**
     * Create a new instance of this class, using the default constructor
     * 
     * @return Object
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public final Object newInstance() throws InstantiationException,
            IllegalAccessException {
        if (defaultConstructor == null) {
            defaultConstructor = getLinkedVmClass().getDeclaredMethod("<init>",
                    "()V");
        }
        if (defaultConstructor == null) {
            throw new InstantiationException("No default constructor");
        }
        try {
            return VmReflection.newInstance(defaultConstructor);
        } catch (InvocationTargetException ex) {
            final InstantiationException ie = new InstantiationException();
            ie.initCause(ex);
            throw ie;
        }
    }

    /**
     * Gets the modifiers of this class
     * 
     * @return int
     */
    public final int getModifiers() {
        return vmClass.getAccessFlags();
    }

    /**
     * Gets the field with the given name that is declared in this class or any
     * of my super-classes.
     * 
     * @param name
     * @return Field
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    public Field getField(String name) throws NoSuchFieldException,
            SecurityException {
        VmField f = getLinkedVmClass().getField(name);
        if (f != null) {
            return f.asField();
        } else {
            throw new NoSuchFieldException(name);
        }
    }

    /**
     * Gets all fields declared in this class and all of its super-classes.
     * 
     * @return Field[]
     */
    public Field[] getFields() {
        if (fields == null) {
            ArrayList<Field> list = new ArrayList<Field>();
            Class< ? > cls = this;
            while (cls != null) {
                final Field[] dlist = cls.getDeclaredFields();
                for (int i = 0; i < dlist.length; i++) {
                    list.add(dlist[i]);
                }
                cls = cls.getSuperclass();
            }
            fields = list;
        }
        return (Field[]) fields.toArray(new Field[fields.size()]);
    }

    /**
     * Gets the field with the given name that is declared in this class.
     * 
     * @param name
     * @return Field
     * @throws NoSuchFieldException
     * @throws SecurityException
     */
    public Field getDeclaredField(String name) throws NoSuchFieldException,
            SecurityException {
        VmField f = getLinkedVmClass().getDeclaredField(name);
        if (f != null) {
            return f.asField();
        } else {
            throw new NoSuchFieldException(name);
        }
    }

    /**
     * Gets all fields declared in this class
     * 
     * @return Field[]
     */
    public Field[] getDeclaredFields() {
        if (declaredFields == null) {
            final VmType<T> vmClass = getLinkedVmClass();
            final int cnt = vmClass.getNoDeclaredFields();
            final Field[] list = new Field[cnt];
            for (int i = 0; i < cnt; i++) {
                list[i] = vmClass.getDeclaredField(i).asField();
            }
            declaredFields = list;
        }
        return declaredFields;
    }

    /**
     * Is this class a primitive class?
     * 
     * @return boolean
     */
    public boolean isPrimitive() {
        return vmClass.isPrimitive();
    }

    /**
     * Return the class of my components (if this class is an array)
     * 
     * @return Class
     */
    public Class getComponentType() {
        final VmType<T> vmClass = getLinkedVmClass();
        if (vmClass instanceof VmArrayClass) {
            final VmType< ? > vmCompType = ((VmArrayClass<T>) vmClass)
                    .getComponentType();
            if (vmCompType != null) {
                return vmCompType.asClass();
            }
        }
        return null;
    }

    /**
     * Gets the method with the given name and argument types declared in this
     * class or any of its super-classes.
     * 
     * @param name
     * @param argTypes
     * @return Method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public Method getMethod(String name, Class< ? >[] argTypes)
            throws NoSuchMethodException, SecurityException {
        VmType< ? >[] vmArgTypes;
        if (argTypes == null) {
            vmArgTypes = null;
        } else {
            final int cnt = argTypes.length;
            vmArgTypes = new VmType[cnt];
            for (int i = 0; i < cnt; i++) {
                vmArgTypes[i] = argTypes[i].getLinkedVmClass();
            }
        }
        VmMethod method = getLinkedVmClass().getMethod(name, vmArgTypes);
        if (method != null) {
            return (Method) method.asMember();
        } else {
            throw new NoSuchMethodException(name);
        }
    }

    /**
     * Gets all methods declared in this class and its super-classes
     * 
     * @return Method[]
     */
    public Method[] getMethods() {
        if (methods == null) {
            final ArrayList<Method> list = new ArrayList<Method>();
            Class< ? > cls = this;
            while (cls != null) {
                final Method[] dlist = cls.getDeclaredMethods();
                for (int i = 0; i < dlist.length; i++) {
                    list.add(dlist[i]);
                }
                cls = cls.getSuperclass();
            }
            methods = list;
        }
        return (Method[]) methods.toArray(new Method[methods.size()]);
    }

    /**
     * Gets the method with the given name and argument types declared in this
     * class.
     * 
     * @param name
     * @param argTypes
     * @return Method
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    public Method getDeclaredMethod(String name, Class< ? >[] argTypes)
            throws NoSuchMethodException, SecurityException {
        VmType< ? >[] vmArgTypes;
        if (argTypes == null) {
            vmArgTypes = null;
        } else {
            final int cnt = argTypes.length;
            vmArgTypes = new VmType[cnt];
            for (int i = 0; i < cnt; i++) {
                vmArgTypes[i] = argTypes[i].getLinkedVmClass();
            }
        }
        VmMethod method = getLinkedVmClass()
                .getDeclaredMethod(name, vmArgTypes);
        if (method != null) {
            return (Method) method.asMember();
        } else {
            throw new NoSuchMethodException(name);
        }
    }

    /**
     * Gets all methods declared in this class.
     * 
     * @return Method[]
     */
    public Method[] getDeclaredMethods() {
        if (declaredMethods == null) {
            final VmType<T> vmClass = getLinkedVmClass();
            final int cnt = vmClass.getNoDeclaredMethods();
            int max = 0;
            for (int i = 0; i < cnt; i++) {
                if (!vmClass.getDeclaredMethod(i).isConstructor()) {
                    max++;
                }
            }
            final Method[] list = new Method[max];
            max = 0;
            for (int i = 0; i < cnt; i++) {
                VmMethod vmMethod = vmClass.getDeclaredMethod(i);
                if (!vmMethod.isConstructor()) {
                    list[max++] = (Method) vmMethod.asMember();
                }
            }
            declaredMethods = list;
        }
        return declaredMethods;
    }

    /**
     * Returns an array of all public classes and interfaces that are members of
     * this class and declared in this class.
     * 
     * @return
     */
    public Class[] getDeclaredClasses() throws SecurityException {
        // TODO implement me
        return new Class[0];
    }

    /**
     * If the class or interface represented by this Class object is a member of
     * another class, returns the Class object representing the class in which
     * it was declared. This method returns null if this class or interface is
     * not a member of any other class. If this Class object represents an array
     * class, a primitive type, or void,then this method returns null.
     * 
     * @return
     */
    public Class getDeclaringClass() {
        // TODO implement me
        return null;
    }

    public Constructor getDeclaredConstructor(Class[] argTypes)
            throws NoSuchMethodException {
        String signature = Signature.toSignature(null, argTypes);
        final VmMethod vmMethod = getLinkedVmClass().getDeclaredMethod(
                "<init>", signature);
        if (vmMethod != null) {
            return (Constructor) vmMethod.asMember();
        } else {
            throw new NoSuchMethodException("<init> " + signature);
        }
    }

    /**
     * Gets all constructors declared in this class
     * 
     * @return Constructor[]
     */
    public Constructor[] getDeclaredConstructors() {
        if (declaredConstructors == null) {
            final VmType<T> vmClass = getLinkedVmClass();
            int cnt = vmClass.getNoDeclaredMethods();
            int max = 0;
            for (int i = 0; i < cnt; i++) {
                if (vmClass.getDeclaredMethod(i).isConstructor()) {
                    max++;
                }
            }
            Constructor[] list = new Constructor[max];
            max = 0;
            for (int i = 0; i < cnt; i++) {
                VmMethod vmMethod = vmClass.getDeclaredMethod(i);
                if (vmMethod.isConstructor()) {
                    list[max++] = (Constructor) vmMethod.asMember();
                }
            }
            declaredConstructors = list;
        }
        return declaredConstructors;
    }

    /**
     * Returns the <code>Package</code> in which this class is defined Returns
     * null when this information is not available from the classloader of this
     * class or when the classloader of this class is null.
     * 
     * @return the package for this class, if it is available
     * @since 1.2
     */
    public Package getPackage() {
        ClassLoader cl = getClassLoader();
        if (cl != null) {
            return cl.getPackage(getPackagePortion(getName()));
        }
        return null;
    }

    /**
     * Returns the ProtectionDomain of this class. If there is a security
     * manager installed, this method first calls the security manager's
     * checkPermission method with a RuntimePermission("getProtectionDomain")
     * permission to ensure it's ok to get the ProtectionDomain.
     * 
     * @return the ProtectionDomain of this class
     * @throws SecurityException
     *             if a security manager exists and its checkPermission method
     *             doesn't allow getting the ProtectionDomain.
     * @since 1.2
     */
    public ProtectionDomain getProtectionDomain() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getProtectionDomain"));
        }
        final ProtectionDomain pd = getLinkedVmClass().getProtectionDomain();
        if (pd != null) {
            return pd;
        } else {
            return getUnknownProtectionDomain();
        }
    }

    /**
     * Gets the unknown protection domain. Create on demand.
     * 
     * @return
     */
    private static final ProtectionDomain getUnknownProtectionDomain() {
        if (unknownProtectionDomain == null) {
            Permissions permissions = new Permissions();
            permissions.add(new AllPermission());
            unknownProtectionDomain = new ProtectionDomain(null, permissions);
        }
        return unknownProtectionDomain;
    }

    /**
     * Is this class an array class?
     * 
     * @return boolean
     */
    public boolean isArray() {
        return vmClass.isArray();
    }

    /**
     * Returns an array of all public classes and interfaces that are members of
     * this class.
     * 
     * @return
     */
    public Class[] getClasses() throws SecurityException {
        // TODO implement me
        return new Class[0];
    }

    /**
     * Gets the constructor with the given argument types.
     * 
     * @param argTypes
     * @return Constructor
     * @throws NoSuchMethodException
     */
    public Constructor getConstructor(Class[] argTypes)
            throws NoSuchMethodException {
        // Check security
        memberAccessCheck(Member.PUBLIC);

        // Create signature
        String signature = Signature.toSignature(null, argTypes);
        final VmMethod vmMethod = getLinkedVmClass().getDeclaredMethod(
                "<init>", signature);
        if (vmMethod != null) {
            return (Constructor) vmMethod.asMember();
        } else {
            throw new NoSuchMethodException("<init> " + signature);
        }
    }

    /**
     * Returns an array containing Constructor objects reflecting all the public
     * constructors of the class represented by this Class object. An array of
     * length 0 is returned if the class has no public constructors, or if the
     * class is an array class, or if the class reflects a primitive type or
     * void. If there is a security manager, this method first calls the
     * security manager's checkMemberAccess method with this and Member.PUBLIC
     * as its arguments. If the class is in a package, then this method also
     * calls the security manager's checkPackageAccess method with the package
     * name as its argument. Either of these calls could result in a
     * SecurityException.
     * 
     * @return Constructor[]
     */
    public Constructor[] getConstructors() {
        if (constructors == null) {
            ArrayList<Constructor> list = new ArrayList<Constructor>();
            final Constructor[] dlist = getDeclaredConstructors();
            for (int i = 0; i < dlist.length; i++) {
                final Constructor c = dlist[i];
                if ((c.getModifiers() & Modifier.PUBLIC) != 0) {
                    list.add(dlist[i]);
                }
            }
            constructors = list;
        }
        return (Constructor[]) constructors
                .toArray(new Constructor[constructors.size()]);
    }

    /**
     * Get a resource URL using this class's package using the
     * getClassLoader().getResource() method. If this class was loaded using the
     * system classloader, ClassLoader.getSystemResource() is used instead.
     * <p>
     * If the name you supply is absolute (it starts with a <code>/</code>),
     * then it is passed on to getResource() as is. If it is relative, the
     * package name is prepended, and <code>.</code>'s are replaced with
     * <code>/</code>.
     * <p>
     * The URL returned is system- and classloader-dependent, and could change
     * across implementations.
     * 
     * @param name
     *            the name of the resource, generally a path
     * @return the URL to the resource
     * @throws NullPointerException
     *             if name is null
     * @since 1.1
     */
    public URL getResource(String name) {
        if (name.length() > 0 && name.charAt(0) != '/') {
            name = VMClassHelper.getPackagePortion(getName()).replace('.', '/')
                    + "/" + name;
        }
        final ClassLoader ld = getClassLoader();
        if (ld != null) {
            return ld.getResource(name);
        } else {
            return ClassLoader.getSystemResource(name);
        }
    }

    /**
     * Get a resource using this class's package using the
     * getClassLoader().getResourceAsStream() method. If this class was loaded
     * using the system classloader, ClassLoader.getSystemResource() is used
     * instead.
     * <p>
     * If the name you supply is absolute (it starts with a <code>/</code>),
     * then it is passed on to getResource() as is. If it is relative, the
     * package name is prepended, and <code>.</code>'s are replaced with
     * <code>/</code>.
     * <p>
     * The URL returned is system- and classloader-dependent, and could change
     * across implementations.
     * 
     * @param name
     *            the name of the resource, generally a path
     * @return an InputStream with the contents of the resource in it, or null
     * @throws NullPointerException
     *             if name is null
     * @since 1.1
     */
    public InputStream getResourceAsStream(String name) {
        if (name.length() > 0 && name.charAt(0) != '/') {
            name = VMClassHelper.getPackagePortion(getName()).replace('.', '/')
                    + "/" + name;
        }
        final ClassLoader ld = getClassLoader();
        if (ld != null) {
            return ld.getResourceAsStream(name);
        } else {
            return ClassLoader.getSystemResourceAsStream(name);
        }
    }

    /**
     * Gets the JNode VmType (internal) representation of this class. If there
     * is a security manager installed, this method first calls the security
     * manager's checkPermission method with a RuntimePermission("getVmClass")
     * permission to ensure it's ok to get the internal representation.
     * 
     * @return the JNode internal representation of this class.
     */
    public final VmType<T> getVmClass() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(GETVMCLASS);
        }
        return vmClass;
    }

    /**
     * Gets a primitive class of a given type.
     * 
     * @param type
     * @return
     * @see VmType#getPrimitiveClass(char)
     */
    static Class getPrimitiveClass(char type) {
        return VmType.getPrimitiveClass(type).asClass();
    }

    /**
     * Returns the enumeration constants of this class, or null if this class is
     * not an <code>Enum</code>.
     * 
     * @return an array of <code>Enum</code> constants associated with this
     *         class, or null if this class is not an <code>enum</code>.
     */
    @SuppressWarnings("unchecked")
    public T[] getEnumConstants() {
        if (isEnum()) {
            try {
                return (T[]) getMethod("values", null).invoke(null, null);
            } catch (NoSuchMethodException exception) {
                throw new Error("Enum lacks values() method");
            } catch (IllegalAccessException exception) {
                throw new Error("Unable to access Enum class");
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(
                        "The values method threw an exception", exception);
            }
        } else {
            return null;
        }
    }

    /**
     * Returns true if this class is an <code>Enum</code>.
     * 
     * @return true if this is an enumeration class.
     */
    public boolean isEnum() {
        return vmClass.isEnum();
    }

    /**
     * Return object, cast to this Class' type.
     * 
     * @param obj
     *            the object to cast
     * @throws ClassCastException
     *             if obj is not an instance of this class
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        if ((obj != null) && !isInstance(obj)) {
            throw new ClassCastException();
        }
        return (T) obj;
    }

    /**
     * Gets the VmType and make sure it is linked.
     * 
     * @return
     */
    private final VmType<T> getLinkedVmClass() {
        vmClass.link();
        return vmClass;
    }

    /**
     * @see java.lang.reflect.GenericDeclaration#getTypeParameters()
     */
    public TypeVariable< ? >[] getTypeParameters() {
        return new TypeVariable[0];
    }

    /**
     * <p>
     * Casts this class to represent a subclass of the specified class. This
     * method is useful for `narrowing' the type of a class so that the class
     * object, and instances of that class, can match the contract of a more
     * restrictive method. For example, if this class has the static type of
     * <code>Class&lt;Object&gt;</code>, and a dynamic type of
     * <code>Class&lt;Rectangle&gt;</code>, then, assuming <code>Shape</code>
     * is a superclass of <code>Rectangle</code>, this method can be used on
     * this class with the parameter, <code>Class&lt;Shape&gt;</code>, to
     * retain the same instance but with the type
     * <code>Class&lt;? extends Shape&gt;</code>.
     * </p>
     * <p>
     * If this class can be converted to an instance which is parameterised over
     * a subtype of the supplied type, <code>U</code>, then this method
     * returns an appropriately cast reference to this object. Otherwise, a
     * <code>ClassCastException</code> is thrown.
     * </p>
     * 
     * @param klass
     *            the class object, the parameterized type (<code>U</code>)
     *            of which should be a superclass of the parameterized type of
     *            this instance.
     * @return a reference to this object, appropriately cast.
     * @throws ClassCastException
     *             if this class can not be converted to one which represents a
     *             subclass of the specified type, <code>U</code>.
     * @since 1.5
     */
    @SuppressWarnings("unchecked")
    public <U> Class< ? extends U> asSubclass(Class<U> klass) {
        if (!klass.isAssignableFrom(this))
            throw new ClassCastException();
        return (Class< ? extends U>) this;
    }

    /**
     * Perform security checks common to all of the methods that get members of
     * this Class.
     */
    private void memberAccessCheck(int which) {
        SecurityManager sm = SecurityManager.current;
        if (sm != null) {
            sm.checkMemberAccess(this, which);
            Package pkg = getPackage();
            if (pkg != null) {
                sm.checkPackageAccess(pkg.getName());
            }
        }
    }

    /**
     * Strip the last portion of the name (after the last dot).
     * 
     * @param name
     *            the name to get package of
     * @return the package name, or "" if no package
     */
    private static String getPackagePortion(String name) {
        int lastInd = name.lastIndexOf('.');
        if (lastInd == -1)
            return "";
        return name.substring(0, lastInd);
    }
}
