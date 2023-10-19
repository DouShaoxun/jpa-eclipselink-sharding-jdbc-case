package cn.cruder.dousx.jesjc.config;

import cn.cruder.dousx.jesjc.alg.HisDataMonthShardingAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Configuration
public class DataSourceConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("jesjc.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConditionalOnClass(DataSourceProperties.class)
    DataSource dataSource(@Autowired DataSourceProperties dataSourceProperties) throws SQLException {
        // 默认数据源
        DataSource defDataSource = dataSourceProperties.initializeDataSourceBuilder().build();
        String hisMonthShardingAlgName = "his_month_sharding";
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        // 逻辑表名要和javax.persistence.Table.name一致
        ShardingTableRuleConfiguration shardingTableRule = new ShardingTableRuleConfiguration(
                "t_operation_record",
                "jesjc.t_operation_record_202$->{3..4}_$->{['01','02','03','04','05','06','07','08','09','10','11','12']}");
        shardingTableRule.setTableShardingStrategy(new StandardShardingStrategyConfiguration("operation_time", hisMonthShardingAlgName));
        shardingRuleConfiguration.setTables(List.of(shardingTableRule));
        Properties props = new Properties(); // 构建属性配置
        props.put("sql-show", true);

        shardingRuleConfiguration.setShardingAlgorithms(Map.of(hisMonthShardingAlgName, new AlgorithmConfiguration(HisDataMonthShardingAlgorithm.ARG_NAME, props)));
        // key为数据源名称 value为具体数据源
        Map<String, DataSource> dataSourceMap = Map.of("jesjc", defDataSource);
        List<RuleConfiguration> ruleList = List.of(shardingRuleConfiguration);
        DataSource shardingSphereDataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleList, props);
        log.info("dataSource: {}", shardingSphereDataSource.getClass());
        return shardingSphereDataSource;
    }
}
