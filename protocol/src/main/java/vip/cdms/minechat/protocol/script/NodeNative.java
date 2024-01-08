package vip.cdms.minechat.protocol.script;

/**
 * 基础代码来自博客
 * <a href="https://toyobayashi.github.io/2021/03/29/NodeEmbedding/">toyobayashi.github.io</a>
 */
public class NodeNative {
    public static final String[] NECESSARY_LIBRARIES = {"c++_shared", "node", "node-android-jni"};
    public static void loadLibrary() {
        for (String library : NECESSARY_LIBRARIES)
            System.loadLibrary(library);
    }

    private final long nativePointer;

    private NodeNative(long pointer) {
        nativePointer = pointer;
    }

    public long getNativePointer() {
        return nativePointer;
    }

    public static NodeNative create() throws Exception {
        return new NodeNative(createInstance());
    }
    private native static long createInstance() throws Exception;
    public native static int runMain(String path, String[] args) throws Exception;

    public native static int initialize();
    public native static void shutdown();
    public native void evalVoid(String script) throws Exception;
    public native boolean evalBool(String script) throws Exception;
    public native int evalInt(String script) throws Exception;
    public native double evalDouble(String script) throws Exception;
    public native String evalString(String script) throws Exception;

    protected native int dispose();
    @Override
    protected void finalize() {
        dispose();
    }
}
