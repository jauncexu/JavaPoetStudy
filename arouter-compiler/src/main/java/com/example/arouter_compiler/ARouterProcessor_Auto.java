package com.example.arouter_compiler;

import com.example.arouter_annotations.ARouter;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.example.arouter_annotations.ARouter"}) // 注解
@SupportedSourceVersion(SourceVersion.RELEASE_7)

// 接收 安卓工程传递过来的参数
@SupportedOptions("student")

// 对类进行注解解析 （动态）
public class ARouterProcessor_Auto extends AbstractProcessor {
    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;

    // Message用来打印 日志相关信息
    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        String value = processingEnvironment.getOptions().get("student");
        // 这个代码已经下毒了
        // 如果我想在注解处理器里面抛出异常 可以使用Diagnostic.Kind.ERROR
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>>" + value);
    }


    // 服务 ：在编译期的时候干活
    // 坑 ：如果没有在任何地方调用的话，此函数是不会调用的
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>> xjw run...");

        if (set.isEmpty()) {
            return false;
        }
        /**
         模板：
         public class MainActivity3$$$$$$$$$ARouter {
         public static Class findTargetClass(String path) {
         return path.equals("/app/MainActivity3") ? MainActivity3.class : null;
         }
         }
         */

        // group 可能有多个
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        for (Element element : elements) {

            // 包信息
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();


            // 获取简单类名，例如：MainActivity  MainActivity2  MainActivity3
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被@ARetuer注解的类有：" + className);

            // 目标：要生成的文件名称  MainActivity$$$$$$$$$ARouter
            String finalClassName = className + "$$$$$$$$$ARouter";

            ARouter aRouter = element.getAnnotation(ARouter.class);

            // javaPoet解析流程
            // 1. 方法
            MethodSpec findTargetClass = MethodSpec.methodBuilder("findTargetClass")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Class.class)
                    .addParameter(String.class, "path")
                    // 方法里面的内容 return path.equals("/app/MainActivity3") ? MainActivity3.class : null;

                    // 需要JavaPoet包装转型
                    .addStatement("return path.equals($S) ? $T.class : null",
                            aRouter.path(),
                            ClassName.get((TypeElement) element))
                    .build();

            // 2.类
            TypeSpec myClass = TypeSpec.classBuilder(finalClassName)
                    .addMethod(findTargetClass)
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            // 3.包
            JavaFile packagef = JavaFile.builder(packageName, myClass).build();

            // 生成文件
            try {
                packagef.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "生成Test文件时失败，异常:" + e.getMessage());
            }
        }

        return false; // false 不干活了， true 活干完了
    }
}
