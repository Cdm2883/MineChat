#ifndef __NODEEXAMPLE_ENV_HPP__
#define __NODEEXAMPLE_ENV_HPP__

#include <memory>
#include <vector>
#include <string>
#include <functional>
#include "node.h"
#include "uv.h"

namespace node_embed_helpers {

    class CommonEnvironmentSetup final {
    public:
        ~CommonEnvironmentSetup();

        // Create a new CommonEnvironmentSetup, that is, a group of objects that
        // together form the typical setup for a single Node.js Environment instance.
        // If any error occurs, `*errors` will be populated and the returned pointer
        // will be empty.
        // env_args will be passed through as arguments to CreateEnvironment(), after
        // `isolate_data` and `context`.
        template<typename... EnvironmentArgs>
        static std::unique_ptr<CommonEnvironmentSetup> Create(
                node::MultiIsolatePlatform *platform,
                std::vector<std::string> *errors,
                EnvironmentArgs &&... env_args);

        struct uv_loop_s *event_loop() const;

        std::shared_ptr<node::ArrayBufferAllocator> array_buffer_allocator() const;

        v8::Isolate *isolate() const;

        node::IsolateData *isolate_data() const;

        node::Environment *env() const;

        v8::Local<v8::Context> context() const;

        CommonEnvironmentSetup(const CommonEnvironmentSetup &) = delete;

        CommonEnvironmentSetup &operator=(const CommonEnvironmentSetup &) = delete;

        CommonEnvironmentSetup(CommonEnvironmentSetup &&) = delete;

        CommonEnvironmentSetup &operator=(CommonEnvironmentSetup &&) = delete;

    private:
        struct Impl;
        Impl *impl_;

        CommonEnvironmentSetup(
                node::MultiIsolatePlatform *,
                std::vector<std::string> *,
                const std::function<node::Environment *(const CommonEnvironmentSetup *)> &);
    };

// Implementation for CommonEnvironmentSetup::Create
    template<typename... EnvironmentArgs>
    std::unique_ptr<CommonEnvironmentSetup> CommonEnvironmentSetup::Create(
            node::MultiIsolatePlatform *platform,
            std::vector<std::string> *errors,
            EnvironmentArgs &&... env_args) {
        auto ret = std::unique_ptr<CommonEnvironmentSetup>(new CommonEnvironmentSetup(
                platform, errors,
                [&](const CommonEnvironmentSetup *setup) -> node::Environment * {
                    return node::CreateEnvironment(
                            setup->isolate_data(), setup->context(),
                            std::forward<EnvironmentArgs>(env_args)...);
                }));
        if (!errors->empty()) ret.reset();
        return ret;
    }

}

#endif
