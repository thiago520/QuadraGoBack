package com.quadrago.backend.repositories.views;

import java.time.OffsetDateTime;

public interface EnrollmentActivityView {
    String getStudentEmail();      // alias: studentEmail
    OffsetDateTime getHappenedAt(); // alias: happenedAt
}
