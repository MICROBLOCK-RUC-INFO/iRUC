package com.example.travel2.domain;

import com.example.travel2.util.StringUtils;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.jetbrains.annotations.NotNull;

/**
 * Trip 实体类，使用 JPA 进行 ORM 映射
 */
@Data
@Entity
@Table(name = "trip2") // 表名映射
@GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
public class Trip {

    // 主键 ID，使用 UUID 生成策略
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", length = 36, nullable = false, updatable = false) // 映射列名为 id
    private String id;

    // 嵌套对象 TripId，使用 @Embedded 进行嵌入式映射
    @Embedded
    private TripId tripId;

    // 列映射，字段不能为空
    @NotNull
    @Column(name = "train_type_name", nullable = false)
    private String trainTypeName;

    // 列映射
    @Column(name = "route_id")
    private String routeId;

    // 起点站
    @NotNull
    @Column(name = "start_station_name", nullable = false)
    private String startStationName;

    // 中间站点
    @Column(name = "stations_name")
    private String stationsName;

    // 终点站
    @NotNull
    @Column(name = "terminal_station_name", nullable = false)
    private String terminalStationName;

    // 起始时间
    @NotNull
    @Column(name = "start_time", nullable = false)
    private String startTime;

    // 结束时间
    @NotNull
    @Column(name = "end_time", nullable = false)
    private String endTime;

    // 构造函数
    public Trip(TripId tripId, String trainTypeName, String startStationName, String stationsName, String terminalStationName, String startTime, String endTime) {
        this.tripId = tripId;
        this.trainTypeName = trainTypeName;
        this.startStationName = StringUtils.String2Lower(startStationName);
        this.stationsName = StringUtils.String2Lower(stationsName);
        this.terminalStationName = StringUtils.String2Lower(terminalStationName);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 另一个构造函数
    public Trip(TripId tripId, String trainTypeName, String routeId) {
        this.tripId = tripId;
        this.trainTypeName = trainTypeName;
        this.routeId = routeId;
        this.startStationName = "";
        this.terminalStationName = "";
        this.startTime = "";
        this.endTime = "";
    }

    // 默认构造函数
    public Trip() {
        this.trainTypeName = "";
        this.startStationName = "";
        this.terminalStationName = "";
        this.startTime = "";
        this.endTime = "";
    }
}
