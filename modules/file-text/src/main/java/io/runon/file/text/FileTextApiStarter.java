package io.runon.file.text;

import io.runon.commons.config.Config;
import io.runon.commons.utils.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

/**
 * @author macle
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"io.runon"})
public class FileTextApiStarter {
    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {

        return beanFactory -> {
            BeanDefinition bean = beanFactory.getBeanDefinition(
                    DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME);

            bean.getPropertyValues().add("loadOnStartup", 1);
        };
    }


    public static void main(String[] args) {

        try {

            Config.setConfig("home.dir",args[0]);
            HashMap<String, Object> props = new HashMap<>();
            props.put("server.port", Config.getInteger("rest.api.port", 30037));
            props.put("logging.config","config/logback.xml");

            String[] springbootArgs = new String[0];

            new SpringApplicationBuilder()
                    .sources(FileTextApiStarter.class)
                    .properties(props)
                    .run(springbootArgs);
        }catch(Exception e){
            log.error(ExceptionUtil.getStackTrace(e));
        }

    }

}
