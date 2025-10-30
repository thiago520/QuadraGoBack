package com.quadrago.backend.repositories.views;

import java.time.OffsetDateTime;

public interface LessonActivityView {
    String getStudentEmail();
    String getEventLabel();        // alias: eventLabel
    OffsetDateTime getHappenedAt();
}
