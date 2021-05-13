package org.sample.chapter10.threadlocal.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "jcp.auth.enabled", matchIfMissing = true)
public class SsoFilterAutoConfig {

    @Bean
    public FilterRegistrationBean<SsoFilter> erpShiroFilter() {
        FilterRegistrationBean<SsoFilter> filterRegBean = new FilterRegistrationBean<>();
        filterRegBean.setFilter(new SsoFilter());
        filterRegBean.addUrlPatterns("/*");
        filterRegBean.setOrder(1);
        return filterRegBean;
    }
}
