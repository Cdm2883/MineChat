#ifndef __NODEEXAMPLE_EMBED_HPP__
#define __NODEEXAMPLE_EMBED_HPP__

#include "env.hpp"

class NodeJs final {
private:
    int exit_;
    void *priv_;
    node_embed_helpers::CommonEnvironmentSetup *setup_;
    std::vector<std::string> args_;
    std::vector<std::string> exec_args_;

    NodeJs() noexcept;

    NodeJs(std::vector<std::string> args,
           std::vector<std::string> exec_args,
           void *priv = nullptr) noexcept;

    struct UncaughtExceptionCallbackData {
        NodeJs *node_instance;
        std::string *err;

        UncaughtExceptionCallbackData(NodeJs *obj, std::string *err) noexcept
                : node_instance(obj), err(err) {}
    };

    static char *argv[2];
    static std::unique_ptr<node::MultiIsolatePlatform> platform;

    static void OnUncaughtException(NodeJs *node_instance,
                                    v8::Local<v8::Value> js_error,
                                    bool stop_env,
                                    std::string *err);

    static void JsOnUncaughtExeption(
            const v8::FunctionCallbackInfo<v8::Value> &args);

public:
    using EvalCallback = std::function<
            void(const v8::Local<v8::Context> &, const v8::Local<v8::Value> &, void *)>;

    static int Initialize();

    static void Shutdown();

    ~NodeJs();

    NodeJs(const NodeJs &) = delete;

    NodeJs &operator=(const NodeJs &) = delete;

    NodeJs(NodeJs &&) = delete;

    NodeJs &operator=(NodeJs &&) = delete;

    void *Data() const noexcept;

    int Eval(const std::string &script,
             const EvalCallback &callback,
             void *data,
             std::string *errout);

    int Dispose();

    void SpinEventLoop();

    static NodeJs *Create(std::string *err, void *priv = nullptr);

    static NodeJs *Create(const std::vector<std::string> &args,
                          const std::vector<std::string> &exec_args,
                          std::string *err,
                          void *priv = nullptr);

    static bool RunMain(const std::string &entry_path,
                        const std::vector<std::string> &args,
                        int *exit_code,
                        std::string *err);
};

#endif
