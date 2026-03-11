package com.aryan.springboot.leavemanagement.response;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobStatus;
import com.aryan.springboot.leavemanagement.entity.enums.BulkJobStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BulkJobResponse {

    private final Long id;
    private final BulkJobStatus status;
    private final Integer totalRecords;
    private final Integer successfulRecords;
    private final Integer failedRecords;
    private final LocalDateTime createdAt;
}