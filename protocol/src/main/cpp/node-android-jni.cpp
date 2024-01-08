#include <jni.h>
#include <unistd.h>
#include <regex>
#include <string>
#include <thread>
#include "binding.hpp"
#include "embed.hpp"
#include "log.h"

namespace {

    int pipe_stdout[2];
    int pipe_stderr[2];

    void thread_stderr_func() {
        ssize_t redirect_size;
        std::string msg;
        char buf[1024];
        while ((redirect_size = read(pipe_stderr[0], buf, sizeof(buf) - 1)) > 0) {
            if (redirect_size == (sizeof(buf) - 1)) {
                buf[redirect_size] = 0;
                msg += buf;
            } else {
                if (buf[redirect_size - 1] == '\n') {
                    --redirect_size;
                }
                buf[redirect_size] = 0;
                msg += buf;
                __android_log_write(ANDROID_LOG_ERROR, LOG_TAG, msg.c_str());
                msg = "";
            }
        }
    }

    void thread_stdout_func() {
        ssize_t redirect_size;
        std::string msg;
        char buf[1024];
        while ((redirect_size = read(pipe_stdout[0], buf, sizeof(buf) - 1)) > 0) {
            if (redirect_size == (sizeof(buf) - 1)) {
                buf[redirect_size] = 0;
                msg += buf;
            } else {
                if (buf[redirect_size - 1] == '\n') {
                    --redirect_size;
                }
                buf[redirect_size] = 0;
                msg += buf;
                __android_log_write(ANDROID_LOG_INFO, LOG_TAG, msg.c_str());
                msg = "";
            }
        }
    }

    void start_redirecting_stdout_stderr() {
        // set stdout as unbuffered.
        setvbuf(stdout, 0, _IONBF, 0);
        pipe(pipe_stdout);
        dup2(pipe_stdout[1], STDOUT_FILENO);

        // set stderr as unbuffered.
        setvbuf(stderr, 0, _IONBF, 0);
        pipe(pipe_stderr);
        dup2(pipe_stderr[1], STDERR_FILENO);

        std::thread thread_stdout(thread_stdout_func);
        std::thread thread_stderr(thread_stderr_func);
        thread_stdout.detach();
        thread_stderr.detach();
    }

}  // namespace

extern "C" JNIEXPORT jint JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_runMain(JNIEnv *env,
                                                          jclass clazz,
                                                          jstring path,
                                                          jobjectArray args) {
    const char *pathString = env->GetStringUTFChars(path, JNI_FALSE);
    std::string entry_path(pathString);
    env->ReleaseStringUTFChars(path, pathString);
    std::string err;
    std::vector<std::string> arglist;
    jsize len = env->GetArrayLength(args);
    arglist.reserve(len);
    for (jsize i = 0; i < len; ++i) {
        auto strarg =
                reinterpret_cast<jstring>(env->GetObjectArrayElement(args, i));
        const char *cstr = env->GetStringUTFChars(strarg, JNI_FALSE);
        arglist.emplace_back(cstr);
        env->ReleaseStringUTFChars(strarg, cstr);
    }
    int code = 0;
    if (!NodeJs::RunMain(entry_path, arglist, &code, &err)) {
        env->ThrowNew(env->FindClass("vip/cdms/minechat/protocol/script/ScriptException"), err.c_str());
    }
    return code;
}

extern "C" JNIEXPORT jint JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_initialize(JNIEnv *env, jclass clazz) {
    start_redirecting_stdout_stderr();
    return NodeJs::Initialize();
}

extern "C" JNIEXPORT void JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_shutdown(JNIEnv *env, jclass clazz) {
    NodeJs::Shutdown();
}

extern "C" JNIEXPORT jlong JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_createInstance(JNIEnv *env, jclass clazz) {
    std::string err;
    NodeJs *p = NodeJs::Create(&err, env);
    if (p == nullptr) {
        env->ThrowNew(env->FindClass("vip/cdms/minechat/protocol/script/NodeException"), err.c_str());
        return 0L;
    }
    return reinterpret_cast<jlong>(p);
}

extern "C" JNIEXPORT jint JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_dispose(JNIEnv *env, jobject instance) {
    jclass Clazz = env->GetObjectClass(instance);
    auto fid = env->GetFieldID(Clazz, "nativePointer", "J");
    NodeJs *p = reinterpret_cast<NodeJs *>(env->GetLongField(instance, fid));
    jint exit_code = 0;
    if (p != nullptr) {
        exit_code = p->Dispose();
        delete p;
    }
    env->SetLongField(instance, fid, 0L);
    return exit_code;
}

inline NodeJs *GetNodeInstance(JNIEnv *env, jobject instance) {
    jclass Clazz = env->GetObjectClass(instance);
    NodeJs *p = reinterpret_cast<NodeJs *>(env->GetLongField(
            instance, env->GetFieldID(Clazz, "nativePointer", "J")));
    if (p == nullptr) {
        env->ThrowNew(env->FindClass("vip/cdms/minechat/protocol/script/NodeException"), "bad node instance");
    }
    return p;
}

extern "C" JNIEXPORT void JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_evalVoid(JNIEnv *env, jobject instance,
                                                           jstring script) {
    std::string errmsg;
    const char *scriptString = env->GetStringUTFChars(script, JNI_FALSE);
    NodeJs *p = GetNodeInstance(env, instance);
    if (!p)
        return;
    int r = p->Eval(scriptString, NodeJs::EvalCallback{}, nullptr, &errmsg);
    env->ReleaseStringUTFChars(script, scriptString);
    if (r != 0) {
        env->ThrowNew(env->FindClass("vip/cdms/minechat/protocol/script/ScriptException"), errmsg.c_str());
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_evalBool(JNIEnv *env, jobject instance,
                                                           jstring script) {
    std::string errmsg;
    uint8_t result = 0;
    const char *scriptString = env->GetStringUTFChars(script, JNI_FALSE);
    NodeJs *p = GetNodeInstance(env, instance);
    if (!p)
        return 0;
    int r = p->Eval(
            scriptString,
            [](const v8::Local<v8::Context> &context,
               const v8::Local<v8::Value> &value, void *data) {
                if (data != nullptr && value->IsBoolean()) {
                    *static_cast<uint8_t *>(data) =
                            value->BooleanValue(context->GetIsolate());
                }
            },
            &result, &errmsg);
    env->ReleaseStringUTFChars(script, scriptString);
    if (r != 0) {
        env->ThrowNew(env->FindClass("vip/cdms/minechat/protocol/script/ScriptException"), errmsg.c_str());
        return 0;
    }
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_evalInt(JNIEnv *env, jobject instance,
                                                          jstring script) {
    std::string errmsg;
    int32_t result = 0;
    const char *scriptString = env->GetStringUTFChars(script, JNI_FALSE);
    NodeJs *p = GetNodeInstance(env, instance);
    if (!p)
        return 0;
    int r = p->Eval(
            scriptString,
            [](const v8::Local<v8::Context> &context,
               const v8::Local<v8::Value> &value, void *data) {
                if (data != nullptr && value->IsNumber()) {
                    *static_cast<int32_t *>(data) = value->Int32Value(context).ToChecked();
                }
            },
            &result, &errmsg);
    env->ReleaseStringUTFChars(script, scriptString);
    if (r != 0) {
        env->ThrowNew(env->FindClass("vip/cdms/minechat/protocol/script/ScriptException"), errmsg.c_str());
        return 0;
    }
    return result;
}

extern "C" JNIEXPORT jdouble JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_evalDouble(JNIEnv *env, jobject instance,
                                                             jstring script) {
    std::string errmsg;
    double result = 0;
    const char *scriptString = env->GetStringUTFChars(script, JNI_FALSE);
    NodeJs *p = GetNodeInstance(env, instance);
    if (!p)
        return 0;
    int r = p->Eval(
            scriptString,
            [](const v8::Local<v8::Context> &context,
               const v8::Local<v8::Value> &value, void *data) {
                if (data != nullptr && value->IsNumber()) {
                    *static_cast<double *>(data) = value->NumberValue(context).ToChecked();
                }
            },
            &result, &errmsg);
    env->ReleaseStringUTFChars(script, scriptString);
    if (r != 0) {
        env->ThrowNew(env->FindClass("vip/cdms/minechat/protocol/script/ScriptException"), errmsg.c_str());
        return 0;
    }
    return result;
}

extern "C" JNIEXPORT jstring JNICALL
Java_vip_cdms_minechat_protocol_script_NodeNative_evalString(JNIEnv *env, jobject instance,
                                                             jstring script) {
    std::string errmsg;
    std::string result;
    const char *scriptString = env->GetStringUTFChars(script, JNI_FALSE);
    NodeJs *p = GetNodeInstance(env, instance);
    if (!p)
        return 0;
    int r = p->Eval(
            scriptString,
            [](const v8::Local<v8::Context> &context,
               const v8::Local<v8::Value> &value, void *data) {
                if (data != nullptr/* && value->IsString()*/) {
                    v8::String::Utf8Value str(context->GetIsolate(), value);
                    *static_cast<std::string *>(data) = *str;
                }
            },
            &result, &errmsg);
    env->ReleaseStringUTFChars(script, scriptString);
    if (r != 0) {
        env->ThrowNew(env->FindClass("vip/cdms/minechat/protocol/script/ScriptException"), errmsg.c_str());
        return 0;
    }
    return env->NewStringUTF(result.c_str());
}
