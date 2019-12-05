package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials() {
        return new DatabaseServiceCredentials(System.getenv("VCAP_SERVICES"));
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials databaseServiceCredentials) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseServiceCredentials.jdbcUrl("movies-mysql"));
        return dataSource;
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials databaseServiceCredentials) {
//        MysqlDataSource dataSource = new MysqlDataSource();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseServiceCredentials.jdbcUrl("albums-mysql"));
        return dataSource;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(true);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactoryBean(HibernateJpaVendorAdapter hibernateJpaVendorAdapter, DataSource moviesDataSource) {
//        Properties jpaProperties = new Properties();
//        jpaProperties.put("hibernate.show_sql",hibernateShowSql);

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(moviesDataSource);
        factoryBean.setPackagesToScan("org.superbiz.moviefun");
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
//        factoryBean.setJpaProperties(jpaProperties);
        factoryBean.setPersistenceUnitName("MoviesPU");

        return factoryBean;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactoryBean(HibernateJpaVendorAdapter hibernateJpaVendorAdapter, DataSource albumsDataSource) {
//        Properties jpaProperties = new Properties();
//        jpaProperties.put("hibernate.show_sql",hibernateShowSql);
        ;

        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(albumsDataSource);
        factoryBean.setPackagesToScan("org.superbiz.moviefun");
        factoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
//        factoryBean.setJpaProperties(jpaProperties);
        factoryBean.setPersistenceUnitName("AlbumsPU");

        return factoryBean;
    }


    @Bean
    public PlatformTransactionManager moviesPlatformManager(EntityManagerFactory moviesEntityManagerFactoryBean) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(moviesEntityManagerFactoryBean);
        return jpaTransactionManager;
    }

    @Bean
    public PlatformTransactionManager albumsPlatformManager(EntityManagerFactory albumsEntityManagerFactoryBean) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(albumsEntityManagerFactoryBean);
        return jpaTransactionManager;
    }



}
