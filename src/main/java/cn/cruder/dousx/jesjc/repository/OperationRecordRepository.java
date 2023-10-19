package cn.cruder.dousx.jesjc.repository;


import cn.cruder.dousx.jesjc.entity.OperationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface OperationRecordRepository extends JpaRepository<OperationRecordEntity, Long> {

    /**
     * 根据时间区间查找
     *
     * @param startDate 起始时间
     * @param endDate   结束时间
     * @return list
     */
    @Query("select e from operation_record_entity e where e.operationTime between :startDate and :endDate")
    public List<OperationRecordEntity> findByOperationTimeDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);


    /**
     * 根据时间区间查找并且排序
     *
     * @param startDate 起始时间
     * @param endDate   结束时间
     * @return list
     */
    @Query("select e from operation_record_entity e where e.operationTime between :startDate and :endDate order by e.operationTime")
    public List<OperationRecordEntity> findByOperationTimeDateBetweenOrderByOperationTime(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
