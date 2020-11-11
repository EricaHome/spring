package framework;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Erica
 * @date 2020/11/5 23:38
 * @description TODO
 */
public class LubanApplicationContext {

    private ConcurrentHashMap<String, LubanBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private List<LuBanBeanPostProcessor> beanPostProcessorList = new ArrayList<>();  // 存储对bean处理的前置处理器，对创建好的bean进行处理

    // 基于注解的方式，需要给上下文类传递一个扫描类信息，因此需要定义一个字段用于接收传递过来的类信息
    private Class configClass;

    public LubanApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 1.判断该类上是否具有componentScan注解;
        // 2.如果有，则获取其扫描路径，获取其全部的class文件;
        // 3.循环所有的class类，为具有component注解的类创建BeanDefinition(包括类信息，作用域信息)；
        // 4.将创建好的BeanDefinition放到map集合中；
        // 5.遍历map集合将作用域为单例的BeanDefinition筛选出来，进行bean对象的定义，并将其加入到单例池中
        if (configClass.isAnnotationPresent(LubanComponentScan.class)) {
            //System.out.println(configClass.getAnnotation(LubanComponentScan.class)); // @framework.LubanComponentScan(value=com.luban.service)
            LubanComponentScan componentScanAnnotation = (LubanComponentScan) configClass.getAnnotation(LubanComponentScan.class); // @framework.LubanComponentScan(value=com.luban.service)
            String packagePath = componentScanAnnotation.value(); // com.luban.service
            // System.out.println(packagePath); // 获取其扫描路径:com.luban.service
            List<Class> beanClasses = getBeanClasses(packagePath);
            for (Class beanClass : beanClasses) {
                if (beanClass.isAnnotationPresent(LubanComponent.class)) {
                    LubanBeanDefinition beanDefinition = new LubanBeanDefinition();
                    beanDefinition.setBeanClass(beanClass); // beanClass: com.luban.service.LubanUserService
                    //System.out.println(beanClass.getAnnotation(LubanComponent.class)); // @framework.LubanComponent(value=)
                    LubanComponent component = (LubanComponent)beanClass.getAnnotation(LubanComponent.class); // @framework.LubanComponent(value=)
                    String beanName = component.value(); // lubanUserService
                    if (beanClass.isAnnotationPresent(LubanScope.class)) {
                        LubanScope scope = (LubanScope)beanClass.getAnnotation(LubanScope.class);
                        String value = scope.value();
                        if (ScopeEnum.singleton.name().equals(value)) {
                            beanDefinition.setScope(ScopeEnum.singleton);
                        } else if (ScopeEnum.prototype.name().equals(value)) {
                            beanDefinition.setScope(ScopeEnum.prototype);
                        }
                    } else {
                        beanDefinition.setScope(ScopeEnum.singleton);
                    }
                   beanDefinitionMap.put(beanName,beanDefinition);

                    // 不同的Processor拥有不同的功能
                    if (LuBanBeanPostProcessor.class.isAssignableFrom(beanClass)) {
                        try {
                            LuBanBeanPostProcessor instance = (LuBanBeanPostProcessor)beanClass.getDeclaredConstructor().newInstance();
                            beanPostProcessorList.add(instance);
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
       instanceSingletonBean();
    }

    private void instanceSingletonBean() {
        for (String beanName : beanDefinitionMap.keySet()) {
            LubanBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals(ScopeEnum.singleton)) {
                // 根据beanDefinition创建bean
                Object bean = doCreateBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object doCreateBean(String beanName, LubanBeanDefinition beanDefinition) {
        // 1.对象实例化；
        // 2.填充对象属性，判断对象上是否存在Autowired注解，若存在则需要为其创建bean对象。
        Class beanClass = beanDefinition.getBeanClass();
        try {
            Constructor declaredConstructor = beanClass.getDeclaredConstructor(); // public com.luban.service.LubanUserService()
            Object instance = declaredConstructor.newInstance();
            Field[] fields = beanClass.getDeclaredFields(); // private com.luban.service.LubanOrderService com.luban.service.LubanUserService.lubanOrderService
            for (Field field : fields) {
                 if (field.isAnnotationPresent(LubanAutowired.class)) {
                     String fieldName = field.getName(); // lubanOrderService
                     Object bean = getBean(fieldName); // class com.luban.service.LubanOrderService
                     field.setAccessible(true);
                     field.set(instance,bean);
                 }
            }
            // 获取到当前bean的名称（其它功能）
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }
            // 对bean进行初始化（其它功能）
            if (instance instanceof LubanInitializingBean) {
                ((LubanInitializingBean) instance).afterPropertiesSet();
            }
            for (LuBanBeanPostProcessor beanPostProcessor: beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            }
            return instance;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    private List<Class> getBeanClasses(String packagePath) {
        // 1.获取LubanApplicationContext的类加载器信息;
        // 2.将待扫描包中的“.“转化为”/“;
        // 3.通过一二步获取的类加载器以及扫描包路径获取资源信息；
        // 4.获取资源中包含的class文件信息，判断该文件是否包含文件夹，若存在文件夹则进行遍历，获取其绝对路径，通过类加载器进行类的定义。
        List<Class> beanClasses = new ArrayList<>();
        ClassLoader classLoader = LubanApplicationContext.class.getClassLoader();
        packagePath = packagePath.replace(".","/"); // com/luban/service
        URL resource = classLoader.getResource(packagePath); // file:/D:/JavaEncoding/lunban_spring/target/classes/com/luban/service
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) { // f:  D:\JavaEncoding\lunban_spring\target\classes\com\luban\service\LubanUserService.class
                String filePath = f.getAbsolutePath(); //  D:\JavaEncoding\lunban_spring\target\classes\com\luban\service\LubanUserService.class
                if (filePath.endsWith(".class")) {
                    String className = filePath.substring(filePath.indexOf("com"), filePath.indexOf(".class")); // com\luban\service\LubanUserService
                    className = className.replace("\\",".");// com.luban.service.LubanUserService
                    Class<?> clazz = null;
                    try {
                        clazz = classLoader.loadClass(className); // class com.luban.service.LubanUserService
                        beanClasses.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return beanClasses;
    }

    public Object getBean(String beanName) {
        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        } else {
            LubanBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            return doCreateBean(beanName, beanDefinition);
        }
    }
}
