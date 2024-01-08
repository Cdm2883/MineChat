#ifndef __NODEEXAMPLE_BINDING_HPP__
#define __NODEEXAMPLE_BINDING_HPP__

#include <jni.h>
#include "node.h"
#include "node_api.h"

void init(
        v8::Local<v8::Object> exports,
        v8::Local<v8::Value> module,
        v8::Local<v8::Context> context,
        void *priv
);

napi_value init2(napi_env env, napi_value exports);

#endif
