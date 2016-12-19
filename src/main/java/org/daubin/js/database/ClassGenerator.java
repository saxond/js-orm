package org.daubin.js.database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import javax.persistence.Column;
import javax.persistence.Table;

import org.daubin.js.database.Columns.ColumnMetadata;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.google.common.collect.ImmutableMap;

class ClassGenerator {
    private static final int JAVA_VERSION = 52;
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

    public Class<?> createClass(String tableName, List<ColumnMetadata> columns) {
        return classLoader.apply(generateClass(tableName, columns));
    }

    private static byte[] generateClass(String tableName, List<ColumnMetadata> columns) {
    
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        final String className = "org/daubin/generated/" + tableName;
        cw.visit(JAVA_VERSION, Opcodes.ACC_PUBLIC, className, null, 
                Type.getInternalName(Object.class), 
                new String[] { Type.getInternalName( Model.class ) });
        
        cw.visitSource(className, null);
        
        // Add the persistence Table annotation
        AnnotationVisitor tableAnnotation = cw.visitAnnotation(Type.getDescriptor(Table.class), true);
        populateAnnotation(tableAnnotation, Table.class, 
        		Annotations.generateAnnotationProxy(Table.class, ImmutableMap.of("name", tableName)));
        
        // generate fields for all columns
        for (ColumnMetadata column : columns) {
            generateField(cw, column);
        }
        
        // generate the default constructor
        generateConstructor(cw);
        
        generateToString(cw, tableName, className, columns);
    
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void generateToString(ClassWriter cw, String tableName, String className, List<ColumnMetadata> columns) {
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
        for (ColumnMetadata colAndType : columns) {
        	Column col = colAndType.getColumn();
            if (first) {
                first = false;
            } else {
                appendChar(mv, local, ',');
            }
            appendString(mv, local, col.name());
            appendChar(mv, local, ':');
            
            mv.loadLocal(local);
            mv.loadThis();
            Class<?> columnClass = colAndType.getColumnType().getColumnClass(col);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, col.name(), Type.getDescriptor(columnClass));
            if (col.nullable()) {
            	mv.invokeVirtual(Type.getType(Object.class), TOSTRING_METHOD);
            }
            mv.invokeVirtual(STRINGBUILDER_TYPE, getAppendMethod(col.nullable() ? String.class : columnClass));
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

    private static void generateField(ClassWriter cw, ColumnMetadata column) {
        FieldVisitor field = cw.visitField(Opcodes.ACC_PUBLIC, column.getColumn().name(), 
        		Type.getDescriptor(column.getColumnType().getColumnClass(column.getColumn())), null, null);
        AnnotationVisitor fieldAnn = field.visitAnnotation(Type.getDescriptor(javax.persistence.Column.class), true);
        populateAnnotation(fieldAnn, Column.class, column.getColumn());
        
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

    static <T> void populateAnnotation(AnnotationVisitor av, Class<T> annotationType, T instance) {

        Method[] annotationValues = annotationType.getDeclaredMethods();
        for (Method value : annotationValues) {
        	if (!value.getReturnType().isArray()) {
	            try {
	                Object val = value.invoke(instance);
	                if (null != val) {
	                    av.visit(value.getName(), val);
	                }
	            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	                e.printStackTrace();
	            }
        	}
        }
    
        av.visitEnd();
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
					String className = reader.getClassName().replace('/', '.');
					Object[] args = new Object[] { className, bytes,
							new Integer(0), new Integer(bytes.length) };
					return (Class<?>) method.invoke(loader, args);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
					return null;
				} finally {
					method.setAccessible(false);
				}
			}
            
        };
    }

}
