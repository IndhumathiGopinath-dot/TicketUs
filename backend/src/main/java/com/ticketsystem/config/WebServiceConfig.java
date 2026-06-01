package com.ticketsystem.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * SOAP / WSDL configuration.
 *
 * Once the app is running, the WSDL is published at:
 *   http://localhost:8082/api/ws/tickets.wsdl
 *
 * SOAP requests are POSTed to:
 *   http://localhost:8082/api/ws
 *
 * (Both go through the /api context path that's already configured.)
 */
@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext ctx) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(ctx);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    /** The "tickets" name here becomes the WSDL filename: tickets.wsdl */
    @Bean(name = "tickets")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema ticketsSchema) {
        DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
        wsdl.setPortTypeName("TicketStatsPort");
        wsdl.setLocationUri("/ws");
        wsdl.setTargetNamespace("http://ticketsystem.com/soap/tickets");
        wsdl.setSchema(ticketsSchema);
        return wsdl;
    }

    @Bean
    public XsdSchema ticketsSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/tickets.xsd"));
    }
}
