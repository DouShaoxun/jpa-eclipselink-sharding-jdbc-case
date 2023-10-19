package cn.cruder.dousx.jesjc.alg;

import cn.hutool.core.date.DateUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 按月分表的 Sharding 算法
 */
@Getter
@Slf4j
public class HisDataMonthShardingAlgorithm implements StandardShardingAlgorithm<Date> {
    private static final String DEF_TABLE_LOWER_DATE = "2017_01";
    public static final String ARG_NAME = "HIS_DATA_SPI_BASED";
    private final ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy_MM"));

    private Properties props;
    /**
     * 设置该参数的原因是，如果在范围查找的时候我们没有设置最小值，比如下面的查询
     * where acquisition_time < '2022-08-11 00:00:00'
     * 这个时候范围查找就只有上限而没有下限，这时候就需要有一个下限值兜底，不能一致遍历下去
     */
    private Date tableLowerDate;

    /**
     * 在配置文件中配置算法的时候会配置 props 参数，框架会将props中的配置放在 properties 参数中，并且初始化算法的时候被调用
     *
     * @param properties properties
     */
    @Override
    public void init(Properties properties) {
        this.props = properties;
        // 配置格式 2017_01
        String autoCreateTableLowerDate = properties.getProperty("auto-create-table-lower");
        try {

            this.tableLowerDate = dateFormatThreadLocal.get().parse(autoCreateTableLowerDate);
        } catch (Exception e) {
            log.error("parse auto-create table lower date failed: {}, use default date {}",
                    e.getMessage(), DEF_TABLE_LOWER_DATE);
            try {
                this.tableLowerDate = dateFormatThreadLocal.get().parse(DEF_TABLE_LOWER_DATE);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 精确路由算法
     *
     * @param availableTargetNames 可用的表列表（配置文件中配置的 actual-data-nodes会被解析成 列表被传递过来）
     * @param shardingValue        精确的值
     * @return 结果表
     */
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {
        Date value = shardingValue.getValue();
        // 根据精确值获取路由表
        String actuallyTableName = shardingValue.getLogicTableName() + shardingSuffix(value);
        if (availableTargetNames.contains(actuallyTableName)) {
            return actuallyTableName;
        }
        return null;
    }

    /**
     * 范围路由算法
     *
     * @param availableTargetNames 可用的表列表（配置文件中配置的 actual-data-nodes会被解析成 列表被传递过来）
     * @param shardingValue        值范围
     * @return 路由后的结果表
     */
    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<Date> shardingValue) {
        // 获取到范围查找的最小值，如果条件中没有最小值设置为 tableLowerDate
        Date rangeLowerDate;
        if (shardingValue.getValueRange().hasLowerBound()) {
            rangeLowerDate = shardingValue.getValueRange().lowerEndpoint();
        } else {
            rangeLowerDate = tableLowerDate;
        }

        // 获取到范围查找的最大值，如果没有配置最大值，设置为当前时间 + 1 月
        // 这里需要注意，你的项目里面这样做是否合理
        Date rangeUpperDate;
        if (shardingValue.getValueRange().hasUpperBound()) {
            rangeUpperDate = shardingValue.getValueRange().upperEndpoint();
        } else {
            // 往后延一个月
            rangeUpperDate = DateUtil.offsetMonth(new Date(), 1);
        }
        rangeUpperDate = DateUtil.endOfMonth(rangeUpperDate);
        List<String> tableNames = new ArrayList<>();
        // 过滤那些存在的表
        while (rangeLowerDate.before(rangeUpperDate)) {
            String actuallyTableName = shardingValue.getLogicTableName() + shardingSuffix(rangeLowerDate);
            if (availableTargetNames.contains(actuallyTableName)) {
                tableNames.add(actuallyTableName);
            }
            rangeLowerDate = DateUtil.offsetMonth(rangeLowerDate, 1);
        }
        return tableNames;
    }

    /**
     * sharding 表后缀 _yyyy_MM
     */
    private String shardingSuffix(Date shardingValue) {
        return "_" + dateFormatThreadLocal.get().format(shardingValue);
    }

    /**
     * SPI方式的 SPI名称，配置文件中配置的时候需要用到
     */
    @Override
    public String getType() {
        return HisDataMonthShardingAlgorithm.ARG_NAME;
    }
}

