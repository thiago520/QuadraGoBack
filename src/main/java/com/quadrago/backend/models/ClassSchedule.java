package com.quadrago.backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class ClassSchedule {

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 16)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Convert(converter = DurationToMinutesConverter.class)
    @Column(name = "duration_minutes", nullable = false)
    private Duration duration;

    /** Convenience factory */
    public static ClassSchedule of(DayOfWeek dayOfWeek, LocalTime startTime, Duration duration) {
        return ClassSchedule.builder()
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .duration(duration)
                .build();
    }

    /** Derived end time: start + duration */
    public LocalTime endTime() {
        return startTime.plus(duration);
    }
}
