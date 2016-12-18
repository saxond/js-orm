package org.daubin.js.database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import javax.persistence.Column;
import javax.persistence.Table;

import org.daubin.js.database.Columns.ColumnAndType;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

class ClassGenerator {
    private static final org.objectweb.asm.commons.Method TOSTRING_METHOD = new 
            org.objectweb.asm.commons.Method("toString", "()Ljava/lang/String;");
    private static final Type STRINGBUILDER_TYPE = Type.getType(StringBuilder.class);

    static final org.objectweb.asm.commons.Method APPEND_STRING_METHOD = getAppendMethod(String.class);
    static final org.objectweb.asm.commons.Method APPEND_CHAR_METHOD = getAppendMethod(char.class);

    private static org.objectweb.asm.commons.Method getAppendMethod(Class<?> clazz) {
        return new
                org.objectweb.asm.commons.Method("append", STRINGBUILDER_TYPE, 
                        new Type[] { Type.getType(clazz) });
    }


    /**
     * Helper for loading new class bytes into a classloader.
     */
    private final Function<byte[], Class<?>> classLoader;
    
    public ClassGenerator() throws NoSuchMethodException, SecurityException {
        classLoader = createClassLoader(DatabaseContext.class.getClassLoader());
    }

    public Class<?> createClass(String tableName, List<ColumnAndType> columns) {
        return classLoader.apply(generateClass(tableName, columns));
    }

    private static byte[] generateClass(String tableName, List<ColumnAndType> columns) {
    
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    
        cw.visit(52, Opcodes.ACC_PUBLIC, tableName, null, 
                Type.getInternalName(Object.class), new String[0]);
        
        cw.visitSource(tableName + ".java", null);
        
        AnnotationVisitor tableAnnotation = cw.visitAnnotation(Type.getDescriptor(Table.class), true);
        tableAnnotation.visit("name", tableName);
        tableAnnotation.visitEnd();
        
        for (ColumnAndType column : columns) {
            generateField(cw, column);
        }
        
        generateConstructor(cw);
        
        generateToString(cw, tableName, columns);
    
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void generateToString(ClassWriter cw, String tableName, List<ColumnAndType> columns) {
        GeneratorAdapter mv =
                new GeneratorAdapter(Opcodes.ACC_PUBLIC, TOSTRING_METHOD,
                cw.visitMethod(Opcodes.ACC_PUBLIC, TOSTRING_METHOD.getName() , TOSTRING_METHOD.getDescriptor(), null, null));
        mv.visitCode();
        
        int local = mv.newLocal(STRINGBUILDER_TYPE);
        mv.push(STRINGBUILDER_TYPE);
        mv.newInstance(STRINGBUILDER_TYPE);
        
        mv.dup();
        mv.invokeConstructor(STRINGBUILDER_TYPE, 
                new org.objectweb.asm.commons.Method("<init>", "()V"));
        mv.storeLocal(local);
        
        appendString(mv, local, tableName);
        
        appendChar(mv, local, '{');
        
        boolean first = true;
        for (ColumnAndType col : columns) {
            if (first) {
                first = false;
            } else {
                appendChar(mv, local, ',');
            }
            appendString(mv, local, col.name());
            appendChar(mv, local, ':');
            
            mv.loadLocal(local);
            mv.loadThis();
            mv.visitFieldInsn(Opcodes.GETFIELD, tableName, col.name(), Type.getDescriptor(col.type().clazz));
            mv.invokeVirtual(STRINGBUILDER_TYPE, getAppendMethod(col.type().clazz));
        }
        
        appendChar(mv, local, '}');
        
        mv.loadLocal(local);
        mv.invokeVirtual(STRINGBUILDER_TYPE, TOSTRING_METHOD);
        
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1,1);
        
        mv.visitEnd();
    }

    private static void appendString(GeneratorAdapter mv, int local, String value) {
        mv.loadLocal(local);
        mv.push(value);
        mv.invokeVirtual(STRINGBUILDER_TYPE, APPEND_STRING_METHOD);
    }

    private static void appendChar(GeneratorAdapter mv, int local, char c) {
        mv.loadLocal(local);
        mv.push(c);
        mv.invokeVirtual(STRINGBUILDER_TYPE, APPEND_CHAR_METHOD);
    }

    private static void generateField(ClassWriter cw, ColumnAndType column) {
        FieldVisitor field = cw.visitField(Opcodes.ACC_PUBLIC, column.name(), Type.getDescriptor(column.type().clazz), null, null);
        annotateField(field, column);
        
        field.visitEnd();
    }

    private static void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0); //load the first local variable: this
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1,1);
        mv.visitEnd();
    }

    static void annotateField(FieldVisitor field, ColumnAndType column) {
        AnnotationVisitor fieldAnn = field.visitAnnotation(Type.getDescriptor(javax.persistence.Column.class), true);

        Method[] annotationValues = Column.class.getDeclaredMethods();
        for (Method value : annotationValues) {
            try {
                Object val = value.invoke(column);
                if (null != val) {
                    fieldAnn.visit(value.getName(), val);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    
        fieldAnn.visitEnd();
    }

    private static Function<byte[], Class<?>> createClassLoader(ClassLoader loader) throws NoSuchMethodException, SecurityException {
        final Class<?> cls = ClassLoader.class;
        final java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", new Class[] { String.class, byte[].class, int.class, int.class });
    
        return new Function<byte[], Class<?>>() {
    
            @Override
            public Class<?> apply(byte[] bytes) {
                ClassReader reader = new ClassReader(bytes);
                method.setAccessible(true);
                try {
                  Object[] args = new Object[] { reader.getClassName(), bytes, new Integer(0), new Integer(bytes.length)};
                  return (Class<?>) method.invoke(loader, args);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                  method.setAccessible(false);
                }
            }
            
        };
    }

}
