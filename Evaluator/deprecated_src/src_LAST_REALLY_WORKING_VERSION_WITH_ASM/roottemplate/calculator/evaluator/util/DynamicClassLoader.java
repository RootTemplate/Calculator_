package roottemplate.calculator.evaluator.util;

public class DynamicClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] bytecode) {
        return defineClass(name.replaceAll("/", "\\."), bytecode, 0, bytecode.length);
    }
}
