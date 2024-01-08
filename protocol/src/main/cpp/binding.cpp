#include "log.h"
#include "binding.hpp"
#include "embed.hpp"

namespace {

    jobject GetGlobalContext(JNIEnv *env) {
        jclass ActivityThread = env->FindClass("android/app/ActivityThread");
        jmethodID currentActivityThread = env->GetStaticMethodID(ActivityThread,
                                                                 "currentActivityThread",
                                                                 "()Landroid/app/ActivityThread;");
        jobject at = env->CallStaticObjectMethod(ActivityThread, currentActivityThread);

        jmethodID getApplication = env->GetMethodID(ActivityThread, "getApplication",
                                                    "()Landroid/app/Application;");
        jobject context = env->CallObjectMethod(at, getApplication);
        return context;
    }

    jobject GetServiceActivity(JNIEnv *env) {
        jclass ServicePool = env->FindClass("vip/cdms/minechat/protocol/plugin/ServicePool");
        jmethodID getInstance = env->GetStaticMethodID(ServicePool, "getInstance",
                                                       "(Ljava/lang/String;)Lvip/cdms/minechat/protocol/plugin/ServiceExtension;");
        jobject nodejsService = env->CallStaticObjectMethod(ServicePool, getInstance,
                                                            env->NewStringUTF("vip.cdms.minechat.protocol.plugin.builtin.service.NodejsService"));

        jclass Plugin = env->FindClass("vip/cdms/minechat/protocol/plugin/Plugin");
        jmethodID getActivity = env->GetMethodID(Plugin, "getActivity", "()Landroid/app/Activity;");
        jobject activity = env->CallObjectMethod(nodejsService, getActivity);

        return activity;
    }

}

void toast(const v8::FunctionCallbackInfo<v8::Value> &args) {
    v8::Isolate *isolate = args.GetIsolate();

    if (args.Length() < 1) {
        isolate->ThrowException(v8::Exception::TypeError(
                v8::String::NewFromUtf8(isolate, "missing message").ToLocalChecked()));
        return;
    }
    if (!args[0]->IsString() || (args.Length() > 1 && !args[1]->IsString())) {
        isolate->ThrowException(v8::Exception::TypeError(
                v8::String::NewFromUtf8(isolate, "message is not a string").ToLocalChecked()));
        return;
    }

    v8::Local<v8::External> env_external = args.Data().As<v8::External>();
    auto *env = static_cast<JNIEnv *>(env_external->Value());

    jclass Activity = env->FindClass("android/app/Activity");
    jclass Window = env->FindClass("android/view/Window");
    jmethodID getWindow = env->GetMethodID(Activity, "getWindow", "()Landroid/view/Window;");
    jmethodID getDecorView = env->GetMethodID(Window, "getDecorView", "()Landroid/view/View;");
    jobject window = env->CallObjectMethod(GetServiceActivity(env), getWindow);
    jobject decorView = env->CallObjectMethod(window, getDecorView);

    jclass Toast = env->FindClass("vip/cdms/mcoreui/view/show/Toast");

    jmethodID init = env->GetMethodID(Toast, "<init>", "()V");
    jobject toast = env->NewObject(Toast, init);

    jmethodID setTitle = env->GetMethodID(Toast, "setTitle", "(Ljava/lang/String;)Lvip/cdms/mcoreui/view/show/Toast;");
    toast = env->CallObjectMethod(toast, setTitle, env->NewStringUTF(
            args.Length() == 1 ? "§2[§aNode.js§2]" : *v8::String::Utf8Value(isolate, args[0])
            ));

    jmethodID setMessage = env->GetMethodID(Toast, "setMessage", "(Ljava/lang/String;)Lvip/cdms/mcoreui/view/show/Toast;");
    toast = env->CallObjectMethod(toast, setMessage, env->NewStringUTF(
            *v8::String::Utf8Value(isolate, args.Length() == 1 ? args[0] : args[1])
            ));

    jmethodID show = env->GetMethodID(Toast, "show", "(Landroid/view/View;)V");
    env->CallVoidMethod(toast, show, decorView);

    args.GetReturnValue().Set(v8::Undefined(isolate));
}

void logd(const v8::FunctionCallbackInfo<v8::Value> &args) {
    v8::Isolate *isolate = args.GetIsolate();
    if (args.Length() < 1) {
        isolate->ThrowException(v8::Exception::TypeError(
                v8::String::NewFromUtf8(isolate, "missing message").ToLocalChecked()));
        return;
    }

    v8::String::Utf8Value str(isolate, args[0]);
    __android_log_write(ANDROID_LOG_INFO, LOG_TAG, *str);
    args.GetReturnValue().Set(v8::Undefined(isolate));
}

void loge(const v8::FunctionCallbackInfo<v8::Value> &args) {
    v8::Isolate *isolate = args.GetIsolate();
    if (args.Length() < 1) {
        isolate->ThrowException(v8::Exception::TypeError(
                v8::String::NewFromUtf8(isolate, "missing message").ToLocalChecked()));
        return;
    }

    v8::String::Utf8Value str(isolate, args[0]);
    __android_log_write(ANDROID_LOG_ERROR, LOG_TAG, *str);
    args.GetReturnValue().Set(v8::Undefined(isolate));
}

void init(
        v8::Local<v8::Object> exports,
        v8::Local<v8::Value> /* module */,
        v8::Local<v8::Context> context,
        void *env
) {
    v8::Isolate *isolate = context->GetIsolate();

    v8::Local<v8::FunctionTemplate> toastTemplate = v8::FunctionTemplate::New(isolate, toast,
                                                                              v8::External::New(
                                                                                      isolate,
                                                                                      env));
    v8::Local<v8::Function> toastFunction = toastTemplate->GetFunction(context).ToLocalChecked();
    v8::Local<v8::String> toastName = v8::String::NewFromUtf8(isolate, "toast").ToLocalChecked();
    toastFunction->SetName(toastName);
    exports->Set(context, toastName, toastFunction).Check();

    v8::Local<v8::FunctionTemplate> logTemplate = v8::FunctionTemplate::New(isolate, logd);
    v8::Local<v8::Function> logFunction = logTemplate->GetFunction(context).ToLocalChecked();
    v8::Local<v8::String> logName = v8::String::NewFromUtf8(isolate,
                                                            "androidLogd").ToLocalChecked();
    logFunction->SetName(logName);
    exports->Set(context, logName, logFunction).Check();

    v8::Local<v8::FunctionTemplate> logeTemplate = v8::FunctionTemplate::New(isolate, loge);
    v8::Local<v8::Function> logeFunction = logeTemplate->GetFunction(context).ToLocalChecked();
    v8::Local<v8::String> logeName = v8::String::NewFromUtf8(isolate,
                                                             "androidLoge").ToLocalChecked();
    logeFunction->SetName(logeName);
    exports->Set(context, logeName, logeFunction).Check();
}

//napi_value init2(napi_env env, napi_value exports) {
//    napi_value world;
//    napi_create_string_utf8(env, "world", NAPI_AUTO_LENGTH, &world);
//    napi_set_named_property(env, exports, "hello", world);
//    return exports;
//}
