package orz.springboot.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class OrzWebCorsConfiguration {
    private final OrzWebProps props;

    public OrzWebCorsConfiguration(OrzWebProps props) {
        this.props = props;
    }

    @Bean
    public CorsFilter corsFilter() {
        var source = new UrlBasedCorsConfigurationSource();
        props.getCorsOrDefault().forEach((path, config) -> {
            var cors = new CorsConfiguration();
            cors.setAllowedOrigins(config.getAllowedOrigins());
            cors.setAllowedMethods(config.getAllowedMethods());
            cors.setAllowedHeaders(config.getAllowedHeaders());
            cors.setExposedHeaders(config.getExposedHeaders());
            cors.setMaxAge(config.getMaxAge());
            source.registerCorsConfiguration(path, cors);
        });
        return new CorsFilter(source);
    }
}
