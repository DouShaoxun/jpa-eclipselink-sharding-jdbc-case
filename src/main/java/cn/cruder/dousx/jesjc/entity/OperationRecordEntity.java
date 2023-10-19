package cn.cruder.dousx.jesjc.entity;

import lombok.Data;
import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;


@Data
@Entity(name = "operation_record_entity")
@Table(name = "t_operation_record")
public class OperationRecordEntity implements Persistable<Long> {

    @Id
    private Long recordId;

    private Long userId;

    private Integer operation;
    /**
     * 不同框架这里生成的列名不一样
     * <li/>eclipselink def operationTime
     * <li/>hibernate def operation_time
     */
    @Column(name = "operation_time")
    private Date operationTime;


    @Override
    public Long getId() {
        return recordId;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
