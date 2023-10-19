package cn.cruder.dousx.jesjc;


import cn.cruder.dousx.jesjc.entity.OperationRecordEntity;
import cn.cruder.dousx.jesjc.repository.OperationRecordRepository;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManagerFactory;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
public class OperationRecordRepositoryTests {
    @Autowired
    private OperationRecordRepository operationRecordRepository;
    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Test
    void insertTestData() {
        Random random = new Random();
        for (long i = 0; i < 300; i++) {
            OperationRecordEntity record = new OperationRecordEntity();
            long recordId = IdUtil.getSnowflake().nextId();
            record.setRecordId(recordId);
            record.setUserId(i);
            record.setOperation(1);
            // 保证以后允许代码能跑的通 (jpa自动创建表的情况下)
            // 2023-2024
            int year = 2023 + random.nextInt(2);
            // 月份 0~11
            int month = random.nextInt(12);
            // 1-10号 不影响分片
            int day = 1 + random.nextInt(10);
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            record.setOperationTime(calendar.getTime());
            log.info(" OperationRecord insert {}", record);
            operationRecordRepository.save(record);

        }
    }

    @Test
    void findByTime() {
        List<OperationRecordEntity> all = operationRecordRepository.findAll();
        log.info("all：{}", CollUtil.size(all));
        Map<String, List<OperationRecordEntity>> collect = all.stream()
                .collect(Collectors.groupingBy(e -> DateUtil.format(e.getOperationTime(), "yyyy_MM")));
        // tong
        List<String> keyList = new ArrayList<>(collect.keySet().stream().toList());
        keyList.sort(StringUtils::compare);
        for (String key : keyList) {
            log.info("{} {}", key, CollUtil.size(collect.get(key)));
        }
        DateTime startDate = DateUtil.parse("2023-06-01 00:00:00");
        DateTime endDate = DateUtil.parse("2023-10-01 00:00:00");
        List<OperationRecordEntity> list = operationRecordRepository.findByOperationTimeDateBetween(startDate.toSqlDate(), endDate.toSqlDate());
        log.info("size：{}", CollUtil.size(list));
    }


    @Test
    void orderByTest() {
        List<OperationRecordEntity> all = operationRecordRepository.findAll();
        log.info("all：{}", CollUtil.size(all));
        Map<String, List<OperationRecordEntity>> collect = all.stream()
                .collect(Collectors.groupingBy(e -> DateUtil.format(e.getOperationTime(), "yyyy_MM")));
        // tong
        List<String> keyList = new ArrayList<>(collect.keySet().stream().toList());
        keyList.sort(StringUtils::compare);
        for (String key : keyList) {
            log.info("{} {}", key, CollUtil.size(collect.get(key)));
        }
        DateTime startDate = DateUtil.parse("2023-05-03 00:00:00");
        DateTime endDate = DateUtil.parse("2023-07-08 00:00:00");
        List<OperationRecordEntity> list = operationRecordRepository.findByOperationTimeDateBetweenOrderByOperationTime(startDate.toSqlDate(), endDate.toSqlDate());

        int size = CollUtil.size(list);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                OperationRecordEntity record = list.get(i);
                log.info("{} {}", record.getId(), DatePattern.NORM_DATETIME_MS_FORMAT.format(record.getOperationTime()));
            }
        }
        log.info("size：{}", size);
    }

}
