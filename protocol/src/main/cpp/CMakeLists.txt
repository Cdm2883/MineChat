# 有关将 CMake 与 Android Studio 配合使用的更多信息,
# 请阅读文档: https://d.android.com/studio/projects/add-native-code.html .
# 有关如何使用 CMake 的更多示例, 请参阅: https://github.com/android/ndk-samples .

# 设置此项目所需的最低 CMake 版本
cmake_minimum_required(VERSION 3.22.1)

# 声明项目名称. 可以通过 ${ PROJECT_NAME} 访问项目名称，
# 由于这是顶级 CMakeLists.txt, 因此也可以使用 ${CMAKE_PROJECT_NAME} 访问项目名称
# (两个 CMake 变量在顶级构建脚本范围内同步).
project("node-android-jni")

# 创建并命名库, 将其设置为 STATIC 或 SHARED, 并提供其源代码的相对路径.
# 您可以定义多个库, CMake 会为您构建它们. Gradle 会自动将共享库与您的 APK 打包.
#
# 在此顶级 CMakeLists.txt 中, ${CMAKE_PROJECT_NAME} 用于定义目标库名称;
# 在子模块的 CMakeLists.txt 中, ${PROJECT_NAME} 是出于相同目的的首选.
#
# 为了在 Java/Kotlin 中将库加载到您的应用程序中, 您必须
# 调用 System.loadLibrary() 并传递此处定义的库的名称;
# 对于 GameActivity/NativeActivity 派生应用程序,
# 必须在 AndroidManifest.xml 文件中使用相同的库名称。
add_library(${CMAKE_PROJECT_NAME} SHARED
        # 列出具有此 CMakeLists.txt 的相对路径的 C/C++ 源文件.
        embed.cpp
        binding.cpp
        env.cpp
        node-android-jni.cpp)

# 指定 CMake 应链接到目标库的库. 您可以链接来自不同来源的库,
# 例如此构建脚本中定义的库, 预构建的第三方库或 Android 系统库.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # 列表库链接到目标库
        android
        log)

# core 目标的私有包含目录
target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include/node)

# 安卓的 logcat 库，保存在 log-lib 变量中
find_library(log-lib log)

# 创建预编译动态库目标，名字是 imported-node-lib
add_library(imported-node-lib
        SHARED
        IMPORTED)

# 设置这个预编译库的路径
set_target_properties(imported-node-lib
        PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/lib/libnode.so)

# 把要用的库链接到 core
target_link_libraries(${CMAKE_PROJECT_NAME}
        imported-node-lib
        ${log-lib})