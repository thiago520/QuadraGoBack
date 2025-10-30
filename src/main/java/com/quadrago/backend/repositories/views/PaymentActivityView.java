package com.quadrago.backend.repositories.views;

import java.time.OffsetDateTime;

public interface PaymentActivityView {
    String getStudentEmail();
    OffsetDateTime getHappenedAt();
}
