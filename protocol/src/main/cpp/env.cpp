#include <cstdlib>
#include <cstdio>
#include "env.hpp"

#ifdef __GNUC__
#define LIKELY(expr) __builtin_expect(!!(expr), 1)
#define UNLIKELY(expr) __builtin_expect(!!(expr), 0)
#define PRETTY_FUNCTION_NAME __PRETTY_FUNCTION__
#else
#define LIKELY(expr) expr
#define UNLIKELY(expr) expr
#define PRETTY_FUNCTION_NAME ""
#endif

#define CHECK(expr)                                                           \
  do {                                                                        \
    if (UNLIKELY(!(expr))) {                                                  \
      abort();                                                                \
    }                                                                         \
  } while (0)

#define CHECK_NOT_NULL(val) CHECK((val) != nullptr)

namespace node_embed_helpers {

    template<typename T, void (*function)(T *)>
    struct FunctionDeleter {
        void operator()(T *pointer) const { function(pointer); }

        using Pointer = std::unique_ptr<T, FunctionDeleter>;
    };

    template<typename T, void (*function)(T *)>
    using DeleteFnPtr = typename FunctionDeleter<T, function>::Pointer;

    struct CommonEnvironmentSetup::Impl {
        node::MultiIsolatePlatform *platform;
        uv_loop_t loop;
        std::shared_ptr<node::ArrayBufferAllocator> allocator;
        v8::Isolate *isolate;
        DeleteFnPtr<node::IsolateData, node::FreeIsolateData> isolate_data;
        DeleteFnPtr<node::Environment, node::FreeEnvironment> env;
        v8::Global<v8::Context> context;

        Impl() noexcept;
    };

    CommonEnvironmentSetup::Impl::Impl() noexcept:
            platform(nullptr),
            loop(),
            allocator(nullptr),
            isolate(nullptr),
            isolate_data(nullptr),
            env(nullptr),
            context() {}

    CommonEnvironmentSetup::CommonEnvironmentSetup(
            node::MultiIsolatePlatform *platform,
            std::vector<std::string> *errors,
            const std::function<node::Environment *(const CommonEnvironmentSetup *)> &make_env)
            : impl_(new Impl()) {
        CHECK_NOT_NULL(platform);
        CHECK_NOT_NULL(errors);

        impl_->platform = platform;
        uv_loop_t *loop = &impl_->loop;
        // Use `data` to tell the destructor whether the loop was initialized or not.
        loop->data = nullptr;
        int ret = uv_loop_init(loop);
        if (ret != 0) {
            errors->push_back(std::string("Failed to initialize loop: ") + uv_err_name(ret));
            return;
        }
        loop->data = this;

        impl_->allocator = node::ArrayBufferAllocator::Create();
        impl_->isolate = node::NewIsolate(impl_->allocator, &impl_->loop, platform);
        v8::Isolate *isolate = impl_->isolate;

        {
            v8::Locker locker(isolate);
            v8::Isolate::Scope isolate_scope(isolate);
            impl_->isolate_data.reset(node::CreateIsolateData(
                    isolate, loop, platform, impl_->allocator.get()));

            v8::HandleScope handle_scope(isolate);
            v8::Local<v8::Context> context = node::NewContext(isolate);
            impl_->context.Reset(isolate, context);
            if (context.IsEmpty()) {
                errors->push_back("Failed to initialize V8 Context");
                return;
            }

            v8::Context::Scope context_scope(context);
            impl_->env.reset(make_env(this));
        }
    }

    CommonEnvironmentSetup::~CommonEnvironmentSetup() {
        if (impl_->isolate != nullptr) {
            v8::Isolate *isolate = impl_->isolate;
            {
                v8::Locker locker(isolate);
                v8::Isolate::Scope isolate_scope(isolate);

                impl_->context.Reset();
                impl_->env.reset();
                impl_->isolate_data.reset();
            }

            bool platform_finished = false;
            impl_->platform->AddIsolateFinishedCallback(isolate, [](void *data) {
                *static_cast<bool *>(data) = true;
            }, &platform_finished);
            impl_->platform->UnregisterIsolate(isolate);
            isolate->Dispose();

            // Wait until the platform has cleaned up all relevant resources.
            while (!platform_finished)
                uv_run(&impl_->loop, UV_RUN_ONCE);
        }

        if (impl_->isolate || impl_->loop.data != nullptr)
            uv_loop_close(&impl_->loop);

        delete impl_;
    }

    uv_loop_t *CommonEnvironmentSetup::event_loop() const {
        return &impl_->loop;
    }

    std::shared_ptr<node::ArrayBufferAllocator>
    CommonEnvironmentSetup::array_buffer_allocator() const {
        return impl_->allocator;
    }

    v8::Isolate *CommonEnvironmentSetup::isolate() const {
        return impl_->isolate;
    }

    node::IsolateData *CommonEnvironmentSetup::isolate_data() const {
        return impl_->isolate_data.get();
    }

    node::Environment *CommonEnvironmentSetup::env() const {
        return impl_->env.get();
    }

    v8::Local<v8::Context> CommonEnvironmentSetup::context() const {
        return impl_->context.Get(impl_->isolate);
    }

}
